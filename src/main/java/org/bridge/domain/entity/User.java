package org.bridge.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class User {
	private final Integer id;
	private final String name;
	private final String email;
}