package com.hun.torbot.discord;

import com.hun.torbot.study.StudyTrackingService;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class TorDiscordListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(TorDiscordListener.class);

    private final TorCommandService torCommandService;
    private final StudyTrackingService studyTrackingService;

    public TorDiscordListener(TorCommandService torCommandService, StudyTrackingService studyTrackingService) {
        this.torCommandService = torCommandService;
        this.studyTrackingService = studyTrackingService;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        log.info("Discord bot is ready. Guild count={}, guilds={}",
                event.getJDA().getGuilds().size(),
                event.getJDA().getGuilds().stream()
                        .map(guild -> guild.getName() + "(" + guild.getId() + ")")
                        .toList());
        event.getJDA().getGuilds().forEach(guild -> {
            guild.updateCommands()
                    .addCommands(torCommandService.slashCommands())
                    .queue(
                            commands -> log.info("Slash commands synced for guild={}({}), count={}",
                                    guild.getName(), guild.getId(), commands.size()),
                            failure -> log.error("Failed to sync slash commands for guild={}({})",
                                    guild.getName(), guild.getId(), failure)
                    );
            CompletableFuture.runAsync(() -> {
                try {
                    log.info("Starting async guild bootstrap. guild={}({})", guild.getName(), guild.getId());
                    studyTrackingService.bootstrapGuild(guild);
                    log.info("Finished async guild bootstrap. guild={}({})", guild.getName(), guild.getId());
                } catch (Exception exception) {
                    log.error("Async guild bootstrap failed. guild={}({})", guild.getName(), guild.getId(), exception);
                }
            });
        });
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        log.info("Message received. guild={}, channel={}, author={}, content={}",
                event.isFromGuild() ? event.getGuild().getId() : "DM",
                event.getChannel().getId(),
                event.getAuthor().getName(),
                event.getMessage().getContentRaw());
        torCommandService.handle(event.getMessage());
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        log.info("Slash command received. guild={}, channel={}, user={}, name={}",
                event.getGuild() == null ? "DM" : event.getGuild().getId(),
                event.getChannel().getId(),
                event.getUser().getName(),
                event.getName());
        torCommandService.handle(event);
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        studyTrackingService.handleVoiceStateChange(event.getMember(), event.getChannelLeft(), event.getChannelJoined());
    }
}
