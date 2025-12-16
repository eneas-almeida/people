package org.people.infrastructure.adapter.typicode;

import org.people.domain.client.PeopleClient;
import org.people.domain.entity.People;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class TypiCodePeopleClientImpl implements PeopleClient {

	@Autowired
	private WebClient typiCodeWebClient;

	@Autowired
	private PeopleMapper peopleMapper;

	@Override
	public Mono<People> findById(Integer id) {
		return typiCodeWebClient
				.get()
				.uri("/users/{id}", id)
				.retrieve()
				.bodyToMono(PeopleResponse.class)
				.map(peopleMapper::toPeople);
	}

	@Override
	public Flux<People> listAll() {

		return typiCodeWebClient
				.get()
				.uri("/users")
				.retrieve()
				.bodyToFlux(PeopleResponse.class)
				.map(peopleMapper::toPeople);
	}
}