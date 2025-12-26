package org.people.infrastructure.client.dummy;

import java.util.List;

public record DummyListResponse(
		List<DummyResponse> users,
		Integer total,
		Integer skip,
		Integer limit
) {}
