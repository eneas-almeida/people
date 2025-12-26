package org.people.infrastructure.config.client;

import org.people.infrastructure.logging.LogContext;
import org.people.infrastructure.logging.Logger;
import org.people.infrastructure.logging.RequestContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class TypiCodeClientConfig {
	private static final Logger logger = Logger.getLogger(TypiCodeClientConfig.class);
	private static final String REQUEST_ID_HEADER = "X-Request-ID";
	private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

	@Bean
	@Qualifier("typiCodeWebClient")
	public WebClient typiCodeWebClient(WebClient.Builder builder,
								@Value("${client.typicode.base-url}") String baseUrl) {
		ExchangeStrategies strategies = ExchangeStrategies.builder()
				.codecs(c -> c.defaultCodecs()
						.maxInMemorySize(5242880))
				.build();

		return builder
				.baseUrl(baseUrl)
				.exchangeStrategies(strategies)
				.filter(addRequestIdHeader())
				.filter(logRequest())
				.filter(logResponse())
				.build();
	}

	private ExchangeFilterFunction addRequestIdHeader() {
		return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
			String requestId = RequestContext.getRequestId();
			String correlationId = LogContext.get("correlation_id");

			ClientRequest newRequest = ClientRequest.from(clientRequest)
					.header(REQUEST_ID_HEADER, requestId)
					.build();

			if (correlationId != null) {
				newRequest = ClientRequest.from(newRequest)
						.header(CORRELATION_ID_HEADER, correlationId)
						.build();
			}

			return Mono.just(newRequest);
		});
	}

	private ExchangeFilterFunction logRequest() {
		return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
			long startTime = System.currentTimeMillis();
			LogContext.add("http.method", clientRequest.method().name());
			LogContext.add("http.url", clientRequest.url().toString());
			LogContext.add("http.start_time", String.valueOf(startTime));

			logger.debug("HTTP Request - method: {}, url: {}",
					clientRequest.method(),
					clientRequest.url());

			return Mono.just(clientRequest);
		});
	}

	private ExchangeFilterFunction logResponse() {
		return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
			String startTimeStr = LogContext.get("http.start_time");
			long duration = 0;

			if (startTimeStr != null) {
				long startTime = Long.parseLong(startTimeStr);
				duration = System.currentTimeMillis() - startTime;
			}

			LogContext.setDuration(duration);
			LogContext.setStatusCode(String.valueOf(clientResponse.statusCode().value()));

			if (clientResponse.statusCode().isError()) {
				logger.warn("HTTP Response - status: {}, duration: {}ms",
						clientResponse.statusCode(),
						duration);
			} else {
				logger.debug("HTTP Response - status: {}, duration: {}ms",
						clientResponse.statusCode(),
						duration);
			}

			return Mono.just(clientResponse);
		});
	}
}
