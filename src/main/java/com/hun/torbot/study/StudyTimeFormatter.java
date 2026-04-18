package com.hun.torbot.study;

import java.time.Duration;

public final class StudyTimeFormatter {

    private StudyTimeFormatter() {
    }

    public static String format(Duration duration) {
        long totalMinutes = duration.toMinutes();
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        long seconds = duration.minusMinutes(totalMinutes).toSeconds();

        if (hours > 0) {
            return minutes > 0 ? hours + "시간 " + minutes + "분" : hours + "시간";
        }
        if (minutes > 0) {
            return seconds > 0 ? minutes + "분 " + seconds + "초" : minutes + "분";
        }
        return Math.max(seconds, 0) + "초";
    }
}
