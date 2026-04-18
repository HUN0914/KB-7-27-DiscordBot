package com.hun.torbot.study;

import java.time.Duration;

public record StudyMemberSummary(
        String userId,
        String displayName,
        Duration duration
) {
}
