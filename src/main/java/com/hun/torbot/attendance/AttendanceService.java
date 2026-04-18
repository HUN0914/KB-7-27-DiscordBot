package com.hun.torbot.attendance;

import com.hun.torbot.config.DiscordBotProperties;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final Clock clock;

    public AttendanceService(AttendanceRecordRepository attendanceRecordRepository, DiscordBotProperties properties) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.clock = Clock.system(ZoneId.of(properties.zoneId()));
    }

    @Transactional
    public AttendanceResult checkIn(Member member) {
        LocalDate today = LocalDate.now(clock);
        return attendanceRecordRepository
                .findFirstByGuildIdAndUserIdAndAttendanceDate(member.getGuild().getId(), member.getId(), today)
                .map(record -> new AttendanceResult(false, member.getEffectiveName()))
                .orElseGet(() -> {
                    attendanceRecordRepository.save(new AttendanceRecord(
                            member.getGuild().getId(),
                            member.getId(),
                            member.getEffectiveName(),
                            today,
                            Instant.now(clock)
                    ));
                    return new AttendanceResult(true, member.getEffectiveName());
                });
    }
}
