package ru.job4j.grabber.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.*;

class HabrCareerDateTimeParserTest {
    @Test
    public void whenParseIsSuccessful() {
        String input = "2023-12-22T12:27:06+03:00";
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        LocalDateTime output = parser.parse(input);
        LocalDateTime expected = LocalDateTime.of(2023, 12, 22, 12, 27, 6);
        assertThat(output).isEqualTo(expected);
    }

    @Test
    public void whenParseIsFailed() {
        String input = "2023-12-22T12:27:06";
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        assertThatThrownBy(() -> parser.parse(input)).isInstanceOf(DateTimeParseException.class);
    }
}
