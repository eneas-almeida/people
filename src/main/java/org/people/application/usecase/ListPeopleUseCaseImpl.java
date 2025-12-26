package org.people.application.usecase;

import lombok.RequiredArgsConstructor;
import org.people.application.dto.PeopleResponse;
import org.people.domain.repository.PeopleRepository;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public class ListPeopleUseCaseImpl {
	private final PeopleRepository peopleRepository;

	public Flux<PeopleResponse> execute() {
		return peopleRepository.findAll();
	}
}