package org.people.domain.client;

import org.people.domain.entity.People;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PeopleClient {
	Mono<People> findById(Integer id);
	Flux<People> listAll();
}