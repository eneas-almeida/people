package org.bridge.infrastructure.adapter.typicode;

import org.bridge.domain.client.UserClient;
import org.bridge.domain.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class TypicodeUserClientAdapter implements UserClient {
	private final WebClient webClient;
	private final UserMapper userMapper;

	@Value("${client.typicode.base-url}")
	private String baseUrl;

	public TypicodeUserClientAdapter(WebClient.Builder webClientBuilder, UserMapper userMapper) {
		this.webClient = webClientBuilder
				.baseUrl(this.baseUrl)
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
		return webClient
				.get()
				.uri("/users")
				.retrieve()
				.bodyToFlux(UserResponse.class)
				.map(userMapper::toUser);
	}
}