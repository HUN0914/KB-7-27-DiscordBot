package com.hun.torbot.study;

import com.hun.torbot.config.DiscordBotProperties;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class StudyTrackingService {

    private final StudySessionRepository studySessionRepository;
    private final DiscordBotProperties properties;
    private final Clock clock;

    public StudyTrackingService(StudySessionRepository studySessionRepository, DiscordBotProperties properties) {
        this.studySessionRepository = studySessionRepository;
        this.properties = properties;
        this.clock = Clock.system(ZoneId.of(properties.zoneId()));
    }

    @Transactional
    public void handleVoiceStateChange(Member member, AudioChannelUnion previousChannel, AudioChannelUnion currentChannel) {
        boolean wasStudying = isStudyChannel(previousChannel);
        boolean isStudying = isStudyChannel(currentChannel);

        if (!wasStudying && isStudying) {
            startSession(member);
            return;
        }
        if (wasStudying && !isStudying) {
            stopSession(member);
        }
    }

    @Transactional
    public void bootstrapGuild(Guild guild) {
        for (VoiceChannel voiceChannel : guild.getVoiceChannels()) {
            if (!voiceChannel.getName().equals(properties.studyVoiceChannelName())) {
                continue;
            }
            voiceChannel.getMembers().forEach(this::startSessionIfAbsent);
        }
    }

    @Transactional(readOnly = true)
    public List<StudyMemberSummary> getGuildSummaries(Guild guild, StudyRangeType rangeType) {
        Map<String, Duration> durations = computeDurations(guild, rangeType);
        List<StudyMemberSummary> summaries = new ArrayList<>();

        for (Map.Entry<String, Duration> entry : durations.entrySet()) {
            if (entry.getValue().isZero() || entry.getValue().isNegative()) {
                continue;
            }
            summaries.add(new StudyMemberSummary(
                    entry.getKey(),
                    resolveDisplayName(guild, entry.getKey()),
                    entry.getValue()
            ));
        }

        summaries.sort(Comparator.comparing(StudyMemberSummary::duration).reversed());
        return summaries;
    }

    @Transactional(readOnly = true)
    public Optional<StudyMemberSummary> getMemberSummary(Guild guild, String memberQuery, StudyRangeType rangeType) {
        Member member = resolveMember(guild, memberQuery);
        if (member == null) {
            return Optional.empty();
        }

        Duration duration = computeDurations(guild, rangeType).getOrDefault(member.getId(), Duration.ZERO);
        return Optional.of(new StudyMemberSummary(member.getId(), member.getEffectiveName(), duration));
    }

    public String getStudyChannelName() {
        return properties.studyVoiceChannelName();
    }

    private Map<String, Duration> computeDurations(Guild guild, StudyRangeType rangeType) {
        ZonedDateTime now = ZonedDateTime.now(clock).withZoneSameInstant(ZoneId.of(properties.zoneId()));
        Instant rangeStart = rangeType.rangeStart(now).toInstant();
        Instant rangeEnd = now.toInstant();

        Map<String, Duration> totals = new LinkedHashMap<>();
        List<StudySession> completedSessions = studySessionRepository
                .findByGuildIdAndEndedAtGreaterThanAndStartedAtLessThan(guild.getId(), rangeStart, rangeEnd);

        for (StudySession session : completedSessions) {
            totals.merge(session.getUserId(), overlap(session.getStartedAt(), session.getEndedAt(), rangeStart, rangeEnd), Duration::plus);
        }

        for (StudySession session : studySessionRepository.findByGuildIdAndEndedAtIsNull(guild.getId())) {
            totals.merge(session.getUserId(), overlap(session.getStartedAt(), rangeEnd, rangeStart, rangeEnd), Duration::plus);
        }

        return totals;
    }

    private Duration overlap(Instant sessionStart, Instant sessionEnd, Instant rangeStart, Instant rangeEnd) {
        Instant actualStart = sessionStart.isAfter(rangeStart) ? sessionStart : rangeStart;
        Instant actualEnd = sessionEnd.isBefore(rangeEnd) ? sessionEnd : rangeEnd;
        if (!actualEnd.isAfter(actualStart)) {
            return Duration.ZERO;
        }
        return Duration.between(actualStart, actualEnd);
    }

    private boolean isStudyChannel(AudioChannelUnion channel) {
        return channel != null && channel.getName().equals(properties.studyVoiceChannelName());
    }

    private void startSession(Member member) {
        startSessionIfAbsent(member);
    }

    private void startSessionIfAbsent(Member member) {
        studySessionRepository.findFirstByGuildIdAndUserIdAndEndedAtIsNull(member.getGuild().getId(), member.getId())
                .orElseGet(() -> studySessionRepository.save(
                        new StudySession(
                                member.getGuild().getId(),
                                member.getId(),
                                member.getEffectiveName(),
                                Instant.now(clock)
                        )
                ));
    }

    private void stopSession(Member member) {
        studySessionRepository.findFirstByGuildIdAndUserIdAndEndedAtIsNull(member.getGuild().getId(), member.getId())
                .ifPresent(session -> session.close(Instant.now(clock), member.getEffectiveName()));
    }

    private Member resolveMember(Guild guild, String memberQuery) {
        String trimmed = memberQuery.trim();
        String mentionDigits = trimmed.replaceAll("[^0-9]", "");
        if (!mentionDigits.isBlank()) {
            Member mentioned = guild.getMemberById(mentionDigits);
            if (mentioned != null) {
                return mentioned;
            }
        }

        List<Member> exactMatches = matchMembers(guild.getMembers(), trimmed, true);
        if (exactMatches.size() == 1) {
            return exactMatches.getFirst();
        }

        List<Member> partialMatches = matchMembers(guild.getMembers(), trimmed, false);
        return partialMatches.size() == 1 ? partialMatches.getFirst() : null;
    }

    private List<Member> matchMembers(Collection<Member> members, String query, boolean exact) {
        String normalizedQuery = query.toLowerCase(Locale.ROOT);
        List<Member> matches = new ArrayList<>();
        for (Member member : members) {
            List<String> candidates = List.of(
                    member.getEffectiveName(),
                    member.getUser().getName(),
                    member.getUser().getGlobalName() == null ? "" : member.getUser().getGlobalName()
            );

            boolean matched = candidates.stream()
                    .map(value -> value == null ? "" : value.toLowerCase(Locale.ROOT))
                    .anyMatch(value -> exact ? value.equals(normalizedQuery) : value.contains(normalizedQuery));
            if (matched) {
                matches.add(member);
            }
        }
        return matches;
    }

    private String resolveDisplayName(Guild guild, String userId) {
        Member member = guild.getMemberById(userId);
        if (member != null) {
            return member.getEffectiveName();
        }
        return studySessionRepository.findFirstByGuildIdAndUserIdAndEndedAtIsNull(guild.getId(), userId)
                .map(StudySession::getUsernameSnapshot)
                .orElse(userId);
    }
}
