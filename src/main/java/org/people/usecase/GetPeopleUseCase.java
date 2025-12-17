package org.people.usecase;

import org.people.domain.client.PeopleClient;
import org.people.domain.entity.People;
import org.people.infrastructure.logging.LogContext;
import org.people.infrastructure.logging.Logger;
import org.people.infrastructure.logging.RequestContext;
import reactor.core.publisher.Mono;

import java.util.Map;

public class GetPeopleUseCase {
	private static final Logger logger = Logger.getLogger(GetPeopleUseCase.class);
	private final PeopleClient peopleClient;

	public GetPeopleUseCase(PeopleClient peopleClient) {
		this.peopleClient = peopleClient;
	}

	public Mono<People> execute(Integer peopleId) {
		String requestId = RequestContext.getRequestId();
		LogContext.add("people_id", String.valueOf(peopleId));
		LogContext.add("operation", "get_people");

		logger.info("Executing GetPeopleUseCase - peopleId: {}, requestId: {}", peopleId, requestId);

		return peopleClient.findById(peopleId)
				.doOnSuccess(people -> {
					if (people != null) {
						logger.info("People found successfully", Map.of(
								"people_id", String.valueOf(peopleId),
								"people_name", people.getName(),
								"request_id", requestId
						));
					} else {
						logger.warn("People not found for id: {}", peopleId);
					}
				})
				.doOnError(error -> {
					LogContext.setError(error.getClass().getSimpleName());
					LogContext.setErrorMessage(error.getMessage());
					logger.error("Error fetching people with id: {}", peopleId, error);
				})
				.doFinally(signalType -> LogContext.clear());
	}
}