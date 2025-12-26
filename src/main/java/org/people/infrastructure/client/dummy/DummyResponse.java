package org.people.infrastructure.client.dummy;

public record DummyResponse(
		Integer id,
		String email,
		String firstName,
		String lastName) {}
