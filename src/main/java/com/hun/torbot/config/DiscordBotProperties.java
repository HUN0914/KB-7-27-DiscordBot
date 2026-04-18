package com.hun.torbot.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "discord")
public record DiscordBotProperties(
        @NotBlank String botToken,
        @NotNull Long reportChannelId,
        Long admonitionChannelId,
        @NotBlank String studyVoiceChannelName,
        @NotBlank String zoneId
) {

    public long resolvedAdmonitionChannelId() {
        return admonitionChannelId != null ? admonitionChannelId : reportChannelId;
    }
}
