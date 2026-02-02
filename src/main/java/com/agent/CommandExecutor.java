package com.agent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author loki1
 */
public class CommandExecutor {
    private static final Pattern BASH_PATTERN = Pattern.compile("```bash\\s*(.*?)\\s*```", Pattern.DOTALL);
    private static final Pattern THINKING_PATTERN = Pattern.compile("<thinking>(.*?)</thinking>", Pattern.DOTALL);

    public static class ExecutionResult {
        public final boolean hasCommand;
        public final String command;
        public final String thinking;
        public final String rawResponse;

        public ExecutionResult(boolean hasCommand, String command, String thinking, String rawResponse) {
            this.hasCommand = hasCommand;
            this.command = command;
            this.thinking = thinking;
            this.rawResponse = rawResponse;
        }
    }

    /**
     * Parse the LLM response to extract bash command and thinking content
     */
    public static ExecutionResult parseResponse(String response) {
        // Extract thinking content
        Matcher thinkingMatcher = THINKING_PATTERN.matcher(response);
        String thinking = thinkingMatcher.find() ? thinkingMatcher.group(1).trim() : "";

        // Remove thinking tags from response
        String cleanResponse = response.replaceAll("<thinking>.*?</thinking>", "").trim();

        // Extract bash command
        Matcher bashMatcher = BASH_PATTERN.matcher(cleanResponse);
        boolean hasCommand = bashMatcher.find();
        String command = hasCommand ? bashMatcher.group(1).trim() : "";

        return new ExecutionResult(hasCommand, command, thinking, response);
    }

    /**
     * Execute a command with user approval
     * Returns: null if user wants to send a message, "EXIT" if user wants to quit
     */
    public static String executeWithApproval(String command, Scanner scanner) throws IOException, InterruptedException {
        System.out.println("\nCommand to execute:");
        System.out.println(command);
        System.out.print("(y:implement n:skip m:send message q:quit) > ");

        String input = scanner.nextLine().trim();

        if (input.isEmpty() || input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes")) {
            return executeCommand(command);
        } else if (input.equalsIgnoreCase("n") || input.equalsIgnoreCase("no")) {
            return "Command skipped by user.";
        } else if (input.equalsIgnoreCase("q") || input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) {
            return "EXIT";
        } else {
            // User wants to send a message directly
            return "USER_MESSAGE:" + input;
        }
    }

    /**
     * Execute a command and return the output
     */
    public static String executeCommand(String command) throws IOException, InterruptedException {
        System.out.println("\n--- Executing ---\n");

        ProcessBuilder pb = new ProcessBuilder();
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            pb.command("cmd.exe", "/c", command);
        } else {
            pb.command("bash", "-c", command);
        }

        pb.redirectErrorStream(true);

        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            int exitCode = process.waitFor();
            output.append("\n[Exit code: ").append(exitCode).append("]");
        }

        return output.toString();
    }

    /**
     * Check if the response indicates task completion
     */
    public static boolean isTaskDone(String response) {
        return response.contains("<TASK_DONE>");
    }
}
