package com.agent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.Files.readString;

/**
 * @author loki1
 */
public class Config {
    private static String baseUrl;
    private static String apiKey;
    private static String model;
    private static String systemPrompt;


    //load api config from .env file
    public static void load() throws Exception {
        //baseUrl -> apiKey -> model
        Map<String, String> env = readEnvFile(".env");
        baseUrl = env.getOrDefault("BASE_URL", "https://api.openai.com/v1");
        apiKey = env.get("API_KEY");
        model = env.getOrDefault("MODEL", "gpt-4o-mini");

        //check apiKey
        if (apiKey == null) {
            throw new IllegalStateException(
                    "API key not found. Please create .env file with 'API_KEY=your-key'"
            );
        }

        //load system prompt
        Path promptPath = Path.of("sys_prompt.txt");
        if (Files.exists(promptPath)) {
            systemPrompt = readString(promptPath);
        } else {
            systemPrompt = getDefaultPrompt();
        }
    }

    private static Map<String, String> readEnvFile(String filename) throws IOException {
        Map<String, String> result = new HashMap<>();
        //get file path
        Path path = Path.of(filename);

        if (!Files.exists(path)) {
            return result;
        }

        for (String line : Files.readAllLines(path)) {
            line = line.trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            //parse KEY=VALUE
            int index = line.indexOf('=');
            if (index > 0) {
                String key = line.substring(0, index).trim();
                String value = line.substring(index + 1).trim();
                result.put(key, value);
            }
        }
//        try {
//            return Files.lines(Path.of(filename))
//                    .map(String::trim)
//                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
//                    .map(line -> line.split("=", 2))
//                    .filter(parts -> parts.length == 2)
//                    .collect(
//                            java.util.stream.Collectors.toMap(
//                                    parts -> parts[0],
//                                    parts -> parts[1]
//                            )
//                    );
//        } catch (Exception e) {
//            return Map.of(); // Return empty map if file doesn't exist or error occurs
//        }
        return result;
    }


    public static String getBaseUrl() {
        return baseUrl;
    }

    public static String getApiKey() {
        return apiKey;
    }

    public static String getModel() {
        return model;
    }

    public static String getSystemPrompt() {
        return systemPrompt;
    }

    //if sys_prompt.txt not found, use the default provided prompt
    private static String getDefaultPrompt() {
        return """
            You are a terminal-based AI programming assistant.

            ## Core Rules
            1. You must output bash commands inside ```bash``` code blocks
            2. Each response must contain exactly ONE bash code block (unless task is done)
            3. Output <TASK_DONE> when the task is complete
            4. Never use tool calls - only bash commands

            ## Your Capabilities
            - Create websites (HTML/CSS/JS)
            - Build terminal games (snake, rock paper scissors, etc.)
            - Write algorithms and data structures
            - Execute shell commands
            - Use git for version control

            ## Workflow
            1. Analyze the user's request
            2. Output the bash command to accomplish it
            3. Wait for user approval (y/n)
            4. After execution, report the result
            5. Continue until <TASK_DONE>
            """;
    }
}
