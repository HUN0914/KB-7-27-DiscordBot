package com.hun.torbot.attendance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "attendance_records")
public class AttendanceRecord {

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
    private LocalDate attendanceDate;

    @Column(nullable = false)
    private Instant checkedInAt;

    protected AttendanceRecord() {
    }

    public AttendanceRecord(String guildId, String userId, String usernameSnapshot, LocalDate attendanceDate, Instant checkedInAt) {
        this.guildId = guildId;
        this.userId = userId;
        this.usernameSnapshot = usernameSnapshot;
        this.attendanceDate = attendanceDate;
        this.checkedInAt = checkedInAt;
    }
}
