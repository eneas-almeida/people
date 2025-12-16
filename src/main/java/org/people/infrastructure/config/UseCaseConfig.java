package org.people.infrastructure.config;

import org.people.domain.client.UserClient;
import org.people.usecase.GetUserUseCase;
import org.people.usecase.ListUsersUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

	@Bean
	public GetUserUseCase getUserUseCase(UserClient userClient) {
		return new GetUserUseCase(userClient);
	}

	@Bean
	public ListUsersUseCase listUsersUseCase(UserClient userClient) {
		return new ListUsersUseCase(userClient);
	}
}