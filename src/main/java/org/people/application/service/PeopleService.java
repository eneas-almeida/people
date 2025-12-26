package org.people.application.service;

import org.people.application.dto.PeopleResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PeopleService {
	Mono<PeopleResponse> getById(Integer id);
	Flux<PeopleResponse> listAll();
}
