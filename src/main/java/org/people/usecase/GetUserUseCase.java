package org.people.usecase;

import org.people.domain.client.UserClient;
import org.people.domain.entity.User;
import reactor.core.publisher.Mono;

public class GetUserUseCase {
	private final UserClient userClient;

	public GetUserUseCase(UserClient userClient) {
		this.userClient = userClient;
	}

	public Mono<User> execute(Integer userId) {
		return userClient.findById(userId);
	}
}