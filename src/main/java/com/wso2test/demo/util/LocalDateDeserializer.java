package com.wso2test.demo.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalDateDeserializer extends JsonDeserializer<LocalDate> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateStr = p.getText().trim();  // Get the string and trim any spaces

        if (dateStr == null || dateStr.isEmpty()) {
            return null;  // or throw an exception based on your preference
        }
        dateStr = dateStr.replaceAll("(\\d{4})-(\\d{1})-(\\d{1})", "$1-$2-0$3");
        try {
            return LocalDate.parse(dateStr, formatter);  // Convert the string to LocalDate
        } catch (DateTimeParseException e) {
            throw new IOException("Invalid date format: " + dateStr, e);  // You can customize this message
        }
    }
}
