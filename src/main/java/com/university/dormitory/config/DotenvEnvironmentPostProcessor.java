package com.university.dormitory.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Автоматическая загрузка переменных окружения из файла .env в Spring Environment.
 * Обеспечивает безопасное хранение чувствительных данных вне репозитория.
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Path envPath = Path.of(".env");
        if (!Files.exists(envPath)) {
            // Резервный поиск в родительской директории
            envPath = Path.of("../.env");
        }
        if (!Files.exists(envPath)) {
            return;
        }

        try {
            List<String> lines = Files.readAllLines(envPath, StandardCharsets.UTF_8);
            Map<String, Object> envProperties = new HashMap<>();

            for (String rawLine : lines) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) {
                    continue;
                }

                int eqIndex = line.indexOf('=');
                String key = line.substring(0, eqIndex).trim();
                String value = line.substring(eqIndex + 1).trim();

                // Снятие кавычек, если значение заключено в кавычки
                if ((value.startsWith("\"") && value.endsWith("\"")) ||
                    (value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }

                envProperties.put(key, value);
            }

            if (!envProperties.isEmpty()) {
                environment.getPropertySources().addFirst(
                        new MapPropertySource("dotenvPropertySource", envProperties)
                );
            }
        } catch (IOException ignored) {
            // При ошибке чтения .env используются системные переменные или дефолтные значения
        }
    }
}
