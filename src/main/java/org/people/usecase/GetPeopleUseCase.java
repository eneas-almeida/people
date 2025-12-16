package org.people.usecase;

import org.people.domain.client.PeopleClient;
import org.people.domain.entity.People;
import reactor.core.publisher.Mono;

public class GetPeopleUseCase {
	private final PeopleClient peopleClient;

	public GetPeopleUseCase(PeopleClient peopleClient) {
		this.peopleClient = peopleClient;
	}

	public Mono<People> execute(Integer peopleId) {
		return peopleClient.findById(peopleId);
	}
}