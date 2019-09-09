package com.example.jpajsondemo;

import javax.persistence.Converter;

@Converter(autoApply = true)
public class NationJsonArrayConverter extends JsonArrayConverter<Nation> {
    public NationJsonArrayConverter() {
        super(Nation.class);
    }
}