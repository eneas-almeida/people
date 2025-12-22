package org.people.infrastructure.config;

import org.people.domain.client.PeopleClient;
import org.people.domain.repository.PeopleRepository;
import org.people.infrastructure.repository.PeopleRepositoryImpl;
import org.people.usecase.GetPeopleUseCaseImpl;
import org.people.usecase.ListPeopleUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

	@Bean
	public PeopleRepository peopleRepository(PeopleClient peopleClient) {
		return new PeopleRepositoryImpl(peopleClient);
	}

	@Bean
	public GetPeopleUseCaseImpl getPeopleUseCase(PeopleRepository peopleRepository) {
		return new GetPeopleUseCaseImpl(peopleRepository);
	}

	@Bean
	public ListPeopleUseCaseImpl listPeopleUseCase(PeopleRepository peopleRepository) {
		return new ListPeopleUseCaseImpl(peopleRepository);
	}
}