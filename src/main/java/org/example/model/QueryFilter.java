package org.example.model;

public record QueryFilter(
        String column,
        String value
) {
}