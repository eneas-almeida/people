package org.people.domain.repository;

import org.people.application.dto.PeopleResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PeopleRepository {
	Mono<PeopleResponse> findById(Integer id);
	Flux<PeopleResponse> findAll();
}