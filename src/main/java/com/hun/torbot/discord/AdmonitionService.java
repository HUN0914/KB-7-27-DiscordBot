package com.hun.torbot.discord;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;

@Service
public class AdmonitionService {

    private final ResourceLoader resourceLoader;
    private final Random random = new Random();
    private List<String> admonitions;

    public AdmonitionService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    void load() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:admonitions.txt");
        try (var inputStream = resource.getInputStream()) {
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            admonitions = content.lines()
                    .map(String::trim)
                    .filter(line -> !line.isBlank())
                    .toList();
        }
    }

    public String randomMessage() {
        if (admonitions == null || admonitions.isEmpty()) {
            return "공부는 핑계 기다려주지 않는다. 오늘 분량부터 끝내라.";
        }
        return admonitions.get(random.nextInt(admonitions.size()));
    }
}
