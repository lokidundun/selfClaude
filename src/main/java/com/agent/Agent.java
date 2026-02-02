package com.agent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @author loki1
 */
public class Agent {
    private final LLMClient llmClient;
    private final List<LLMClient.Message> conversationHistory;

    public Agent() {
        this.llmClient = new LLMClient(
                Config.getBaseUrl(),
                Config.getApiKey(),
                Config.getModel()
        );
        this.conversationHistory = new ArrayList<>();

        // Add system prompt
        conversationHistory.add(new LLMClient.Message("system", Config.getSystemPrompt()));
    }

    public void start() throws IOException, InterruptedException {
        System.out.println("\nStarting ogent... (Type 'quit' to exit)\n");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("> ");
            String userInput = scanner.nextLine().trim();

            if (userInput.isEmpty()) {
                continue;
            }

            if (userInput.equalsIgnoreCase("quit") || userInput.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye!");
                break;
            }

            // Add user message to history
            conversationHistory.add(new LLMClient.Message("user", userInput));

            // Run the task loop
            runTaskLoop(scanner);
            // 注意：不清理对话历史，保留上下文
        }

        scanner.close();
    }

    private void runTaskLoop(Scanner scanner) throws IOException, InterruptedException {
        while (true) {
            // Get response from LLM
            System.out.println("\nThinking...\n");
            String response = llmClient.chat(new ArrayList<>(conversationHistory));

            // Print thinking if present
            CommandExecutor.ExecutionResult result = CommandExecutor.parseResponse(response);
            if (!result.thinking.isEmpty()) {
                System.out.println("[Thinking] " + result.thinking);
            }

            // Check if task is done
            if (CommandExecutor.isTaskDone(response)) {
                System.out.println("\n" + response.replace("<TASK_DONE>", "").trim());
                break;
            }

            // Parse and execute command
            if (result.hasCommand) {
                // Add assistant message to history
                conversationHistory.add(new LLMClient.Message("assistant", response));

                String executionOutput = CommandExecutor.executeWithApproval(result.command, scanner);

                // Check if user wants to exit
                if ("EXIT".equals(executionOutput)) {
                    System.out.println("Goodbye!");
                    System.exit(0);
                    return;
                }

                // Check if user wants to send a message directly
                if (executionOutput != null && executionOutput.startsWith("USER_MESSAGE:")) {
                    // User wants to send a message directly - break out and let them input
                    break;
                }

                // Add execution result as user message
                conversationHistory.add(new LLMClient.Message("user",
                        "Command output:\n" + executionOutput + "\n\nWhat's next?"));
            } else {
                // No command found - this is a conversational response, exit task loop
                System.out.println("\n" + response);
                break;
            }
        }
    }
}
