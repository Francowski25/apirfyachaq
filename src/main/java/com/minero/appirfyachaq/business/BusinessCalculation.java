package com.minero.appirfyachaq.business;

import com.minero.appirfyachaq.dto.request.RequestCalculation;
import com.minero.appirfyachaq.dto.response.ResponseCalculation;
import com.minero.appirfyachaq.dto.response.ResponseHistorial;
import com.minero.appirfyachaq.entity.EntityCalculationHistory;
import com.minero.appirfyachaq.entity.EntityPlant;
import com.minero.appirfyachaq.entity.EntityTerms;
import com.minero.appirfyachaq.repository.RepositoryCalculation;
import com.minero.appirfyachaq.repository.RepositoryPlant;
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

    public BusinessCalculation(RepositoryCalculation calculationRepository, RepositoryPlant plantRepository) {
        this.calculationRepository = calculationRepository;
        this.plantRepository = plantRepository;
    }

    public ResponseCalculation calcular(RequestCalculation request) {
        ResponseCalculation response = new ResponseCalculation();

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
            double precioAjustadoCu = 1000 * Math.max(0, request.getPrecio() - t.getGastosRefCu()) / 0.45359;
            double pagoCu = pagableCu * precioAjustadoCu;

            double gastosRefCuPorTm = 1000 * t.getGastosRefCu() / 0.45359;
            double reduccionRefinacion = pagableCu * gastosRefCuPorTm;
            
            double analisisPorTmns = t.getAnalisis() / tmns;
            double trasladoPorTmns = t.getTrasladoInterno() / tmns;

            double pagoPorTm = pagoCu - t.getMaquila() - reduccionRefinacion - analisisPorTmns - trasladoPorTmns;
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
                    .pagoTotal(pagoCu)
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
            response.setPagoTotal(pagoCu); 
            response.setTotalAPagar(totalAPagar);
            
            response.setValorNeto(valorNeto);
            response.setIgv(igv);
            response.setDetraccion(detraccion);
            response.setAlquilerTotal(alquilerTotal);

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

}