package org.people.usecase;

import org.people.domain.client.UserClient;
import org.people.domain.entity.User;
import reactor.core.publisher.Flux;

public class ListUsersUseCase {
	private final UserClient userClient;

	public ListUsersUseCase(UserClient userClient) {
		this.userClient = userClient;
	}

	public Flux<User> execute() {
		return userClient.listAll().flatMap(users -> {
			if (users != null) {
				return Flux.just(users);
			} else {
				return Flux.empty();
			}
		});
	}
}