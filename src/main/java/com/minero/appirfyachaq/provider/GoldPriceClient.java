package com.minero.appirfyachaq.provider;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.minero.appirfyachaq.entity.EntityMarketPrice;
import com.minero.appirfyachaq.repository.RepositoryMarketPrice;

@Component
@EnableScheduling
public class GoldPriceClient {

	private static final String URL = "https://api.goldprice.dev/v1/prices";

	private final RestClient restClient = RestClient.create();
	private final RepositoryMarketPrice marketPriceRepository;

	public GoldPriceClient(RepositoryMarketPrice marketPriceRepository) {
		this.marketPriceRepository = marketPriceRepository;
	}

	@Scheduled(cron = "0 0 0,12 * * *", zone = "America/Lima")
	public void actualizarPrecios() {
		try {
			Map<String, List<Map<String, Object>>> body = restClient.get()
					.uri(URL)
					.retrieve()
					.body(new ParameterizedTypeReference<Map<String, List<Map<String, Object>>>>() {});

			if (body == null || body.get("symbols") == null) {
				return;
			}

			for (Map<String, Object> item : body.get("symbols")) {
				if (!"USD".equals(item.get("quote_currency"))) {
					continue;
				}

				String metal = switch (String.valueOf(item.get("symbol"))) {
					case "HG" -> "CU";
					case "XAG" -> "AG";
					case "XAU" -> "AU";
					default -> null;
				};
				if (metal == null) {
					continue;
				}

				BigDecimal precio = new BigDecimal(String.valueOf(item.get("price")));
				if (precio.signum() <= 0) {
					continue;
				}

				EntityMarketPrice entity = marketPriceRepository.findByMetal(metal)
						.orElseGet(() -> EntityMarketPrice.builder()
								.idMarketPrice(UUID.randomUUID().toString())
								.metal(metal)
								.build());

				entity.setPrice(precio);
				entity.setUpdatedAt(LocalDateTime.now());
				marketPriceRepository.save(entity);
			}
		} catch (Exception e) {
			// Si la API falla, se conservan los últimos precios guardados.
		}
	}
}