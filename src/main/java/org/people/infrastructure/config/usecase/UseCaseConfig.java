package org.people.infrastructure.config.usecase;

import org.people.domain.client.PeopleClient;
import org.people.domain.enums.DataSource;
import org.people.domain.repository.PeopleRepository;
import org.people.infrastructure.client.dummy.DummyClientImpl;
import org.people.infrastructure.client.typicode.TypiCodeClientImpl;
import org.people.infrastructure.repository.PeopleRepositoryImpl;
import org.people.application.usecase.GetPeopleUseCaseImpl;
import org.people.application.usecase.ListPeopleUseCaseImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class UseCaseConfig {

	@Bean
	public PeopleRepository peopleRepository(
			TypiCodeClientImpl typiCodeClient,
			DummyClientImpl dummyClient,
			@Value("${client.active-datasource:TYPICODE}")
			String activeDataSourceStr) {

		Map<DataSource, PeopleClient> clientStrategies = new HashMap<>();
		clientStrategies.put(DataSource.TYPICODE, typiCodeClient);
		clientStrategies.put(DataSource.DUMMY, dummyClient);

		DataSource activeDataSource = DataSource.valueOf(activeDataSourceStr.toUpperCase());

		return new PeopleRepositoryImpl(clientStrategies, activeDataSource);
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