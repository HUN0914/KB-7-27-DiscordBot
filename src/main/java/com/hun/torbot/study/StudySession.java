package com.hun.torbot.study;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "study_sessions")
public class StudySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String guildId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String usernameSnapshot;

    @Column(nullable = false)
    private Instant startedAt;

    private Instant endedAt;

    protected StudySession() {
    }

    public StudySession(String guildId, String userId, String usernameSnapshot, Instant startedAt) {
        this.guildId = guildId;
        this.userId = userId;
        this.usernameSnapshot = usernameSnapshot;
        this.startedAt = startedAt;
    }

    public Long getId() {
        return id;
    }

    public String getGuildId() {
        return guildId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsernameSnapshot() {
        return usernameSnapshot;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public boolean isActive() {
        return endedAt == null;
    }

    public void close(Instant endedAt, String usernameSnapshot) {
        this.endedAt = endedAt;
        this.usernameSnapshot = usernameSnapshot;
    }
}
