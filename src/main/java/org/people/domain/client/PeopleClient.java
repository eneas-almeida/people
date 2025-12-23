package org.people.domain.client;

import org.people.application.dto.PeopleResponse;
import org.people.domain.entity.People;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PeopleClient {
	Mono<PeopleResponse> findById(Integer id);
	Flux<PeopleResponse> listAll();
}