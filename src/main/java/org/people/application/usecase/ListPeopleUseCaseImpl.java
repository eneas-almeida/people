package org.people.application.usecase;

import org.people.application.dto.PeopleResponse;
import org.people.domain.repository.PeopleRepository;
import reactor.core.publisher.Flux;

public class ListPeopleUseCaseImpl {
	private final PeopleRepository peopleRepository;

	public ListPeopleUseCaseImpl(PeopleRepository peopleRepository) {
		this.peopleRepository = peopleRepository;
	}

	public Flux<PeopleResponse> execute() {
		return peopleRepository.findAll();
	}
}