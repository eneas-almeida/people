package org.people.infrastructure.adapter.typicode;

import org.people.domain.client.UserClient;
import org.people.domain.entity.User;
import org.people.infrastructure.properties.TypicodeClientProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class TypicodeClientAdapter implements UserClient {
	private final WebClient webClient;
	private final UserMapper userMapper;

	public TypicodeClientAdapter(
			WebClient.Builder webClientBuilder,
			UserMapper userMapper,
			TypicodeClientProperties properties) {
		this.webClient = webClientBuilder
				.baseUrl(properties.getBaseUrl())
				.build();
		this.userMapper = userMapper;
	}

	@Override
	public Mono<User> findById(Integer id) {
		return webClient
				.get()
				.uri("/users/{id}", id)
				.retrieve()
				.bodyToMono(UserResponse.class)
				.map(userMapper::toUser);
	}

	@Override
	public Flux<User> listAll() {
		try {
			return webClient
					.get()
					.uri("/users")
					.retrieve()
					.bodyToFlux(UserResponse.class)
					.map(userMapper::toUser);
		} catch (Exception e) {
			return Flux.error(new RuntimeException("Failed to fetch users", e));
		}
	}
}