package com.hun.torbot.discord;

import com.hun.torbot.config.DiscordBotProperties;
import com.hun.torbot.study.StudyRangeType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TorScheduler {

    private final JDA jda;
    private final DiscordBotProperties properties;
    private final TorCommandService torCommandService;
    private final AdmonitionService admonitionService;

    public TorScheduler(JDA jda,
                        DiscordBotProperties properties,
                        TorCommandService torCommandService,
                        AdmonitionService admonitionService) {
        this.jda = jda;
        this.properties = properties;
        this.torCommandService = torCommandService;
        this.admonitionService = admonitionService;
    }

    @Scheduled(cron = "0 59 23 * * SUN", zone = "${discord.zone-id:Asia/Seoul}")
    public void sendWeeklyStudyReport() {
        TextChannel channel = jda.getTextChannelById(properties.reportChannelId());
        if (channel == null || channel.getGuild() == null) {
            return;
        }

        String message = "**토르 주간 공부 리포트**\n" + torCommandService.renderGuildSummary(channel.getGuild(), StudyRangeType.WEEKLY);
        channel.sendMessage(message).queue();
    }

    @Scheduled(cron = "0 0 19 * * *", zone = "${discord.zone-id:Asia/Seoul}")
    public void sendDailyAdmonition() {
        TextChannel channel = jda.getTextChannelById(properties.resolvedAdmonitionChannelId());
        if (channel == null) {
            return;
        }
        channel.sendMessage("**토르의 한마디**\n" + admonitionService.randomMessage()).queue();
    }
}
