package com.minero.appirfyachaq.business;

import com.minero.appirfyachaq.dto.request.RequestTerms;
import com.minero.appirfyachaq.dto.response.ResponseTerms;
import com.minero.appirfyachaq.entity.EntityPlant;
import com.minero.appirfyachaq.entity.EntityTerms;
import com.minero.appirfyachaq.repository.RepositoryPlant;
import com.minero.appirfyachaq.repository.RepositoryTerms;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class BusinessTerms {

    private final RepositoryTerms termsRepository;
    private final RepositoryPlant plantRepository;

    public BusinessTerms(RepositoryTerms termsRepository, RepositoryPlant plantRepository) {
        this.termsRepository = termsRepository;
        this.plantRepository = plantRepository;
    }

    public ResponseTerms obtenerPorPlanta(String idPlant) {
        ResponseTerms response = new ResponseTerms();
        
        Optional<EntityPlant> optionalPlant = plantRepository.findById(idPlant);
        
        if (optionalPlant.isEmpty()) {
            response.error("Planta no encontrada.");
            return response;
        }

        EntityTerms terms = optionalPlant.get().getTerms();
        if (terms == null) {
            response.error("Esta planta no tiene términos asignados.");
            return response;
        }

        response.success("Términos obtenidos correctamente.");
        mapearRespuesta(terms, idPlant, response);
        return response;
    }

    public ResponseTerms actualizar(String idPlant, RequestTerms request) {
        ResponseTerms response = new ResponseTerms();

        Optional<EntityPlant> optionalPlant = plantRepository.findById(idPlant);
        
        if (optionalPlant.isEmpty()) {
            response.error("Planta no encontrada.");
            return response;
        }

        EntityPlant plant = optionalPlant.get();
        EntityTerms terms = plant.getTerms();

        if (terms == null) {
            response.error("Esta planta no tiene términos para actualizar.");
            return response;
        }

        try {
            terms.setFactorConcentrado(request.getFactorConcentrado());
            terms.setHumedad(request.getHumedad());
            terms.setMerma(request.getMerma());
            
            terms.setLeyConcentradoCu(request.getLeyConcentradoCu());
            terms.setLeyConcentradoAg(request.getLeyConcentradoAg());
            terms.setLeyConcentradoAu(request.getLeyConcentradoAu());
            
            terms.setPagableCu(request.getPagableCu());
            terms.setDeduccionCu(request.getDeduccionCu());
            terms.setGastosRefCu(request.getGastosRefCu());
            terms.setProteccionCu(request.getProteccionCu());
            
            terms.setPagableAg(request.getPagableAg());
            terms.setDeduccionAg(request.getDeduccionAg());
            terms.setGastosRefAg(request.getGastosRefAg());
            
            terms.setPagableAu(request.getPagableAu());
            terms.setDeduccionAu(request.getDeduccionAu());
            terms.setGastosRefAu(request.getGastosRefAu());
            
            terms.setMaquila(request.getMaquila());
            terms.setAnalisis(request.getAnalisis());
            terms.setTrasladoInterno(request.getTrasladoInterno());
            
            terms.setIgvPorcentaje(request.getIgvPorcentaje());
            terms.setDetraccionPorcentaje(request.getDetraccionPorcentaje());
            
            terms.setUpdatedAt(LocalDateTime.now());

            termsRepository.save(terms);

            response.success("Términos actualizados correctamente.");
            mapearRespuesta(terms, idPlant, response);

        } catch (Exception e) {
            response.exception("Error al actualizar términos: " + e.getMessage());
        }

        return response;
    }

    private void mapearRespuesta(EntityTerms t, String idPlant, ResponseTerms res) {
        res.setIdTerm(t.getIdTerm());
        res.setIdPlant(idPlant);
        
        res.setFactorConcentrado(t.getFactorConcentrado());
        res.setHumedad(t.getHumedad());
        res.setMerma(t.getMerma());
        
        res.setLeyConcentradoCu(t.getLeyConcentradoCu());
        res.setLeyConcentradoAg(t.getLeyConcentradoAg());
        res.setLeyConcentradoAu(t.getLeyConcentradoAu());
        
        res.setPagableCu(t.getPagableCu());
        res.setDeduccionCu(t.getDeduccionCu());
        res.setGastosRefCu(t.getGastosRefCu());
        res.setProteccionCu(t.getProteccionCu());
        
        res.setPagableAg(t.getPagableAg());
        res.setDeduccionAg(t.getDeduccionAg());
        res.setGastosRefAg(t.getGastosRefAg());
        
        res.setPagableAu(t.getPagableAu());
        res.setDeduccionAu(t.getDeduccionAu());
        res.setGastosRefAu(t.getGastosRefAu());
        
        res.setMaquila(t.getMaquila());
        res.setAnalisis(t.getAnalisis());
        res.setTrasladoInterno(t.getTrasladoInterno());
        
        res.setIgvPorcentaje(t.getIgvPorcentaje());
        res.setDetraccionPorcentaje(t.getDetraccionPorcentaje());
    }
}