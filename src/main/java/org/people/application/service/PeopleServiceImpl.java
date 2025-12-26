package org.people.application.service;

import lombok.RequiredArgsConstructor;
import org.people.application.dto.PeopleResponse;
import org.people.domain.repository.PeopleRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PeopleServiceImpl implements PeopleService {

	private final PeopleRepository peopleRepository;

	@Override
	public Mono<PeopleResponse> getById(Integer id) {
		return peopleRepository.findById(id);
	}

	@Override
	public Flux<PeopleResponse> listAll() {
		return peopleRepository.findAll();
	}
}
