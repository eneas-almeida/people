package org.people.domain.repository;

import org.people.domain.entity.People;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PeopleRepository {
	Mono<People> findById(Integer id);
	Flux<People> findAll();
}