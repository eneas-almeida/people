package org.bridge.domain.client;

import org.bridge.domain.entity.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserClient {
	Mono<User> findById(Integer id);
	Flux<User> listAll();
}