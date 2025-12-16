package org.bridge.infrastructure.config;

import org.bridge.domain.client.UserClient;
import org.bridge.usecase.GetUserUseCase;
import org.bridge.usecase.ListUsersUseCase;
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