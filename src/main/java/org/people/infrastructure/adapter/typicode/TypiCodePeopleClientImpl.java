package org.people.infrastructure.adapter.typicode;

import org.people.domain.client.PeopleClient;
import org.people.domain.entity.People;
import org.people.domain.exception.PeopleNotFoundException;
import org.people.infrastructure.exception.ExternalServiceException;
import org.people.infrastructure.logging.LogContext;
import org.people.infrastructure.logging.Logger;
import org.people.infrastructure.logging.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

@Component
public class TypiCodePeopleClientImpl implements PeopleClient {

	private static final Logger logger = Logger.getLogger(TypiCodePeopleClientImpl.class);

	@Autowired
	private WebClient typiCodeWebClient;

	@Autowired
	private PeopleMapper peopleMapper;

	@Override
	public Mono<People> findById(Integer id) {
		String requestId = RequestContext.getRequestId();
		LogContext.add("people_id", String.valueOf(id));
		LogContext.add("operation", "findById");

		logger.info("Fetching people by id from external API - id: {}, requestId: {}", id, requestId);

		return typiCodeWebClient
				.get()
				.uri("/users/{id}", id)
				.retrieve()
				.onStatus(status -> status == HttpStatus.NOT_FOUND, response -> handleNotFound(id, response))
				.onStatus(status -> status.is4xxClientError(), response -> handleClientError(id, response))
				.onStatus(status -> status.is5xxServerError(), response -> handleServerError(id, response))
				.bodyToMono(PeopleResponse.class)
				.switchIfEmpty(Mono.defer(() -> {
					logger.warn("Empty response from external API for people id: {}", id);
					return Mono.error(new PeopleNotFoundException(id));
				}))
				.map(response -> {
					if (response == null || response.id() == null) {
						logger.warn("Invalid or null response from external API for people id: {}", id);
						throw new PeopleNotFoundException(id);
					}

					People people = peopleMapper.toPeople(response);

					if (people == null) {
						logger.warn("Failed to map response to People entity for id: {}", id);
						throw new PeopleNotFoundException(id);
					}

					logger.info("People fetched successfully from external API", Map.of(
							"people_id", String.valueOf(id),
							"people_name", people.getName(),
							"request_id", requestId
					));
					return people;
				})
				.retryWhen(Retry.backoff(2, Duration.ofMillis(100))
						.filter(this::isRetryableException)
						.doBeforeRetry(retrySignal -> {
							logger.warn("Retrying request - attempt: {}, error: {}",
									retrySignal.totalRetries() + 1,
									retrySignal.failure().getMessage());
						}))
				.doOnError(error -> {
					LogContext.setError(error.getClass().getSimpleName());
					LogContext.setErrorMessage(error.getMessage());
					logger.error("Error fetching people from external API - id: {}", id, error);
				})
				.doFinally(signalType -> LogContext.clear());
	}

	@Override
	public Flux<People> listAll() {
		String requestId = RequestContext.getRequestId();
		LogContext.add("operation", "listAll");

		logger.info("Fetching all people from external API - requestId: {}", requestId);

		return typiCodeWebClient
				.get()
				.uri("/users")
				.retrieve()
				.onStatus(status -> status.is4xxClientError(), this::handleClientErrorList)
				.onStatus(status -> status.is5xxServerError(), this::handleServerErrorList)
				.bodyToFlux(PeopleResponse.class)
				.map(peopleMapper::toPeople)
				.doOnComplete(() -> logger.info("Successfully fetched all people from external API"))
				.retryWhen(Retry.backoff(2, Duration.ofMillis(100))
						.filter(this::isRetryableException)
						.doBeforeRetry(retrySignal -> {
							logger.warn("Retrying list request - attempt: {}, error: {}",
									retrySignal.totalRetries() + 1,
									retrySignal.failure().getMessage());
						}))
				.doOnError(error -> {
					LogContext.setError(error.getClass().getSimpleName());
					LogContext.setErrorMessage(error.getMessage());
					logger.error("Error fetching people list from external API", error);
				})
				.doFinally(signalType -> LogContext.clear());
	}

	private Mono<Throwable> handleNotFound(Integer id, ClientResponse response) {
		return response.bodyToMono(String.class)
				.defaultIfEmpty("No body")
				.flatMap(body -> {
					String errorMsg = String.format("People not found - id: %d, status: %s",
							id, response.statusCode());
					logger.warn(errorMsg);
					return Mono.error(new PeopleNotFoundException(errorMsg));
				});
	}

	private Mono<Throwable> handleClientError(Integer id, ClientResponse response) {
		return response.bodyToMono(String.class)
				.defaultIfEmpty("No body")
				.flatMap(body -> {
					String errorMsg = String.format("Client error fetching people - id: %d, status: %s, body: %s",
							id, response.statusCode(), body);
					logger.error(errorMsg);
					return Mono.error(new ExternalServiceException(errorMsg, "TypiCode API", response.statusCode().value()));
				});
	}

	private Mono<Throwable> handleServerError(Integer id, ClientResponse response) {
		return response.bodyToMono(String.class)
				.defaultIfEmpty("No body")
				.flatMap(body -> {
					String errorMsg = String.format("Server error fetching people - id: %d, status: %s, body: %s",
							id, response.statusCode(), body);
					logger.error(errorMsg);
					return Mono.error(new ExternalServiceException(errorMsg, "TypiCode API", response.statusCode().value()));
				});
	}

	private Mono<Throwable> handleClientErrorList(ClientResponse response) {
		return response.bodyToMono(String.class)
				.defaultIfEmpty("No body")
				.flatMap(body -> {
					String errorMsg = String.format("Client error fetching people list - status: %s, body: %s",
							response.statusCode(), body);
					logger.error(errorMsg);
					return Mono.error(new ExternalServiceException(errorMsg, "TypiCode API", response.statusCode().value()));
				});
	}

	private Mono<Throwable> handleServerErrorList(ClientResponse response) {
		return response.bodyToMono(String.class)
				.defaultIfEmpty("No body")
				.flatMap(body -> {
					String errorMsg = String.format("Server error fetching people list - status: %s, body: %s",
							response.statusCode(), body);
					logger.error(errorMsg);
					return Mono.error(new ExternalServiceException(errorMsg, "TypiCode API", response.statusCode().value()));
				});
	}

	private boolean isRetryableException(Throwable throwable) {
		// Não fazer retry para PeopleNotFoundException (404 - recurso não existe)
		if (throwable instanceof PeopleNotFoundException) {
			return false;
		}

		// Retry apenas para erros de servidor (5xx)
		return throwable instanceof ExternalServiceException ||
				(throwable instanceof WebClientResponseException &&
						((WebClientResponseException) throwable).getStatusCode().is5xxServerError());
	}
}