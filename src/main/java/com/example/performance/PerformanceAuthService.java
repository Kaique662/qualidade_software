package com.example.performance;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PerformanceAuthService {

    private static final List<String> EXPECTED_HEADER =
            List.of("username", "password", "resourceId", "teacherId");

    private final Map<String, User> users;
    private final Map<String, String> tokenUsers = new ConcurrentHashMap<>();
    private final Map<String, String> userTokens = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    public PerformanceAuthService(@Value("${perf.users-file:perf/usuarios.csv}") String usersFile) {
        this.users = loadUsers(Path.of(usersFile));
    }

    public String authenticate(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        User user = users.get(username);
        if (user == null || !MessageDigest.isEqual(
                user.password().getBytes(StandardCharsets.UTF_8),
                password.getBytes(StandardCharsets.UTF_8))) {
            return null;
        }
        return userTokens.computeIfAbsent(username, this::newToken);
    }

    public boolean isValidToken(String token) {
        return token != null && tokenUsers.containsKey(token);
    }

    private String newToken(String username) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokenUsers.put(token, username);
        return token;
    }

    private static Map<String, User> loadUsers(Path path) {
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            if (lines.isEmpty() || !parseCsvLine(lines.get(0)).equals(EXPECTED_HEADER)) {
                throw new IllegalStateException("Performance user CSV has an invalid header: " + path);
            }

            Map<String, User> loaded = new HashMap<>();
            for (int lineNumber = 2; lineNumber <= lines.size(); lineNumber++) {
                List<String> fields = parseCsvLine(lines.get(lineNumber - 1));
                if (fields.size() != EXPECTED_HEADER.size()
                        || fields.stream().anyMatch(String::isBlank)) {
                    throw new IllegalStateException("Invalid performance user CSV row " + lineNumber);
                }
                User user = new User(fields.get(0), fields.get(1));
                if (loaded.putIfAbsent(user.username(), user) != null) {
                    throw new IllegalStateException("Duplicate performance username at CSV row " + lineNumber);
                }
            }
            if (loaded.isEmpty()) {
                throw new IllegalStateException("Performance user CSV contains no users: " + path);
            }
            return Map.copyOf(loaded);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read performance user CSV: " + path, exception);
        }
    }

    private static List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);
            if (current == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (current == ',' && !quoted) {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(current);
            }
        }
        if (quoted) {
            throw new IllegalStateException("Unclosed quoted value in performance user CSV");
        }
        fields.add(field.toString());
        return fields;
    }

    private record User(String username, String password) {
    }
}
