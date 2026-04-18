package com.hun.torbot.discord;

import com.hun.torbot.attendance.AttendanceResult;
import com.hun.torbot.attendance.AttendanceService;
import com.hun.torbot.study.StudyMemberSummary;
import com.hun.torbot.study.StudyRangeType;
import com.hun.torbot.study.StudyTimeFormatter;
import com.hun.torbot.study.StudyTrackingService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TorCommandService {

    private static final Pattern STUDY_QUERY_PATTERN =
            Pattern.compile("^!(?:(.+?)\\s+)?(일간|주간|월간)\\s*공부\\s*시간$", Pattern.CASE_INSENSITIVE);

    private final AttendanceService attendanceService;
    private final StudyTrackingService studyTrackingService;

    public TorCommandService(AttendanceService attendanceService, StudyTrackingService studyTrackingService) {
        this.attendanceService = attendanceService;
        this.studyTrackingService = studyTrackingService;
    }

    public void handle(Message message) {
        if (message.getAuthor().isBot()) {
            return;
        }
        if (!message.isFromGuild()) {
            return;
        }

        String raw = message.getContentRaw().trim();
        if (!raw.startsWith("!")) {
            return;
        }

        MessageChannel channel = message.getChannel();
        Guild guild = message.getGuild();

        if (raw.equals("!") || raw.equalsIgnoreCase("!help") || raw.equals("!명령어") || raw.equals("!도움말")) {
            channel.sendMessage(helpMessage()).queue();
            return;
        }
        if (raw.equals("!출석")) {
            AttendanceResult result = attendanceService.checkIn(message.getMember());
            if (result.firstCheckInToday()) {
                channel.sendMessage("`" + result.displayName() + "`님 오늘 출석 완료했습니다.").queue();
                return;
            }
            channel.sendMessage("`" + result.displayName() + "`님은 오늘 이미 출석했습니다.").queue();
            return;
        }

        Matcher matcher = STUDY_QUERY_PATTERN.matcher(raw);
        if (!matcher.matches()) {
            channel.sendMessage("알 수 없는 명령어입니다. `!` 또는 `!명령어`를 입력해 보세요.").queue();
            return;
        }

        String memberQuery = matcher.group(1);
        StudyRangeType rangeType = switch (matcher.group(2)) {
            case "일간" -> StudyRangeType.DAILY;
            case "주간" -> StudyRangeType.WEEKLY;
            case "월간" -> StudyRangeType.MONTHLY;
            default -> throw new IllegalStateException("Unsupported range");
        };

        if (memberQuery == null || memberQuery.isBlank()) {
            channel.sendMessage(renderGuildSummary(guild, rangeType)).queue();
            return;
        }

        Optional<StudyMemberSummary> summary = studyTrackingService.getMemberSummary(guild, memberQuery, rangeType);
        if (summary.isEmpty()) {
            channel.sendMessage("해당 사용자를 찾지 못했습니다. 닉네임이나 멘션으로 다시 입력해 주세요.").queue();
            return;
        }

        StudyMemberSummary result = summary.get();
        channel.sendMessage("`" + result.displayName() + "`님의 " + rangeType.label() + " 공부 시간은 "
                + StudyTimeFormatter.format(result.duration()) + "입니다.").queue();
    }

    public String renderGuildSummary(Guild guild, StudyRangeType rangeType) {
        List<StudyMemberSummary> summaries = studyTrackingService.getGuildSummaries(guild, rangeType);
        if (summaries.isEmpty()) {
            return "**" + rangeType.label() + " 공부 시간**\n아직 `" + studyTrackingService.getStudyChannelName() + "` 기록이 없습니다.";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("**").append(rangeType.label()).append(" 공부 시간 순위**\n");
        for (int i = 0; i < summaries.size(); i++) {
            StudyMemberSummary summary = summaries.get(i);
            builder.append(i + 1)
                    .append(". ")
                    .append(summary.displayName())
                    .append(" - ")
                    .append(StudyTimeFormatter.format(summary.duration()))
                    .append('\n');
        }
        return builder.toString().trim();
    }

    private String helpMessage() {
        return """
                **토르 명령어**
                `!` 또는 `!명령어`
                입력 가능한 명령어 목록을 보여줍니다.

                `!일간 공부 시간`
                서버 멤버들의 오늘 공부 시간을 보여줍니다.

                `!주간 공부 시간`
                서버 멤버들의 이번 주 공부 시간을 보여줍니다.

                `!월간 공부 시간`
                서버 멤버들의 이번 달 공부 시간을 보여줍니다.

                `!닉네임 일간 공부 시간`
                특정 멤버의 오늘 공부 시간을 보여줍니다.

                `!닉네임 주간 공부 시간`
                특정 멤버의 이번 주 공부 시간을 보여줍니다.

                `!닉네임 월간 공부 시간`
                특정 멤버의 이번 달 공부 시간을 보여줍니다.

                `!출석`
                오늘 출석을 기록합니다. 하루에 한 번만 가능합니다.

                기준 음성채널: `%s`
                """.formatted(studyTrackingService.getStudyChannelName());
    }
}
