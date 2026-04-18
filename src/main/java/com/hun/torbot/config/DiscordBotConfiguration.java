package com.hun.torbot.config;

import com.hun.torbot.discord.TorDiscordListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DiscordBotConfiguration {

    @Bean(destroyMethod = "shutdown")
    public JDA jda(DiscordBotProperties properties, TorDiscordListener listener) throws InterruptedException {
        return JDABuilder.createDefault(properties.botToken())
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_VOICE_STATES
                )
                .setChunkingFilter(ChunkingFilter.NONE)
                .setMemberCachePolicy(MemberCachePolicy.VOICE)
                .disableCache(
                        CacheFlag.ACTIVITY,
                        CacheFlag.CLIENT_STATUS,
                        CacheFlag.EMOJI,
                        CacheFlag.FORUM_TAGS,
                        CacheFlag.ONLINE_STATUS,
                        CacheFlag.ROLE_TAGS,
                        CacheFlag.SCHEDULED_EVENTS,
                        CacheFlag.STICKER,
                        CacheFlag.VOICE_STATE
                )
                .addEventListeners(listener)
                .build()
                .awaitReady();
    }
}
