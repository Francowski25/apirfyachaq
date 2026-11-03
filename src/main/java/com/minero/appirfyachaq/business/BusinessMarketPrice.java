package com.minero.appirfyachaq.business;

import com.minero.appirfyachaq.dto.request.RequestMarketPrice;
import com.minero.appirfyachaq.dto.response.ResponseMarketPrice;
import com.minero.appirfyachaq.entity.EntityMarketPrice;
import com.minero.appirfyachaq.repository.RepositoryMarketPrice;
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
public class BusinessMarketPrice {

    private final RepositoryMarketPrice marketPriceRepository;

    public BusinessMarketPrice(RepositoryMarketPrice marketPriceRepository) {
        this.marketPriceRepository = marketPriceRepository;
    }

    public List<ResponseMarketPrice> listar() {
        List<EntityMarketPrice> precios = marketPriceRepository.findAll();
        List<ResponseMarketPrice> responses = new ArrayList<>();

        if (precios.isEmpty()) {
            ResponseMarketPrice res = new ResponseMarketPrice();
            res.error("No hay precios de metales registrados.");
            responses.add(res);
            return responses;
        }

        for (EntityMarketPrice p : precios) {
            ResponseMarketPrice res = new ResponseMarketPrice();
            res.success("OK");
            mapearRespuesta(p, res);
            responses.add(res);
        }
        return responses;
    }


    public ResponseMarketPrice actualizar(RequestMarketPrice request) {
        ResponseMarketPrice response = new ResponseMarketPrice();

        if (request == null || request.getMetal() == null || request.getMetal().trim().isEmpty()) {
            response.error("El metal es obligatorio.");
            return response;
        }

        if (request.getPrice() == null) {
            response.error("El precio es obligatorio.");
            return response;
        }

        if (request.getPrice().signum() <= 0) {
            response.error("El precio debe ser mayor a cero.");
            return response;
        }

        try {
            String metal = normalizar(request.getMetal());

            EntityMarketPrice entity = marketPriceRepository.findByMetal(metal)
                    .orElseGet(() -> EntityMarketPrice.builder()
                            .idMarketPrice(UUID.randomUUID().toString())
                            .metal(metal)
                            .build());

            boolean esNuevo = entity.getUpdatedAt() == null;

            entity.setPrice(request.getPrice());
            entity.setUpdatedAt(LocalDateTime.now());

            marketPriceRepository.save(entity);

            if (esNuevo) {
                response.success("Precio del metal registrado correctamente.");
            } else {
                response.success("Precio del metal actualizado correctamente.");
            }
            mapearRespuesta(entity, response);
        } catch (Exception e) {
            response.exception("Error al actualizar precio: " + e.getMessage());
        }
        return response;
    }

    private String normalizar(String metal) {
        return metal == null ? null : metal.trim().toUpperCase();
    }

    private void mapearRespuesta(EntityMarketPrice p, ResponseMarketPrice res) {
        res.setIdMarketPrice(p.getIdMarketPrice());
        res.setMetal(p.getMetal());
        res.setPrice(p.getPrice());
        res.setUpdatedAt(p.getUpdatedAt());
    }
    
    public Optional<BigDecimal> obtenerPrecio(String metal) {
        return marketPriceRepository.findByMetal(normalizar(metal))
                .map(EntityMarketPrice::getPrice);
    }
}