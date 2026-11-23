package com.minero.appirfyachaq.business;

import com.minero.appirfyachaq.dto.request.RequestCalculation;
import com.minero.appirfyachaq.dto.response.ResponseCalculation;
import com.minero.appirfyachaq.dto.response.ResponseHistorial;
import com.minero.appirfyachaq.entity.EntityCalculationHistory;
import com.minero.appirfyachaq.entity.EntityPlant;
import com.minero.appirfyachaq.entity.EntityTerms;
import com.minero.appirfyachaq.entity.EntityUserPlant;
import com.minero.appirfyachaq.generic.ResponseGeneric;
import com.minero.appirfyachaq.repository.RepositoryCalculation;
import com.minero.appirfyachaq.repository.RepositoryPlant;
import com.minero.appirfyachaq.repository.RepositoryUser;
import com.minero.appirfyachaq.repository.RepositoryUserPlant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class BusinessCalculation {

    private final RepositoryCalculation calculationRepository;
    private final RepositoryPlant plantRepository;
    private final RepositoryUser userRepository;
    private final RepositoryUserPlant userPlantRepository;
    private final BusinessMarketPrice businessMarketPrice;
    private final BusinessUserTerms businessUserTerms;

    private static final double LB_A_KG = 0.45359;

    public BusinessCalculation(RepositoryCalculation calculationRepository,
                               RepositoryPlant plantRepository,
                               RepositoryUser userRepository,
                               RepositoryUserPlant userPlantRepository,
                               BusinessMarketPrice businessMarketPrice,
                               BusinessUserTerms businessUserTerms) {
        this.calculationRepository = calculationRepository;
        this.plantRepository = plantRepository;
        this.userRepository = userRepository;
        this.userPlantRepository = userPlantRepository;
        this.businessMarketPrice = businessMarketPrice;
        this.businessUserTerms = businessUserTerms;
    }

    private double valor(Double d) {
        return d != null ? d : 0.0;
    }

    private double precio(String metal) {
        return businessMarketPrice.obtenerPrecio(metal)
                .map(BigDecimal::doubleValue)
                .orElse(0.0);
    }

    private LocalDateTime fechaPrecio(String metal) {
        return businessMarketPrice.obtenerActualizacion(metal).orElse(null);
    }

    private LocalDateTime masAntigua(LocalDateTime a, LocalDateTime b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.isBefore(b) ? a : b;
    }

    public ResponseCalculation calcular(RequestCalculation request) {
        ResponseCalculation response = new ResponseCalculation();

        if (request.getIdUser() == null || !userRepository.existsById(request.getIdUser())) {
            response.error("El usuario no existe.");
            return response;
        }

        boolean esSistema = request.getIdPlant() != null && !request.getIdPlant().isBlank();
        boolean esPersonal = request.getIdUserPlant() != null && !request.getIdUserPlant().isBlank();

        if (esSistema == esPersonal) {
            response.error("Seleccione una planta válida.");
            return response;
        }

        EntityTerms t;
        double alquilerPlanta;

        String plantType;
        String plantName;
        String plantLocation;
        String termsSource;

        if (esPersonal) {
            Optional<EntityUserPlant> optUserPlant = userPlantRepository
                    .findByIdUserPlantAndUser_IdUser(request.getIdUserPlant(), request.getIdUser());
            if (optUserPlant.isEmpty()) {
                response.error("Planta personal no encontrada.");
                return response;
            }
            EntityUserPlant userPlant = optUserPlant.get();
            t = businessUserTerms.obtenerTerminos(request.getIdUser(), userPlant);
            if (t == null) {
                response.error("La planta no tiene términos configurados.");
                return response;
            }
            alquilerPlanta = userPlant.getRentalCostPerTM();
            plantType = "PERSONAL";
            plantName = userPlant.getName();
            plantLocation = userPlant.getLocation();
            termsSource = businessUserTerms.tienePersonalizados(request.getIdUser(), userPlant)
                    ? "PERSONALIZADO" : "GENERAL";
        } else {
            Optional<EntityPlant> optPlant = plantRepository.findById(request.getIdPlant());
            if (optPlant.isEmpty()) {
                response.error("Planta no encontrada.");
                return response;
            }
            EntityPlant plant = optPlant.get();
            t = businessUserTerms.obtenerTerminos(request.getIdUser(), plant);
            if (t == null) {
                response.error("La planta no tiene términos configurados.");
                return response;
            }
            alquilerPlanta = plant.getRentalCostPerTM();
            plantType = "OFICIAL";
            plantName = plant.getName();
            plantLocation = plant.getLocation();
            termsSource = businessUserTerms.tienePersonalizados(request.getIdUser(), plant)
                    ? "PERSONALIZADO" : "GENERAL";
        }

        try {
            double tonelaje = valor(request.getTonelaje());
            double leyCu = valor(request.getLeyCu());
            double leyAg = valor(request.getLeyAg());
            double leyAu = valor(request.getLeyAu());
            double adelanto = valor(request.getAdelanto());

            if (tonelaje <= 0) {
                response.error("El tonelaje debe ser mayor que cero.");
                return response;
            }

            if (leyCu <= 0 && leyAg <= 0 && leyAu <= 0) {
                response.error("Ingrese la ley de al menos un metal.");
                return response;
            }

            boolean hayCu = leyCu > 0;
            boolean hayAg = leyAg > 0;
            boolean hayAu = leyAu > 0;

            LocalDateTime preciosUpdatedAt = null;

            double precioCu = 0.0;
            if (hayCu) {
                precioCu = precio("CU");
                if (precioCu <= 0) {
                    response.error("No hay precio configurado para el cobre (CU).");
                    return response;
                }
                preciosUpdatedAt = masAntigua(preciosUpdatedAt, fechaPrecio("CU"));
            }

            double precioAg = 0.0;
            if (hayAg) {
                precioAg = precio("AG");
                if (precioAg <= 0) {
                    response.error("No hay precio configurado para la plata (AG).");
                    return response;
                }
                preciosUpdatedAt = masAntigua(preciosUpdatedAt, fechaPrecio("AG"));
            }

            double precioAu = 0.0;
            if (hayAu) {
                precioAu = precio("AU");
                if (precioAu <= 0) {
                    response.error("No hay precio configurado para el oro (AU).");
                    return response;
                }
                preciosUpdatedAt = masAntigua(preciosUpdatedAt, fechaPrecio("AU"));
            }

            double tmHumedas = tonelaje * leyCu * t.getFactorConcentrado() / 100;
            double tmns = hayCu
                    ? tmHumedas - t.getMerma() / 100 - tmHumedas * t.getHumedad() / 100
                    : tonelaje - tonelaje * t.getHumedad() / 100;

            if (tmns <= 0) {
                response.error("El cálculo no generó concentrado válido.");
                return response;
            }

            double contenidoPagableCu = 0.0;
            double precioAjustadoCu = 0.0;
            double pagoCu = 0.0;
            if (hayCu) {
                contenidoPagableCu = (t.getLeyConcentradoCu() - t.getDeduccionCu()) / 100 * (t.getPagableCu() / 100);
                precioAjustadoCu = 1000 * (precioCu - t.getProteccionCu()) / LB_A_KG;
                pagoCu = contenidoPagableCu * precioAjustadoCu;
            }

            double contenidoPagableAg = 0.0;
            double precioAjustadoAg = 0.0;
            double pagoAg = 0.0;
            if (hayAg) {
                contenidoPagableAg = leyAg * t.getPagableAg() / 100;
                precioAjustadoAg = precioAg - t.getDeduccionAg();
                pagoAg = contenidoPagableAg * precioAjustadoAg;
            }

            double contenidoPagableAu = 0.0;
            double precioAjustadoAu = 0.0;
            double pagoAu = 0.0;
            if (hayAu) {
                contenidoPagableAu = leyAu * t.getPagableAu() / 100;
                precioAjustadoAu = precioAu - t.getDeduccionAu();
                pagoAu = contenidoPagableAu * precioAjustadoAu;
            }

            double pagoTotal = pagoCu + pagoAg + pagoAu;

            double gastosRefCuPorTm = 1000 * t.getGastosRefCu() / LB_A_KG;
            double reduccionRefinacion = contenidoPagableCu * gastosRefCuPorTm
                    + contenidoPagableAg * t.getGastosRefAg()
                    + contenidoPagableAu * t.getGastosRefAu();

            double analisisPorTmns = t.getAnalisis() / tmns;
            double trasladoPorTmns = t.getTrasladoInterno() / tmns;

            double pagoPorTm = pagoTotal - t.getMaquila() - reduccionRefinacion - analisisPorTmns - trasladoPorTmns;

            double valorNeto = pagoPorTm * tmns;
            double igv = valorNeto * t.getIgvPorcentaje();
            double valorTotal = valorNeto + igv;
            double detraccion = valorTotal * t.getDetraccionPorcentaje();
            double alquilerTotal = alquilerPlanta * tonelaje;

            double totalAPagar = valorTotal - detraccion - alquilerTotal - adelanto;

            EntityCalculationHistory historial = EntityCalculationHistory.builder()
                    .idCalculation(UUID.randomUUID().toString())
                    .idUser(request.getIdUser())

                    .idPlant(esSistema ? request.getIdPlant() : null)
                    .idUserPlant(esPersonal ? request.getIdUserPlant() : null)
                    .plantType(plantType)
                    .plantName(plantName)
                    .plantLocation(plantLocation)
                    .plantRentalCostPerTM(alquilerPlanta)
                    .termsSource(termsSource)

                    .factorConcentrado(t.getFactorConcentrado())
                    .humedad(t.getHumedad())
                    .merma(t.getMerma())
                    .leyConcentradoCu(t.getLeyConcentradoCu())
                    .leyConcentradoAg(t.getLeyConcentradoAg())
                    .leyConcentradoAu(t.getLeyConcentradoAu())
                    .pagableCu(t.getPagableCu())
                    .deduccionCu(t.getDeduccionCu())
                    .gastosRefCu(t.getGastosRefCu())
                    .proteccionCu(t.getProteccionCu())
                    .pagableAg(t.getPagableAg())
                    .deduccionAg(t.getDeduccionAg())
                    .gastosRefAg(t.getGastosRefAg())
                    .pagableAu(t.getPagableAu())
                    .deduccionAu(t.getDeduccionAu())
                    .gastosRefAu(t.getGastosRefAu())
                    .maquila(t.getMaquila())
                    .analisis(t.getAnalisis())
                    .trasladoInterno(t.getTrasladoInterno())
                    .igvPorcentaje(t.getIgvPorcentaje())
                    .detraccionPorcentaje(t.getDetraccionPorcentaje())

                    .material(request.getMaterial())
                    .tonelaje(tonelaje)
                    .leyCu(leyCu)
                    .leyAg(leyAg)
                    .leyAu(leyAu)
                    .adelanto(adelanto)

                    .precioCu(precioCu)
                    .precioAg(precioAg)
                    .precioAu(precioAu)
                    .preciosUpdatedAt(preciosUpdatedAt)

                    .tmHumedas(tmHumedas)
                    .tmns(tmns)
                    .contenidoPagableCu(contenidoPagableCu)
                    .precioAjustadoCu(precioAjustadoCu)
                    .pagoCu(pagoCu)
                    .contenidoPagableAg(contenidoPagableAg)
                    .precioAjustadoAg(precioAjustadoAg)
                    .pagoAg(pagoAg)
                    .contenidoPagableAu(contenidoPagableAu)
                    .precioAjustadoAu(precioAjustadoAu)
                    .pagoAu(pagoAu)
                    .pagoTotal(pagoTotal)
                    .reduccionRefinacion(reduccionRefinacion)
                    .analisisPorTmns(analisisPorTmns)
                    .trasladoPorTmns(trasladoPorTmns)
                    .pagoPorTm(pagoPorTm)
                    .valorNeto(valorNeto)
                    .igv(igv)
                    .valorTotal(valorTotal)
                    .detraccion(detraccion)
                    .alquilerPlanta(alquilerPlanta)
                    .alquilerTotal(alquilerTotal)
                    .totalAPagar(totalAPagar)

                    .calculatedAt(LocalDateTime.now())
                    .isSynced(true)
                    .build();

            calculationRepository.saveAndFlush(historial);

            response.success("Cálculo realizado correctamente.");
            response.setIdCalculation(historial.getIdCalculation());
            response.setMaterial(request.getMaterial());

            response.setTmHumedas(tmHumedas);
            response.setTmns(tmns);

            response.setContenidoPagableCu(contenidoPagableCu);
            response.setPrecioAjustadoCu(precioAjustadoCu);
            response.setPagoCu(pagoCu);

            response.setContenidoPagableAg(contenidoPagableAg);
            response.setPrecioAjustadoAg(precioAjustadoAg);
            response.setPagoAg(pagoAg);

            response.setContenidoPagableAu(contenidoPagableAu);
            response.setPrecioAjustadoAu(precioAjustadoAu);
            response.setPagoAu(pagoAu);

            response.setPagoTotal(valorNeto);

            response.setReduccionRefinacion(reduccionRefinacion);
            response.setAnalisisPorTmns(analisisPorTmns);
            response.setTrasladoPorTmns(trasladoPorTmns);
            response.setPagoPorTm(pagoPorTm);

            response.setValorNeto(valorNeto);
            response.setIgv(igv);
            response.setValorTotal(valorTotal);
            response.setDetraccion(detraccion);
            response.setAlquilerTotal(alquilerTotal);
            response.setAdelanto(adelanto);
            response.setTotalAPagar(totalAPagar);

        } catch (Exception e) {
            response.exception("Error al calcular: " + e.getMessage());
        }

        return response;
    }

    public List<ResponseHistorial> listarHistorial(String idUser) {
        List<EntityCalculationHistory> historial = calculationRepository.findByIdUserOrderByCalculatedAtDesc(idUser);
        List<ResponseHistorial> responses = new ArrayList<>();

        for (EntityCalculationHistory h : historial) {
            ResponseHistorial res = new ResponseHistorial();
            res.success("OK");
            mapearHistorial(h, res);
            responses.add(res);
        }
        return responses;
    }

    private void mapearHistorial(EntityCalculationHistory h, ResponseHistorial res) {
        res.setIdCalculation(h.getIdCalculation());

        res.setMaterial(h.getMaterial());
        res.setTonelaje(h.getTonelaje());
        res.setLey(h.getLeyCu());
        res.setPrecio(h.getPrecioCu());
        res.setTmns(h.getTmns());
        res.setTotalAPagar(h.getTotalAPagar());
        res.setCalculatedAt(h.getCalculatedAt());

        res.setIdPlant(h.getIdPlant());
        res.setIdUserPlant(h.getIdUserPlant());
        res.setPlantType(h.getPlantType());
        res.setPlantName(h.getPlantName());
        res.setPlantLocation(h.getPlantLocation());
        res.setPlantRentalCostPerTM(h.getPlantRentalCostPerTM());
        res.setTermsSource(h.getTermsSource());

        res.setFactorConcentrado(h.getFactorConcentrado());
        res.setHumedad(h.getHumedad());
        res.setMerma(h.getMerma());
        res.setLeyConcentradoCu(h.getLeyConcentradoCu());
        res.setLeyConcentradoAg(h.getLeyConcentradoAg());
        res.setLeyConcentradoAu(h.getLeyConcentradoAu());
        res.setPagableCu(h.getPagableCu());
        res.setDeduccionCu(h.getDeduccionCu());
        res.setGastosRefCu(h.getGastosRefCu());
        res.setProteccionCu(h.getProteccionCu());
        res.setPagableAg(h.getPagableAg());
        res.setDeduccionAg(h.getDeduccionAg());
        res.setGastosRefAg(h.getGastosRefAg());
        res.setPagableAu(h.getPagableAu());
        res.setDeduccionAu(h.getDeduccionAu());
        res.setGastosRefAu(h.getGastosRefAu());
        res.setMaquila(h.getMaquila());
        res.setAnalisis(h.getAnalisis());
        res.setTrasladoInterno(h.getTrasladoInterno());
        res.setIgvPorcentaje(h.getIgvPorcentaje());
        res.setDetraccionPorcentaje(h.getDetraccionPorcentaje());

        res.setLeyCu(h.getLeyCu());
        res.setLeyAg(h.getLeyAg());
        res.setLeyAu(h.getLeyAu());
        res.setAdelanto(h.getAdelanto());

        res.setPrecioCu(h.getPrecioCu());
        res.setPrecioAg(h.getPrecioAg());
        res.setPrecioAu(h.getPrecioAu());
        res.setPreciosUpdatedAt(h.getPreciosUpdatedAt());

        res.setTmHumedas(h.getTmHumedas());
        res.setContenidoPagableCu(h.getContenidoPagableCu());
        res.setPrecioAjustadoCu(h.getPrecioAjustadoCu());
        res.setPagoCu(h.getPagoCu());
        res.setContenidoPagableAg(h.getContenidoPagableAg());
        res.setPrecioAjustadoAg(h.getPrecioAjustadoAg());
        res.setPagoAg(h.getPagoAg());
        res.setContenidoPagableAu(h.getContenidoPagableAu());
        res.setPrecioAjustadoAu(h.getPrecioAjustadoAu());
        res.setPagoAu(h.getPagoAu());
        res.setPagoTotal(h.getPagoTotal());
        res.setReduccionRefinacion(h.getReduccionRefinacion());
        res.setAnalisisPorTmns(h.getAnalisisPorTmns());
        res.setTrasladoPorTmns(h.getTrasladoPorTmns());
        res.setPagoPorTm(h.getPagoPorTm());
        res.setValorNeto(h.getValorNeto());
        res.setIgv(h.getIgv());
        res.setValorTotal(h.getValorTotal());
        res.setDetraccion(h.getDetraccion());
        res.setAlquilerPlanta(h.getAlquilerPlanta());
        res.setAlquilerTotal(h.getAlquilerTotal());
        res.setIsSynced(h.getIsSynced());
    }

    public ResponseGeneric eliminarCalculo(String idUser, String idCalculation) {
        ResponseGeneric response = new ResponseGeneric() {};
        Optional<EntityCalculationHistory> opt = calculationRepository.findById(idCalculation);

        if (opt.isEmpty()) {
            response.error("El cálculo no existe.");
            return response;
        }

        if (!opt.get().getIdUser().equals(idUser)) {
            response.error("No tiene permiso para eliminar este cálculo.");
            return response;
        }

        try {
            calculationRepository.delete(opt.get());
            response.success("Cálculo eliminado correctamente.");
        } catch (Exception e) {
            response.exception("Error al eliminar: " + e.getMessage());
        }
        return response;
    }

    public ResponseGeneric eliminarTodoHistorial(String idUser) {
        ResponseGeneric response = new ResponseGeneric() {};
        try {
            calculationRepository.deleteByIdUser(idUser);
            response.success("Historial eliminado correctamente.");
        } catch (Exception e) {
            response.exception("Error al eliminar el historial: " + e.getMessage());
        }
        return response;
    }
}