package org.people.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PeopleResponse {
    
    private final Integer id;
    private final String name;
    private final String email;
}