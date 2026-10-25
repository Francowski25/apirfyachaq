package com.minero.appirfyachaq.business;

import com.minero.appirfyachaq.dto.request.RequestTracking;
import com.minero.appirfyachaq.dto.response.ResponseTracking;
import com.minero.appirfyachaq.entity.EntityPlant;
import com.minero.appirfyachaq.entity.EntityTracking;
import com.minero.appirfyachaq.entity.EntityUser;
import com.minero.appirfyachaq.repository.RepositoryPlant;
import com.minero.appirfyachaq.repository.RepositoryTracking;
import com.minero.appirfyachaq.repository.RepositoryUser;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BusinessTracking {

    private final RepositoryTracking trackingRepository;
    private final RepositoryUser userRepository;
    private final RepositoryPlant plantRepository;

    public BusinessTracking(RepositoryTracking trackingRepository, RepositoryUser userRepository, RepositoryPlant plantRepository) {
        this.trackingRepository = trackingRepository;
        this.userRepository = userRepository;
        this.plantRepository = plantRepository;
    }

    public ResponseTracking crear(RequestTracking request) {
        ResponseTracking response = new ResponseTracking();

        Optional<EntityUser> user = userRepository.findById(request.getIdUser());
        Optional<EntityPlant> plant = plantRepository.findById(request.getIdPlant());

        if (user.isEmpty() || plant.isEmpty()) {
            response.error("Usuario o Planta no encontrados.");
            return response;
        }

        try {
            long dias = ChronoUnit.DAYS.between(request.getEntryDate(), LocalDate.now());
            int diasInt = (int) Math.max(0, dias); // Evitar negativos

            EntityTracking tracking = EntityTracking.builder()
                    .idTracking(UUID.randomUUID().toString())
                    .user(user.get())
                    .plant(plant.get())
                    .mineral(request.getMineral())
                    .grossTonnage(request.getGrossTonnage())
                    .entryDate(request.getEntryDate())
                    .status(request.getStatus() != null ? request.getStatus() : "EN PLANTA")
                    .daysInPlant(diasInt)
                    .build();

            trackingRepository.save(tracking);

            response.success("Carga registrada correctamente.");
            mapearRespuesta(tracking, response);

        } catch (Exception e) {
            response.exception("Error al registrar carga: " + e.getMessage());
        }

        return response;
    }

    public List<ResponseTracking> listarPorUsuario(String idUser) {
        List<EntityTracking> lista = trackingRepository.findByUserIdUserOrderByEntryDateDesc(idUser);
        List<ResponseTracking> responses = new ArrayList<>();

        for (EntityTracking t : lista) {
            ResponseTracking res = new ResponseTracking();
            res.success("OK");
            mapearRespuesta(t, res);
            responses.add(res);
        }
        return responses;
    }

    public ResponseTracking actualizarEstado(String idTracking, String nuevoEstado) {
        ResponseTracking response = new ResponseTracking();
        Optional<EntityTracking> opt = trackingRepository.findById(idTracking);

        if (opt.isEmpty()) {
            response.error("Carga no encontrada.");
            return response;
        }

        try {
            EntityTracking t = opt.get();
            t.setStatus(nuevoEstado);
            trackingRepository.save(t);
            
            response.success("Estado actualizado.");
            mapearRespuesta(t, response);
        } catch (Exception e) {
            response.exception("Error al actualizar: " + e.getMessage());
        }
        return response;
    }

    public ResponseTracking eliminar(String idTracking) {
        ResponseTracking response = new ResponseTracking();
        Optional<EntityTracking> opt = trackingRepository.findById(idTracking);

        if (opt.isEmpty()) {
            response.error("Carga no encontrada.");
            return response;
        }

        try {
            trackingRepository.delete(opt.get());
            response.success("Carga eliminada correctamente.");
        } catch (Exception e) {
            response.exception("Error al eliminar: " + e.getMessage());
        }
        return response;
    }

    private void mapearRespuesta(EntityTracking t, ResponseTracking res) {
        res.setIdTracking(t.getIdTracking());
        res.setIdUser(t.getUser().getIdUser());
        res.setIdPlant(t.getPlant().getIdPlant());
        res.setPlantName(t.getPlant().getName());
        res.setMineral(t.getMineral());
        res.setGrossTonnage(t.getGrossTonnage());
        res.setEntryDate(t.getEntryDate());
        res.setStatus(t.getStatus());
        res.setDaysInPlant(t.getDaysInPlant());
    }
}