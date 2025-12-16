package org.people.usecase;

import org.people.domain.client.PeopleClient;
import org.people.domain.entity.People;
import reactor.core.publisher.Flux;

public class ListPeopleUseCase {
	private final PeopleClient peopleClient;

	public ListPeopleUseCase(PeopleClient peopleClient) {
		this.peopleClient = peopleClient;
	}

	public Flux<People> execute() {
		return peopleClient.listAll();
	}
}