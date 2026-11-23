package com.minero.appirfyachaq.business;

import com.minero.appirfyachaq.dto.request.RequestUserTerms;
import com.minero.appirfyachaq.dto.response.ResponseUserTerms;
import com.minero.appirfyachaq.entity.EntityPlant;
import com.minero.appirfyachaq.entity.EntityTerms;
import com.minero.appirfyachaq.entity.EntityUserFavorite;
import com.minero.appirfyachaq.entity.EntityUserPlant;
import com.minero.appirfyachaq.entity.EntityUserTerms;
import com.minero.appirfyachaq.generic.ResponseGeneric;
import com.minero.appirfyachaq.repository.RepositoryPlant;
import com.minero.appirfyachaq.repository.RepositoryUser;
import com.minero.appirfyachaq.repository.RepositoryUserFavorite;
import com.minero.appirfyachaq.repository.RepositoryUserPlant;
import com.minero.appirfyachaq.repository.RepositoryUserTerms;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class BusinessUserTerms {

    private final RepositoryUserTerms userTermsRepository;
    private final RepositoryPlant plantRepository;
    private final RepositoryUser userRepository;
    private final RepositoryUserFavorite userFavoriteRepository;
    private final RepositoryUserPlant userPlantRepository;

    public BusinessUserTerms(RepositoryUserTerms userTermsRepository,
                             RepositoryPlant plantRepository,
                             RepositoryUser userRepository,
                             RepositoryUserFavorite userFavoriteRepository,
                             RepositoryUserPlant userPlantRepository) {
        this.userTermsRepository = userTermsRepository;
        this.plantRepository = plantRepository;
        this.userRepository = userRepository;
        this.userFavoriteRepository = userFavoriteRepository;
        this.userPlantRepository = userPlantRepository;
    }

    public List<ResponseUserTerms> listar(String idUser) {
        List<ResponseUserTerms> responses = new ArrayList<>();

        if (idUser == null || !userRepository.existsById(idUser)) {
            ResponseUserTerms res = new ResponseUserTerms();
            res.error("El usuario no existe.");
            responses.add(res);
            return responses;
        }

        List<EntityPlant> plantas = plantRepository.findAll();
        List<EntityUserPlant> plantasPersonales = userPlantRepository.findByUser_IdUser(idUser);

        Map<String, EntityUserTerms> personalizadosOficiales = new HashMap<>();
        Map<String, EntityUserTerms> personalizadosPersonales = new HashMap<>();
        for (EntityUserTerms ut : userTermsRepository.findByIdUser(idUser)) {
            if (ut.getIdPlant() != null) {
                personalizadosOficiales.put(ut.getIdPlant(), ut);
            } else if (ut.getIdUserPlant() != null) {
                personalizadosPersonales.put(ut.getIdUserPlant(), ut);
            }
        }

        Set<String> favoritas = new HashSet<>();
        for (EntityUserFavorite f : userFavoriteRepository.findByIdUser(idUser)) {
            favoritas.add(f.getIdPlant());
        }

        for (EntityPlant p : plantas) {
            EntityUserTerms ut = personalizadosOficiales.get(p.getIdPlant());
            EntityTerms t = (ut != null) ? aTerms(ut) : p.getTerms();

            if (t == null) {
                continue;
            }

            ResponseUserTerms res = new ResponseUserTerms();
            res.success("OK");
            mapearRespuesta(p.getIdPlant(), null, p.getName(), p.getLocation(),
                    t, ut, favoritas.contains(p.getIdPlant()), res);
            responses.add(res);
        }

        for (EntityUserPlant p : plantasPersonales) {
            EntityUserTerms ut = personalizadosPersonales.get(p.getIdUserPlant());
            EntityTerms t = (ut != null) ? aTerms(ut) : p.getTerms();
            if (t == null) {
                continue;
            }

            ResponseUserTerms res = new ResponseUserTerms();
            res.success("OK");
            mapearRespuesta(null, p.getIdUserPlant(), p.getName(), p.getLocation(),
                    t, ut, false, res);
            responses.add(res);
        }

        if (responses.isEmpty()) {
            ResponseUserTerms res = new ResponseUserTerms();
            res.error("No hay plantas con términos registrados.");
            responses.add(res);
        }
        return responses;
    }

    public ResponseGeneric guardar(String authenticatedUserId, RequestUserTerms request) {
        ResponseGeneric response = new ResponseGeneric() {};

        if (request == null || authenticatedUserId == null || !userRepository.existsById(authenticatedUserId)) {
            response.error("El usuario no existe.");
            return response;
        }

        boolean officialPlant = request.getIdPlant() != null && !request.getIdPlant().isBlank();
        boolean personalPlant = request.getIdUserPlant() != null && !request.getIdUserPlant().isBlank();
        if (officialPlant == personalPlant) {
            response.error("Debe seleccionar exactamente una planta.");
            return response;
        }

        EntityTerms defaultTerms;
        String plantId;
        String userPlantId = null;
        String plantName;
        if (officialPlant) {
            EntityPlant plant = plantRepository.findById(request.getIdPlant()).orElse(null);
            if (plant == null) {
                response.error("La planta seleccionada no existe.");
                return response;
            }
            defaultTerms = plant.getTerms();
            plantId = plant.getIdPlant();
            plantName = plant.getName();
        } else {
            EntityUserPlant plant = userPlantRepository
                    .findByIdUserPlantAndUser_IdUser(request.getIdUserPlant(), authenticatedUserId)
                    .orElse(null);
            if (plant == null) {
                response.error("La planta personal no existe o no pertenece al usuario.");
                return response;
            }
            if (request.getFavorita() != null) {
                response.error("Las plantas personales no se pueden marcar como favoritas.");
                return response;
            }
            defaultTerms = plant.getTerms();
            plantId = null;
            userPlantId = plant.getIdUserPlant();
            plantName = plant.getName();
        }

        // Hay términos si viene al menos un valor; ninguno puede ser negativo
        boolean hayTerminos = false;
        for (Double v : valoresTerminos(request)) {
            if (v != null) {
                if (v < 0) {
                    response.error("Los valores no pueden ser negativos.");
                    return response;
                }
                hayTerminos = true;
            }
        }

        if (!hayTerminos && request.getFavorita() == null) {
            response.error("No hay cambios para guardar.");
            return response;
        }

        EntityUserTerms ut = null;
        if (hayTerminos) {
            ut = officialPlant
                    ? userTermsRepository.findByIdUserAndIdPlant(authenticatedUserId, plantId).orElse(null)
                    : userTermsRepository.findByIdUserAndIdUserPlant(authenticatedUserId, userPlantId)
                            .orElse(null);

            if (ut == null && defaultTerms == null) {
                response.error("La planta " + plantName + " no tiene términos por defecto.");
                return response;
            }
        }

        try {
            if (hayTerminos) {
                if (ut == null) {
                    // Primera vez: se parte de una copia de los términos de la planta
                    ut = desdeTerms(defaultTerms);
                    ut.setIdUserTerm(UUID.randomUUID().toString());
                    ut.setIdUser(authenticatedUserId);
                    ut.setIdPlant(plantId);
                    ut.setIdUserPlant(userPlantId);
                }
                aplicar(request, ut);
                ut.setUpdatedAt(LocalDateTime.now());
                userTermsRepository.save(ut);
            }

            if (request.getFavorita() != null) {
                if (request.getFavorita()) {
                    if (!userFavoriteRepository.existsByIdUserAndIdPlant(
                            authenticatedUserId, request.getIdPlant())) {
                        userFavoriteRepository.save(EntityUserFavorite.builder()
                                .idUserFavorite(UUID.randomUUID().toString())
                                .idUser(authenticatedUserId)
                                .idPlant(request.getIdPlant())
                                .createdAt(LocalDateTime.now())
                                .build());
                    }
                } else {
                    userFavoriteRepository.deleteByIdUserAndIdPlant(authenticatedUserId, request.getIdPlant());
                }
            }

            response.success("Cambios guardados correctamente para la planta " + plantName);
        } catch (Exception e) {
            response.exception("Error al guardar los cambios: " + e.getMessage());
        }
        return response;
    }

    public ResponseGeneric restablecer(String authenticatedUserId, RequestUserTerms request) {
        ResponseGeneric response = new ResponseGeneric() {};

        if (request == null || authenticatedUserId == null || !userRepository.existsById(authenticatedUserId)) {
            response.error("El usuario no existe.");
            return response;
        }

        boolean officialPlant = request.getIdPlant() != null && !request.getIdPlant().isBlank();
        boolean personalPlant = request.getIdUserPlant() != null && !request.getIdUserPlant().isBlank();
        if (officialPlant == personalPlant) {
            response.error("Debe seleccionar exactamente una planta para restablecer.");
            return response;
        }

        try {
            if (officialPlant) {
                if (!plantRepository.existsById(request.getIdPlant())) {
                    response.error("La planta seleccionada no existe.");
                    return response;
                }
                userTermsRepository.deleteByIdUserAndIdPlant(authenticatedUserId, request.getIdPlant());
            } else {
                if (!userPlantRepository.existsByIdUserPlantAndUser_IdUser(
                        request.getIdUserPlant(), authenticatedUserId)) {
                    response.error("La planta personal no existe o no pertenece al usuario.");
                    return response;
                }
                userTermsRepository.deleteByIdUserAndIdUserPlant(
                        authenticatedUserId, request.getIdUserPlant());
            }
            response.success("Términos restablecidos a los valores por defecto de la planta.");
        } catch (Exception e) {
            response.exception("Error al restablecer los términos: " + e.getMessage());
        }
        return response;
    }

    public EntityTerms obtenerTerminos(String idUser, EntityPlant plant) {
        return userTermsRepository.findByIdUserAndIdPlant(idUser, plant.getIdPlant())
                .map(this::aTerms)
                .orElse(plant.getTerms());
    }

    public EntityTerms obtenerTerminos(String idUser, EntityUserPlant plant) {
        return userTermsRepository.findByIdUserAndIdUserPlant(idUser, plant.getIdUserPlant())
                .map(this::aTerms)
                .orElse(plant.getTerms());
    }

    private List<Double> valoresTerminos(RequestUserTerms r) {
        return Arrays.asList(
                r.getFactorConcentrado(), r.getHumedad(), r.getMerma(),
                r.getLeyConcentradoCu(), r.getLeyConcentradoAg(), r.getLeyConcentradoAu(),
                r.getPagableCu(), r.getDeduccionCu(), r.getGastosRefCu(), r.getProteccionCu(),
                r.getPagableAg(), r.getDeduccionAg(), r.getGastosRefAg(),
                r.getPagableAu(), r.getDeduccionAu(), r.getGastosRefAu(),
                r.getMaquila(), r.getAnalisis(), r.getTrasladoInterno(),
                r.getIgvPorcentaje(), r.getDetraccionPorcentaje());
    }

    private void aplicar(RequestUserTerms r, EntityUserTerms u) {
        if (r.getFactorConcentrado() != null) u.setFactorConcentrado(r.getFactorConcentrado());
        if (r.getHumedad() != null) u.setHumedad(r.getHumedad());
        if (r.getMerma() != null) u.setMerma(r.getMerma());

        if (r.getLeyConcentradoCu() != null) u.setLeyConcentradoCu(r.getLeyConcentradoCu());
        if (r.getLeyConcentradoAg() != null) u.setLeyConcentradoAg(r.getLeyConcentradoAg());
        if (r.getLeyConcentradoAu() != null) u.setLeyConcentradoAu(r.getLeyConcentradoAu());

        if (r.getPagableCu() != null) u.setPagableCu(r.getPagableCu());
        if (r.getDeduccionCu() != null) u.setDeduccionCu(r.getDeduccionCu());
        if (r.getGastosRefCu() != null) u.setGastosRefCu(r.getGastosRefCu());
        if (r.getProteccionCu() != null) u.setProteccionCu(r.getProteccionCu());

        if (r.getPagableAg() != null) u.setPagableAg(r.getPagableAg());
        if (r.getDeduccionAg() != null) u.setDeduccionAg(r.getDeduccionAg());
        if (r.getGastosRefAg() != null) u.setGastosRefAg(r.getGastosRefAg());

        if (r.getPagableAu() != null) u.setPagableAu(r.getPagableAu());
        if (r.getDeduccionAu() != null) u.setDeduccionAu(r.getDeduccionAu());
        if (r.getGastosRefAu() != null) u.setGastosRefAu(r.getGastosRefAu());

        if (r.getMaquila() != null) u.setMaquila(r.getMaquila());
        if (r.getAnalisis() != null) u.setAnalisis(r.getAnalisis());
        if (r.getTrasladoInterno() != null) u.setTrasladoInterno(r.getTrasladoInterno());

        if (r.getIgvPorcentaje() != null) u.setIgvPorcentaje(r.getIgvPorcentaje());
        if (r.getDetraccionPorcentaje() != null) u.setDetraccionPorcentaje(r.getDetraccionPorcentaje());
    }

    private EntityUserTerms desdeTerms(EntityTerms t) {
        return EntityUserTerms.builder()
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
                .build();
    }

    private EntityTerms aTerms(EntityUserTerms u) {
        return EntityTerms.builder()
                .factorConcentrado(u.getFactorConcentrado())
                .humedad(u.getHumedad())
                .merma(u.getMerma())
                .leyConcentradoCu(u.getLeyConcentradoCu())
                .leyConcentradoAg(u.getLeyConcentradoAg())
                .leyConcentradoAu(u.getLeyConcentradoAu())
                .pagableCu(u.getPagableCu())
                .deduccionCu(u.getDeduccionCu())
                .gastosRefCu(u.getGastosRefCu())
                .proteccionCu(u.getProteccionCu())
                .pagableAg(u.getPagableAg())
                .deduccionAg(u.getDeduccionAg())
                .gastosRefAg(u.getGastosRefAg())
                .pagableAu(u.getPagableAu())
                .deduccionAu(u.getDeduccionAu())
                .gastosRefAu(u.getGastosRefAu())
                .maquila(u.getMaquila())
                .analisis(u.getAnalisis())
                .trasladoInterno(u.getTrasladoInterno())
                .igvPorcentaje(u.getIgvPorcentaje())
                .detraccionPorcentaje(u.getDetraccionPorcentaje())
                .build();
    }

    private void mapearRespuesta(
            String idPlant, String idUserPlant, String name, String location,
            EntityTerms t, EntityUserTerms ut, boolean favorita, ResponseUserTerms res) {
        res.setIdPlant(idPlant);
        res.setIdUserPlant(idUserPlant);
        res.setName(name);
        res.setLocation(location);
        res.setPersonalizado(ut != null);
        res.setFavorita(favorita);

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

        res.setUpdatedAt(ut != null ? ut.getUpdatedAt() : null);
    }
    
    public boolean tienePersonalizados(String idUser, EntityPlant plant) {
        return userTermsRepository.findByIdUserAndIdPlant(idUser, plant.getIdPlant()).isPresent();
    }

    public boolean tienePersonalizados(String idUser, EntityUserPlant plant) {
        return userTermsRepository.findByIdUserAndIdUserPlant(idUser, plant.getIdUserPlant()).isPresent();
    }
}