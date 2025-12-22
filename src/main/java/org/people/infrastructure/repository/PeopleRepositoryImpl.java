package org.people.infrastructure.repository;

import org.people.domain.client.PeopleClient;
import org.people.domain.entity.People;
import org.people.domain.repository.PeopleRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PeopleRepositoryImpl implements PeopleRepository {
	private final PeopleClient peopleClient;

	public PeopleRepositoryImpl(PeopleClient peopleClient) {
		this.peopleClient = peopleClient;
	}

	@Override
	public Mono<People> findById(Integer id) {
		return peopleClient.findById(id);
	}

	@Override
	public Flux<People> findAll() {
		return peopleClient.listAll();
	}
}