package com.hun.torbot.attendance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    Optional<AttendanceRecord> findFirstByGuildIdAndUserIdAndAttendanceDate(String guildId, String userId, LocalDate attendanceDate);
}
