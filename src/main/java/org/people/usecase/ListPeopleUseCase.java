package org.people.usecase;

import org.people.domain.entity.People;
import org.people.domain.repository.PeopleRepository;
import reactor.core.publisher.Flux;

public class ListPeopleUseCase {
	private final PeopleRepository peopleRepository;

	public ListPeopleUseCase(PeopleRepository peopleRepository) {
		this.peopleRepository = peopleRepository;
	}

	public Flux<People> execute() {
		return peopleRepository.findAll();
	}
}