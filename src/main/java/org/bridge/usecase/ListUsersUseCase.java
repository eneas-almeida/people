package org.bridge.usecase;

import org.bridge.domain.client.UserClient;
import org.bridge.domain.entity.User;
import reactor.core.publisher.Flux;

public class ListUsersUseCase {
	private final UserClient userClient;

	public ListUsersUseCase(UserClient userClient) {
		this.userClient = userClient;
	}

	public Flux<User> execute() {
		return userClient.listAll();
	}
}