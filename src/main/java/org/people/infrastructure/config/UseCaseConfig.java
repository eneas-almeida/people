package org.people.infrastructure.config;

import org.people.domain.client.PeopleClient;
import org.people.usecase.GetPeopleUseCase;
import org.people.usecase.ListPeopleUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

	@Bean
	public GetPeopleUseCase getPeopleUseCase(PeopleClient peopleClient) {
		return new GetPeopleUseCase(peopleClient);
	}

	@Bean
	public ListPeopleUseCase listPeopleUseCase(PeopleClient peopleClient) {
		return new ListPeopleUseCase(peopleClient);
	}
}