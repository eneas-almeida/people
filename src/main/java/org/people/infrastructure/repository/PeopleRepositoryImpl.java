package org.people.infrastructure.repository;

import org.people.application.dto.PeopleResponse;
import org.people.domain.client.PeopleClient;
import org.people.domain.enums.DataSource;
import org.people.domain.repository.PeopleRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public class PeopleRepositoryImpl implements PeopleRepository {
	private final Map<DataSource, PeopleClient> clientStrategies;
	private final DataSource activeDataSource;

	public PeopleRepositoryImpl(Map<DataSource, PeopleClient> clientStrategies,
								DataSource activeDataSource) {
		this.clientStrategies = clientStrategies;
		this.activeDataSource = activeDataSource;
	}

	private PeopleClient getActiveClient() {
		PeopleClient client = clientStrategies.get(activeDataSource);
		if (client == null) {
			throw new IllegalStateException("No client configured for data source: " + activeDataSource);
		}
		return client;
	}

	@Override
	public Mono<PeopleResponse> findById(Integer id) {
		return getActiveClient().findById(id);
	}

	@Override
	public Flux<PeopleResponse> findAll() {
		return getActiveClient().listAll();
	}
}