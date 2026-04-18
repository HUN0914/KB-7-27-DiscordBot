package com.hun.torbot;

import com.hun.torbot.study.StudyTimeFormatter;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class TorStudyBotApplicationTests {

    @Test
    void formatsDurationsInKorean() {
        assertThat(StudyTimeFormatter.format(Duration.ofMinutes(130))).isEqualTo("2시간 10분");
        assertThat(StudyTimeFormatter.format(Duration.ofSeconds(45))).isEqualTo("45초");
    }
}
