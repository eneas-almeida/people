package org.people.infrastructure.adapter.typicode;

import org.people.domain.client.UserClient;
import org.people.domain.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class TypicodeClientAdapter implements UserClient {

	private final UserMapper userMapper;

	private final WebClient.Builder webClient;

	public TypicodeClientAdapter(
			UserMapper userMapper,
			WebClient.Builder webClient) {
		this.userMapper = userMapper;
		this.webClient = webClient;
	}

	@Value("${client.typicode.base-url}")
	private String baseUrl;

	@Override
	public Mono<User> findById(Integer id) {
		final ExchangeStrategies strategies = ExchangeStrategies.builder()
				.codecs(config -> config
						.defaultCodecs()
						.maxInMemorySize(16 * 1024 * 1024))
				.build();

		return webClient
				.baseUrl(baseUrl)
				.exchangeStrategies(strategies)
				.build()
				.get()
				.uri("/users/{id}", id)
				.retrieve()
				.bodyToMono(UserResponse.class)
				.map(userMapper::toUser);
	}

	@Override
	public Flux<User> listAll() {
		final ExchangeStrategies strategies = ExchangeStrategies.builder()
				.codecs(config -> config
						.defaultCodecs()
						.maxInMemorySize(16 * 1024 * 1024))
				.build();


		return webClient
				.baseUrl(baseUrl)
				.exchangeStrategies(strategies)
				.build()
				.get()
				.uri("/users")
				.retrieve()
				.bodyToFlux(UserResponse.class)
				.map(userMapper::toUser);
	}
}