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
    
    private static final double OZ_A_GR = 31.1035;
    private static final double LB_A_KG = 0.45359;

    public BusinessCalculation(RepositoryCalculation calculationRepository, RepositoryPlant plantRepository,
    		RepositoryUser userRepository) {
        this.calculationRepository = calculationRepository;
        this.plantRepository = plantRepository;
        this.userRepository = userRepository;

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
        double alquilerPlanta = optPlant.get().getRentalCostPerTM();

        try {
            double tmHumedas = request.getTonelaje() * request.getLey() * t.getFactorConcentrado() / 100;
            double tmns = tmHumedas - (t.getMerma() / 100) - (tmHumedas * t.getHumedad() / 100);

            if (tmns <= 0) {
                response.error("El cálculo no generó concentrado válido.");
                return response;
            }

            double pagableCu = ((t.getLeyConcentradoCu() - t.getDeduccionCu()) / 100) * (t.getPagableCu() / 100);
            double precioAjustadoCu = 1000 * Math.max(0, request.getPrecio() - t.getGastosRefCu()) / LB_A_KG;
            double pagoCu = pagableCu * precioAjustadoCu;

            double pagoAg = 0.0;
            double contenidoPagableAg = 0.0;
            double precioAjustadoAg = 0.0;
            if (request.getLeyAg() != null && request.getLeyAg() > 0) {
                double agGramos = request.getLeyAg() * OZ_A_GR;
                contenidoPagableAg = Math.max(0, agGramos - t.getDeduccionAg()) / OZ_A_GR;
                precioAjustadoAg = Math.max(0, (request.getPrecioAg() != null ? request.getPrecioAg() : 0.0) - t.getGastosRefAg());
                pagoAg = contenidoPagableAg * (t.getPagableAg() / 100) * precioAjustadoAg;
            }

            double pagoAu = 0.0;
            double contenidoPagableAu = 0.0;
            double precioAjustadoAu = 0.0;
            if (request.getLeyAu() != null && request.getLeyAu() > 0) {
                double auGramos = request.getLeyAu() * OZ_A_GR;
                contenidoPagableAu = Math.max(0, auGramos - t.getDeduccionAu()) / OZ_A_GR;
                precioAjustadoAu = Math.max(0, (request.getPrecioAu() != null ? request.getPrecioAu() : 0.0) - t.getGastosRefAu());
                pagoAu = contenidoPagableAu * (t.getPagableAu() / 100) * precioAjustadoAu;
            }

            double pagoTotal = pagoCu + pagoAg + pagoAu;

            double gastosRefCuPorTm = 1000 * t.getGastosRefCu() / LB_A_KG;
            double reduccionRefinacion = pagableCu * gastosRefCuPorTm;
            
            double analisisPorTmns = t.getAnalisis() / tmns;
            double trasladoPorTmns = t.getTrasladoInterno() / tmns;

            double pagoPorTm = pagoTotal - t.getMaquila() - reduccionRefinacion - analisisPorTmns - trasladoPorTmns;
            
            double valorNeto = pagoPorTm * tmns;
            double igv = valorNeto * t.getIgvPorcentaje();
            double valorTotal = valorNeto + igv;
            double detraccion = valorTotal * t.getDetraccionPorcentaje();
            double alquilerTotal = alquilerPlanta * request.getTonelaje();
            
            double totalAPagar = valorTotal - detraccion - alquilerTotal;

            EntityCalculationHistory historial = EntityCalculationHistory.builder()
                    .idCalculation(UUID.randomUUID().toString())
                    .idUser(request.getIdUser())
                    .material(request.getMaterial())
                    .tonelaje(request.getTonelaje())
                    .ley(request.getLey())
                    .precio(request.getPrecio())
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