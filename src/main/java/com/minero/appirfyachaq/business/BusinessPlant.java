package com.minero.appirfyachaq.business;

import com.minero.appirfyachaq.dto.request.RequestPlant;
import com.minero.appirfyachaq.dto.response.ResponsePlant;
import com.minero.appirfyachaq.entity.EntityPlant;
import com.minero.appirfyachaq.entity.EntityTerms;
import com.minero.appirfyachaq.repository.RepositoryPlant;
import com.minero.appirfyachaq.repository.RepositoryTerms;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class BusinessPlant {

    private final RepositoryPlant plantRepository;
    private final RepositoryTerms termsRepository;

    public BusinessPlant(RepositoryPlant plantRepository, RepositoryTerms termsRepository) {
        this.plantRepository = plantRepository;
        this.termsRepository = termsRepository;
    }

    public ResponsePlant crear(RequestPlant request) {
        ResponsePlant response = new ResponsePlant();
        try {
            EntityTerms nuevosTerminos = EntityTerms.builder()
                    .idTerm(UUID.randomUUID().toString())
                    .factorConcentrado(3.3)
                    .humedad(10.1)
                    .merma(1.0)
                    .leyConcentradoCu(25.23)
                    .leyConcentradoAg(0.0)
                    .leyConcentradoAu(0.0)
                    .pagableCu(100.0)
                    .deduccionCu(1.35)
                    .gastosRefCu(0.19)
                    .proteccionCu(0.1)
                    .pagableAg(90.0)
                    .deduccionAg(2.0)
                    .gastosRefAg(1.2)
                    .pagableAu(90.0)
                    .deduccionAu(60.0)
                    .gastosRefAu(12.0)
                    .maquila(175.0)
                    .analisis(100.0)
                    .trasladoInterno(55.0)
                    .igvPorcentaje(0.18)
                    .detraccionPorcentaje(0.10)
                    .updatedAt(LocalDateTime.now())
                    .build();
            termsRepository.save(nuevosTerminos);

            EntityPlant nuevaPlanta = EntityPlant.builder()
                    .idPlant(UUID.randomUUID().toString())
                    .name(request.getName())
                    .location(request.getLocation())
                    .capacityTMAD(request.getCapacityTMAD())
                    .rentalCostPerTM(request.getRentalCostPerTM())
                    .status(request.getStatus() != null ? request.getStatus() : "operativa")
                    .terms(nuevosTerminos)
                    .build();
            plantRepository.save(nuevaPlanta);

            response.success("Planta creada exitosamente con sus términos por defecto.");
            mapearRespuesta(nuevaPlanta, response);
        } catch (Exception e) {
            response.exception("Error al crear planta: " + e.getMessage());
        }
        return response;
    }

    public List<ResponsePlant> listar() {
        List<EntityPlant> plantas = plantRepository.findAll();
        List<ResponsePlant> responses = new ArrayList<>();

        for (EntityPlant p : plantas) {
            ResponsePlant res = new ResponsePlant();
            res.success("OK");
            mapearRespuesta(p, res);
            responses.add(res);
        }
        return responses;
    }

    public ResponsePlant editar(String idPlant, RequestPlant request) {
        ResponsePlant response = new ResponsePlant();
        Optional<EntityPlant> optional = plantRepository.findById(idPlant);

        if (optional.isEmpty()) {
            response.error("Planta no encontrada.");
            return response;
        }

        try {
            EntityPlant planta = optional.get();
            planta.setName(request.getName());
            planta.setLocation(request.getLocation());
            planta.setCapacityTMAD(request.getCapacityTMAD());
            planta.setRentalCostPerTM(request.getRentalCostPerTM());
            if (request.getStatus() != null) planta.setStatus(request.getStatus());

            plantRepository.save(planta);

            response.success("Planta actualizada correctamente.");
            mapearRespuesta(planta, response);
        } catch (Exception e) {
            response.exception("Error al actualizar: " + e.getMessage());
        }
        return response;
    }

    public ResponsePlant eliminar(String idPlant) {
        ResponsePlant response = new ResponsePlant();
        Optional<EntityPlant> optional = plantRepository.findById(idPlant);

        if (optional.isEmpty()) {
            response.error("Planta no encontrada.");
            return response;
        }

        try {
            plantRepository.delete(optional.get());
            response.success("Planta eliminada correctamente.");
        } catch (Exception e) {
            response.exception("Error al eliminar: " + e.getMessage());
        }
        return response;
    }

    private void mapearRespuesta(EntityPlant p, ResponsePlant res) {
        res.setIdPlant(p.getIdPlant());
        res.setIdTerm(p.getTerms().getIdTerm());
        res.setName(p.getName());
        res.setLocation(p.getLocation());
        res.setCapacityTMAD(p.getCapacityTMAD());
        res.setRentalCostPerTM(p.getRentalCostPerTM());
        res.setStatus(p.getStatus());
    }
}