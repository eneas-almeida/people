package org.people.infrastructure.client.reqres;

import org.people.application.dto.PeopleResponse;
import org.people.domain.client.PeopleClient;
import org.people.infrastructure.exception.ExternalServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ReqResPeopleClientImpl implements PeopleClient {

	@Autowired
	@Qualifier("reqresWebClient")
	private WebClient reqresWebClient;

	@Autowired
	private ReqResPeopleClientMapper reqResPeopleClientMapper;

	@Override
	public Mono<PeopleResponse> findById(Integer id) {
		return reqresWebClient
				.get()
				.uri("/users/{id}", id)
				.header("Accept", "application/json")
				.retrieve()
				.bodyToMono(ReqResPeopleClientResponse.class)
				.map(response -> {
						PeopleResponse peopleResponse = reqResPeopleClientMapper.toPeopleResponse(response);
						return peopleResponse;
				})
				.onErrorMap(e -> {
					String errorMessage = "Error fetching people from ReqRes: " + e.getMessage();
					return new ExternalServiceException(errorMessage, "ReqRes API", e);
				});
	}

	@Override
	public Flux<PeopleResponse> listAll() {
		return null;
//		return reqresWebClient
//				.get()
//				.uri("/users")
//				.retrieve()
//				.bodyToMono(JsonNode.class)
//				.flatMapMany(json -> {
//					JsonNode dataArray = json.get("data");
//					return Flux.fromIterable(dataArray)
//							.map(data -> reqResPeopleClientMapper.toPeopleResponse(new ReqResPeopleClientResponse(
//									data.get("id").asInt(),
//									data.get("email").asText(),
//									data.get("first_name").asText(),
//									data.get("last_name").asText()
//							)));
//				})
//				.onErrorMap(e -> {
//				    String errorMessage = "Error fetching people list from ReqRes: " + e.getMessage();
//				    return new ExternalServiceException(errorMessage, "ReqRes API", e);
//				});
	}
}