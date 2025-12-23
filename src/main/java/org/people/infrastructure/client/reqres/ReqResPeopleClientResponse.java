package org.people.infrastructure.client.reqres;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ReqResPeopleClientResponse(
		Integer id,
		String email,
		String firstName,
		String lastName) {}