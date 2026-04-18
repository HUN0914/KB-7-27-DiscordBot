package com.hun.torbot.attendance;

public record AttendanceResult(
        boolean firstCheckInToday,
        String displayName
) {
}
