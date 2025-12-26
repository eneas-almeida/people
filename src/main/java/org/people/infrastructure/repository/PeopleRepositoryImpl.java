package org.people.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.people.application.dto.PeopleResponse;
import org.people.domain.client.PeopleClient;
import org.people.domain.enums.DataSource;
import org.people.domain.repository.PeopleRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RequiredArgsConstructor
public class PeopleRepositoryImpl implements PeopleRepository {
	private final Map<DataSource, PeopleClient> clientStrategies;
	private final DataSource activeDataSource;

	private PeopleClient getActiveClient() {
		return clientStrategies.get(activeDataSource);
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