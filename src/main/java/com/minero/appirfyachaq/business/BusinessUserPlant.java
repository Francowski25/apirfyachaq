
package com.minero.appirfyachaq.business;

import com.minero.appirfyachaq.dto.request.RequestUserPlant;
import com.minero.appirfyachaq.dto.response.ResponseUserPlant;
import com.minero.appirfyachaq.entity.EntityTerms;
import com.minero.appirfyachaq.entity.EntityUser;
import com.minero.appirfyachaq.entity.EntityUserPlant;
import com.minero.appirfyachaq.repository.RepositoryTerms;
import com.minero.appirfyachaq.repository.RepositoryUser;
import com.minero.appirfyachaq.repository.RepositoryUserPlant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class BusinessUserPlant {

    private static final String STATUS_OPERATIVE = "Operativa";
    private static final String STATUS_MAINTENANCE = "Mantenimiento";

    private final RepositoryUserPlant userPlantRepository;
    private final RepositoryTerms termsRepository;
    private final RepositoryUser userRepository;

    public BusinessUserPlant(
            RepositoryUserPlant userPlantRepository,
            RepositoryTerms termsRepository,
            RepositoryUser userRepository) {
        this.userPlantRepository = userPlantRepository;
        this.termsRepository = termsRepository;
        this.userRepository = userRepository;
    }

    public ResponseUserPlant crear(String idUser, RequestUserPlant request) {
        ResponseUserPlant response = new ResponseUserPlant();

        try {
            Optional<EntityUser> user = usuarioMineroActivo(idUser);
            if (user.isEmpty()) {
                response.error("El usuario no puede administrar plantas personales.");
                return response;
            }

            if (!requestValido(request, response)) {
                return response;
            }

            LocalDateTime now = LocalDateTime.now();

            // Crear términos predeterminados automáticamente
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
                    .updatedAt(now)
                    .build();

            termsRepository.save(nuevosTerminos);

            // Crear planta personal asociada a sus propios términos
            EntityUserPlant plant = EntityUserPlant.builder()
                    .idUserPlant(UUID.randomUUID().toString())
                    .user(user.get())
                    .terms(nuevosTerminos)
                    .name(request.getName())
                    .location(request.getLocation())
                    .capacityTMAD(request.getCapacityTMAD())
                    .rentalCostPerTM(request.getRentalCostPerTM())
                    .status(request.getStatus() != null
                            ? request.getStatus()
                            : STATUS_OPERATIVE)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            userPlantRepository.save(plant);

            response.success("Planta personal creada exitosamente con sus términos por defecto.");
            mapearRespuesta(plant, response);

        } catch (Exception e) {
            response.exception("Error al crear planta personal: " + e.getMessage());
        }

        return response;
    }

    public List<ResponseUserPlant> listar(String idUser) {
        List<ResponseUserPlant> responses = new ArrayList<>();

        try {
            if (usuarioMineroActivo(idUser).isEmpty()) {
                ResponseUserPlant response = new ResponseUserPlant();
                response.error("El usuario no puede administrar plantas personales.");
                responses.add(response);
                return responses;
            }

            for (EntityUserPlant plant : userPlantRepository.findByUser_IdUser(idUser)) {
                ResponseUserPlant response = new ResponseUserPlant();
                response.success("OK");
                mapearRespuesta(plant, response);
                responses.add(response);
            }

        } catch (Exception e) {
            ResponseUserPlant response = new ResponseUserPlant();
            response.exception("Error al listar plantas personales: " + e.getMessage());
            responses.add(response);
        }

        return responses;
    }

    public ResponseUserPlant obtener(String idUser, String idUserPlant) {
        ResponseUserPlant response = new ResponseUserPlant();

        try {
            if (usuarioMineroActivo(idUser).isEmpty()) {
                response.error("El usuario no puede administrar plantas personales.");
                return response;
            }

            Optional<EntityUserPlant> plant =
                    userPlantRepository.findByIdUserPlantAndUser_IdUser(idUserPlant, idUser);

            if (plant.isEmpty()) {
                response.error("Planta personal no encontrada.");
                return response;
            }

            response.success("OK");
            mapearRespuesta(plant.get(), response);

        } catch (Exception e) {
            response.exception("Error al consultar planta personal: " + e.getMessage());
        }

        return response;
    }

    public ResponseUserPlant editar(String idUser, String idUserPlant, RequestUserPlant request) {
        ResponseUserPlant response = new ResponseUserPlant();

        if (!requestValido(request, response)) {
            return response;
        }

        try {
            if (usuarioMineroActivo(idUser).isEmpty()) {
                response.error("El usuario no puede administrar plantas personales.");
                return response;
            }

            Optional<EntityUserPlant> existing =
                    userPlantRepository.findByIdUserPlantAndUser_IdUser(idUserPlant, idUser);

            if (existing.isEmpty()) {
                response.error("Planta personal no encontrada.");
                return response;
            }

            EntityUserPlant plant = existing.get();

            // Se mantienen los términos que ya pertenecen a esta planta
            plant.setName(request.getName());
            plant.setLocation(request.getLocation());
            plant.setCapacityTMAD(request.getCapacityTMAD());
            plant.setRentalCostPerTM(request.getRentalCostPerTM());

            if (request.getStatus() != null) {
                plant.setStatus(request.getStatus());
            }

            plant.setUpdatedAt(LocalDateTime.now());

            userPlantRepository.save(plant);

            response.success("Planta personal actualizada correctamente.");
            mapearRespuesta(plant, response);

        } catch (Exception e) {
            response.exception("Error al actualizar planta personal: " + e.getMessage());
        }

        return response;
    }

    public ResponseUserPlant eliminar(String idUser, String idUserPlant) {
        ResponseUserPlant response = new ResponseUserPlant();

        try {
            if (usuarioMineroActivo(idUser).isEmpty()) {
                response.error("El usuario no puede administrar plantas personales.");
                return response;
            }

            Optional<EntityUserPlant> plant =
                    userPlantRepository.findByIdUserPlantAndUser_IdUser(idUserPlant, idUser);

            if (plant.isEmpty()) {
                response.error("Planta personal no encontrada.");
                return response;
            }

            userPlantRepository.delete(plant.get());
            response.success("Planta personal eliminada correctamente.");

        } catch (Exception e) {
            response.exception("Error al eliminar planta personal: " + e.getMessage());
        }

        return response;
    }

    private Optional<EntityUser> usuarioMineroActivo(String idUser) {
        if (idUser == null || idUser.isBlank()) {
            return Optional.empty();
        }

        return userRepository.findById(idUser)
                .filter(user -> Boolean.TRUE.equals(user.getStatus()))
                .filter(user -> "minero".equalsIgnoreCase(user.getRole()));
    }

    private boolean requestValido(RequestUserPlant request, ResponseUserPlant response) {
        if (request == null
                || request.getName() == null || request.getName().isBlank()
                || request.getName().length() > 150
                || request.getLocation() == null || request.getLocation().isBlank()
                || request.getLocation().length() > 200
                || request.getCapacityTMAD() == null || request.getCapacityTMAD() <= 0
                || request.getRentalCostPerTM() == null || request.getRentalCostPerTM() < 0
                || (request.getStatus() != null
                    && !STATUS_OPERATIVE.equals(request.getStatus())
                    && !STATUS_MAINTENANCE.equals(request.getStatus()))) {

            response.error("Los datos de la planta personal no son válidos.");
            return false;
        }

        return true;
    }

    private void mapearRespuesta(EntityUserPlant plant, ResponseUserPlant response) {
        response.setIdUserPlant(plant.getIdUserPlant());
        response.setIdTerm(plant.getTerms().getIdTerm());
        response.setName(plant.getName());
        response.setLocation(plant.getLocation());
        response.setCapacityTMAD(plant.getCapacityTMAD());
        response.setRentalCostPerTM(plant.getRentalCostPerTM());
        response.setStatus(plant.getStatus());
        response.setCreatedAt(plant.getCreatedAt());
        response.setUpdatedAt(plant.getUpdatedAt());
    }
}
