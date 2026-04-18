package com.hun.torbot.study;

import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;

public enum StudyRangeType {
    DAILY("일간"),
    WEEKLY("주간"),
    MONTHLY("월간");

    private final String koreanLabel;

    StudyRangeType(String koreanLabel) {
        this.koreanLabel = koreanLabel;
    }

    public String label() {
        return koreanLabel;
    }

    public ZonedDateTime rangeStart(ZonedDateTime now) {
        return switch (this) {
            case DAILY -> now.toLocalDate().atStartOfDay(now.getZone());
            case WEEKLY -> now.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                    .toLocalDate()
                    .atStartOfDay(now.getZone());
            case MONTHLY -> now.withDayOfMonth(1).toLocalDate().atStartOfDay(now.getZone());
        };
    }
}
