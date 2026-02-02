package com.agent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author loki1
 */
public class Main {
    public static void main(String[] args) {
        try {
            // Print ASCII header
            printHeader();

            // Load configuration
            Config.load();

            // Start the agent
            Agent agent = new Agent();
            agent.start();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void printHeader() throws IOException {
        Path headerPath = Paths.get("header.txt");
        if (Files.exists(headerPath)) {
            String header = Files.readString(headerPath);
            System.out.println(header);
        }
    }
}
