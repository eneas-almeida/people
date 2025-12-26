package org.people.infrastructure.client.dummy;

import org.people.application.dto.PeopleResponse;
import org.people.domain.client.PeopleClient;
import org.people.infrastructure.exception.ExternalServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class DummyClientImpl implements PeopleClient {

	@Autowired
	@Qualifier("dummyWebClient")
	private WebClient dummyWebClient;

	@Autowired
	private DummyMapper dummyMapper;

	@Override
	public Mono<PeopleResponse> findById(Integer id) {
		return dummyWebClient
				.get()
				.uri("/users/{id}", id)
				.header("Accept", "application/json")
				.retrieve()
				.bodyToMono(DummyResponse.class)
				.map(response -> {
						PeopleResponse peopleResponse = dummyMapper.toPeopleResponse(response);
						return peopleResponse;
				})
				.onErrorMap(e -> {
					String errorMessage = "Error fetching people from Dummy: " + e.getMessage();
					return new ExternalServiceException(errorMessage, "Dummy API", e);
				});
	}

	@Override
	public Flux<PeopleResponse> listAll() {
		return dummyWebClient
				.get()
				.uri("/users")
				.header("Accept", "application/json")
				.retrieve()
				.bodyToMono(DummyListResponse.class)
				.flatMapMany(response -> {
					List<DummyResponse> users = response.users();

					return Flux.fromIterable(users)
							.map(user -> {
								PeopleResponse peopleResponse = dummyMapper.toPeopleResponse(user);
								return peopleResponse;
							});
				})
				.onErrorMap(e -> {
					String errorMessage = "Error fetching people list from Dummy: " + e.getMessage();
					return new ExternalServiceException(errorMessage, "Dummy API", e);
				});
	}
}
