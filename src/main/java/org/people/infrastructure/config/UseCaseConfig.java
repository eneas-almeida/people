package org.people.infrastructure.config;

import org.people.domain.client.PeopleClient;
import org.people.domain.repository.PeopleRepository;
import org.people.infrastructure.repository.PeopleRepositoryImpl;
import org.people.usecase.GetPeopleUseCase;
import org.people.usecase.ListPeopleUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

	@Bean
	public PeopleRepository peopleRepository(PeopleClient peopleClient) {
		return new PeopleRepositoryImpl(peopleClient);
	}

	@Bean
	public GetPeopleUseCase getPeopleUseCase(PeopleRepository peopleRepository) {
		return new GetPeopleUseCase(peopleRepository);
	}

	@Bean
	public ListPeopleUseCase listPeopleUseCase(PeopleRepository peopleRepository) {
		return new ListPeopleUseCase(peopleRepository);
	}
}