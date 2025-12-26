package org.people.application.usecase;

import lombok.RequiredArgsConstructor;
import org.people.application.dto.PeopleResponse;
import org.people.domain.repository.PeopleRepository;
import org.people.infrastructure.logging.LogContext;
import org.people.infrastructure.logging.Logger;
import org.people.infrastructure.logging.RequestContext;
import reactor.core.publisher.Mono;

import java.util.Map;

@RequiredArgsConstructor
public class GetPeopleUseCaseImpl {
	private static final Logger logger = Logger.getLogger(GetPeopleUseCaseImpl.class);
	private final PeopleRepository peopleRepository;

	public Mono<PeopleResponse> execute(Integer peopleId) {
		String requestId = RequestContext.getRequestId();
		LogContext.add("people_id", String.valueOf(peopleId));
		LogContext.add("operation", "get_people");

		logger.info("Executing GetPeopleUseCaseImpl - peopleId: {}, requestId: {}", peopleId, requestId);

		return peopleRepository.findById(peopleId)
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