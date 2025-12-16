package org.people.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class People {
	private final Integer id;
	private final String name;
	private final String email;
}