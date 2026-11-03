package com.minero.appirfyachaq.business;

import com.minero.appirfyachaq.dto.request.RequestCalculation;
import com.minero.appirfyachaq.dto.response.ResponseCalculation;
import com.minero.appirfyachaq.dto.response.ResponseHistorial;
import com.minero.appirfyachaq.entity.EntityCalculationHistory;
import com.minero.appirfyachaq.entity.EntityPlant;
import com.minero.appirfyachaq.entity.EntityTerms;
import com.minero.appirfyachaq.generic.ResponseGeneric;
import com.minero.appirfyachaq.repository.RepositoryCalculation;
import com.minero.appirfyachaq.repository.RepositoryPlant;
import com.minero.appirfyachaq.repository.RepositoryUser;
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
    private final BusinessMarketPrice businessMarketPrice;

    private static final double OZ_A_GR = 31.1035;
    private static final double LB_A_KG = 0.45359;

    public BusinessCalculation(RepositoryCalculation calculationRepository,
                               RepositoryPlant plantRepository,
                               RepositoryUser userRepository,
                               BusinessMarketPrice businessMarketPrice) {
        this.calculationRepository = calculationRepository;
        this.plantRepository = plantRepository;
        this.userRepository = userRepository;
        this.businessMarketPrice = businessMarketPrice;
    }

    private double valor(Double d) {
        return d != null ? d : 0.0;
    }

    public ResponseCalculation calcular(RequestCalculation request) {
        ResponseCalculation response = new ResponseCalculation();

        if (request.getIdUser() == null || !userRepository.existsById(request.getIdUser())) {
            response.error("El usuario no existe.");
            return response;
        }

        Optional<EntityPlant> optPlant = plantRepository.findById(request.getIdPlant());
        if (optPlant.isEmpty()) {
            response.error("Planta no encontrada.");
            return response;
        }

        EntityTerms t = optPlant.get().getTerms();
        if (t == null) {
            response.error("La planta no tiene términos configurados.");
            return response;
        }
        double alquilerPlanta = optPlant.get().getRentalCostPerTM();

        try {
            double tonelaje = valor(request.getTonelaje());
            double leyCu = valor(request.getLey());
            double leyAg = valor(request.getLeyAg());
            double leyAu = valor(request.getLeyAu());

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

            double precioCu = 0.0;
            if (hayCu) {
                precioCu = businessMarketPrice.obtenerPrecio("CU")
                        .map(BigDecimal::doubleValue)
                        .orElse(0.0);
                if (precioCu <= 0) {
                    response.error("No hay precio configurado para el cobre (CU).");
                    return response;
                }
            }

            double precioAg = 0.0;
            if (hayAg) {
                precioAg = businessMarketPrice.obtenerPrecio("AG")
                        .map(BigDecimal::doubleValue)
                        .orElse(0.0);
                if (precioAg <= 0) {
                    response.error("No hay precio configurado para la plata (AG).");
                    return response;
                }
            }

            double precioAu = 0.0;
            if (hayAu) {
                precioAu = businessMarketPrice.obtenerPrecio("AU")
                        .map(BigDecimal::doubleValue)
                        .orElse(0.0);
                if (precioAu <= 0) {
                    response.error("No hay precio configurado para el oro (AU).");
                    return response;
                }
            }

            double leyBase = hayCu ? leyCu : 1.0;
            double tmHumedas = tonelaje * leyBase * t.getFactorConcentrado() / 100;
            double tmns = tmHumedas - (t.getMerma() / 100) - (tmHumedas * t.getHumedad() / 100);

            if (tmns <= 0) {
                response.error("El cálculo no generó concentrado válido.");
                return response;
            }

            double pagableCu = 0.0;
            double precioAjustadoCu = 0.0;
            double pagoCu = 0.0;
            if (hayCu) {
                pagableCu = ((t.getLeyConcentradoCu() - t.getDeduccionCu()) / 100) * (t.getPagableCu() / 100);
                precioAjustadoCu = 1000 * Math.max(0, precioCu - t.getProteccionCu()) / LB_A_KG;
                pagoCu = pagableCu * precioAjustadoCu;
            }

            double contenidoPagableAg = 0.0;
            double precioAjustadoAg = hayAg ? precioAg : 0.0;
            double pagoAg = 0.0;
            if (hayAg) {
                contenidoPagableAg = Math.max(0, leyAg * OZ_A_GR - t.getDeduccionAg())
                        / OZ_A_GR * (t.getPagableAg() / 100);
                pagoAg = contenidoPagableAg * precioAg;
            }

            double contenidoPagableAu = 0.0;
            double precioAjustadoAu = hayAu ? precioAu : 0.0;
            double pagoAu = 0.0;
            if (hayAu) {
                contenidoPagableAu = Math.max(0, leyAu * OZ_A_GR - t.getDeduccionAu())
                        / OZ_A_GR * (t.getPagableAu() / 100);
                pagoAu = contenidoPagableAu * precioAu;
            }

            double pagoTotal = pagoCu + pagoAg + pagoAu;

            double gastosRefCuPorTm = 1000 * t.getGastosRefCu() / LB_A_KG;
            double reduccionRefinacion = pagableCu * gastosRefCuPorTm
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

            double totalAPagar = valorTotal - detraccion - alquilerTotal;

            EntityCalculationHistory historial = EntityCalculationHistory.builder()
                    .idCalculation(UUID.randomUUID().toString())
                    .idUser(request.getIdUser())
                    .material(request.getMaterial())
                    .tonelaje(tonelaje)
                    .ley(leyCu)
                    .precio(precioCu)
                    .alquilerPlanta(alquilerPlanta)
                    .tmHumedas(tmHumedas)
                    .tmns(tmns)
                    .pagoTotal(valorNeto)
                    .totalAPagar(totalAPagar)
                    .calculatedAt(LocalDateTime.now())
                    .isSynced(true)
                    .build();

            calculationRepository.save(historial);

            response.success("Cálculo realizado correctamente.");
            response.setIdCalculation(historial.getIdCalculation());
            response.setMaterial(request.getMaterial());

            response.setTmHumedas(tmHumedas);
            response.setTmns(tmns);

            response.setContenidoPagableCu(pagableCu);
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
            res.setIdCalculation(h.getIdCalculation());
            res.setMaterial(h.getMaterial());
            res.setTonelaje(h.getTonelaje());
            res.setLey(h.getLey());
            res.setPrecio(h.getPrecio());
            res.setTmns(h.getTmns());
            res.setTotalAPagar(h.getTotalAPagar());
            res.setCalculatedAt(h.getCalculatedAt());
            responses.add(res);
        }
        return responses;
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