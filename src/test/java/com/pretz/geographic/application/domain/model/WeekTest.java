package com.pretz.geographic.application.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class WeekTest {

    @ParameterizedTest
    @MethodSource("datesAndWeeks")
    void shouldCreateIsoWeekFromDate(LocalDate date, Week expectedWeek) {

        //when
        Week week = Week.of(date);

        //then
        assertThat(week).isEqualTo(expectedWeek);
    }

    @Test
    void shouldOrderWeeksChronologically() {

        //given
        Week lastWeekOf2020 = Week.of(2020, 53);
        Week firstWeekOf2021 = Week.of(2021, 1);
        Week tenthWeekOf2021 = Week.of(2021, 10);

        //when, then
        assertThat(List.of(tenthWeekOf2021, lastWeekOf2020, firstWeekOf2021).stream().sorted().toList())
                .containsExactly(lastWeekOf2020, firstWeekOf2021, tenthWeekOf2021);
        assertThat(firstWeekOf2021.isAfter(lastWeekOf2020)).isTrue();
        assertThat(lastWeekOf2020.isAfter(firstWeekOf2021)).isFalse();
        assertThat(firstWeekOf2021.isAfter(firstWeekOf2021)).isFalse();
    }

    private static Stream<Arguments> datesAndWeeks() {
        return Stream.of(
                Arguments.of(LocalDate.of(2025, 1, 6), Week.of(2025, 2)),
                Arguments.of(LocalDate.of(2025, 1, 12), Week.of(2025, 2)),
                Arguments.of(LocalDate.of(2021, 1, 3), Week.of(2020, 53)),
                Arguments.of(LocalDate.of(2024, 12, 30), Week.of(2025, 1)));
    }
}
