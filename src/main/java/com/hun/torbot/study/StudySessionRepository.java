package com.hun.torbot.study;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface StudySessionRepository extends JpaRepository<StudySession, Long> {

    Optional<StudySession> findFirstByGuildIdAndUserIdAndEndedAtIsNull(String guildId, String userId);

    List<StudySession> findByGuildIdAndEndedAtGreaterThanAndStartedAtLessThan(String guildId, Instant rangeStart, Instant rangeEnd);

    List<StudySession> findByGuildIdAndEndedAtIsNull(String guildId);
}
