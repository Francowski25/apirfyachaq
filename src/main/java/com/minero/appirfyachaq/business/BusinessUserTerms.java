package com.minero.appirfyachaq.business;

import com.minero.appirfyachaq.dto.request.RequestUserTerms;
import com.minero.appirfyachaq.dto.response.ResponseUserTerms;
import com.minero.appirfyachaq.entity.EntityPlant;
import com.minero.appirfyachaq.entity.EntityTerms;
import com.minero.appirfyachaq.entity.EntityUserFavorite;
import com.minero.appirfyachaq.entity.EntityUserTerms;
import com.minero.appirfyachaq.generic.ResponseGeneric;
import com.minero.appirfyachaq.repository.RepositoryPlant;
import com.minero.appirfyachaq.repository.RepositoryUser;
import com.minero.appirfyachaq.repository.RepositoryUserFavorite;
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

    public BusinessUserTerms(RepositoryUserTerms userTermsRepository,
                             RepositoryPlant plantRepository,
                             RepositoryUser userRepository,
                             RepositoryUserFavorite userFavoriteRepository) {
        this.userTermsRepository = userTermsRepository;
        this.plantRepository = plantRepository;
        this.userRepository = userRepository;
        this.userFavoriteRepository = userFavoriteRepository;
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

        Map<String, EntityUserTerms> personalizados = new HashMap<>();
        for (EntityUserTerms ut : userTermsRepository.findByIdUser(idUser)) {
            personalizados.put(ut.getIdPlant(), ut);
        }

        Set<String> favoritas = new HashSet<>();
        for (EntityUserFavorite f : userFavoriteRepository.findByIdUser(idUser)) {
            favoritas.add(f.getIdPlant());
        }

        for (EntityPlant p : plantas) {
            EntityUserTerms ut = personalizados.get(p.getIdPlant());
            EntityTerms t = (ut != null) ? aTerms(ut) : p.getTerms();

            if (t == null) {
                continue;
            }

            ResponseUserTerms res = new ResponseUserTerms();
            res.success("OK");
            mapearRespuesta(p, t, ut, favoritas.contains(p.getIdPlant()), res);
            responses.add(res);
        }

        if (responses.isEmpty()) {
            ResponseUserTerms res = new ResponseUserTerms();
            res.error("No hay plantas con términos registrados.");
            responses.add(res);
        }
        return responses;
    }

    public ResponseGeneric guardar(RequestUserTerms request) {
        ResponseGeneric response = new ResponseGeneric() {};

        if (request == null || request.getIdUser() == null || !userRepository.existsById(request.getIdUser())) {
            response.error("El usuario no existe.");
            return response;
        }

        if (request.getIdPlant() == null || request.getIdPlant().isEmpty()) {
            response.error("Debe seleccionar una planta.");
            return response;
        }

        EntityPlant p = plantRepository.findById(request.getIdPlant()).orElse(null);
        if (p == null) {
            response.error("La planta seleccionada no existe.");
            return response;
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
            ut = userTermsRepository
                    .findByIdUserAndIdPlant(request.getIdUser(), request.getIdPlant())
                    .orElse(null);

            if (ut == null && p.getTerms() == null) {
                response.error("La planta " + p.getName() + " no tiene términos por defecto.");
                return response;
            }
        }

        try {
            if (hayTerminos) {
                if (ut == null) {
                    // Primera vez: se parte de una copia de los términos de la planta
                    ut = desdeTerms(p.getTerms());
                    ut.setIdUserTerm(UUID.randomUUID().toString());
                    ut.setIdUser(request.getIdUser());
                    ut.setIdPlant(p.getIdPlant());
                }
                aplicar(request, ut);
                ut.setUpdatedAt(LocalDateTime.now());
                userTermsRepository.save(ut);
            }

            if (request.getFavorita() != null) {
                if (request.getFavorita()) {
                    if (!userFavoriteRepository.existsByIdUserAndIdPlant(request.getIdUser(), request.getIdPlant())) {
                        userFavoriteRepository.save(EntityUserFavorite.builder()
                                .idUserFavorite(UUID.randomUUID().toString())
                                .idUser(request.getIdUser())
                                .idPlant(request.getIdPlant())
                                .createdAt(LocalDateTime.now())
                                .build());
                    }
                } else {
                    userFavoriteRepository.deleteByIdUserAndIdPlant(request.getIdUser(), request.getIdPlant());
                }
            }

            response.success("Cambios guardados correctamente para la planta " + p.getName());
        } catch (Exception e) {
            response.exception("Error al guardar los cambios: " + e.getMessage());
        }
        return response;
    }

    public ResponseGeneric restablecer(RequestUserTerms request) {
        ResponseGeneric response = new ResponseGeneric() {};

        if (request == null || request.getIdUser() == null || !userRepository.existsById(request.getIdUser())) {
            response.error("El usuario no existe.");
            return response;
        }

        if (request.getIdPlant() == null || request.getIdPlant().isEmpty()) {
            response.error("Debe seleccionar una planta para restablecer.");
            return response;
        }

        try {
            userTermsRepository.deleteByIdUserAndIdPlant(request.getIdUser(), request.getIdPlant());
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

    private void mapearRespuesta(EntityPlant p, EntityTerms t, EntityUserTerms ut, boolean favorita, ResponseUserTerms res) {
        res.setIdPlant(p.getIdPlant());
        res.setName(p.getName());
        res.setLocation(p.getLocation());
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
}