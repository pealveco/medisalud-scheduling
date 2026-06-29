package co.com.medisalud.usecase.common;

import java.time.LocalTime;

/**
 * Working start and end times for a date. The end time is exclusive.
 *
 * @param start opening time
 * @param end closing time
 */
public record TimeWindow(LocalTime start, LocalTime end) {
}
