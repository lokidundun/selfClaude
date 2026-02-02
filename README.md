# SelfAgent

---
[简体中文](./README_CN.md)

A versatile CLI AI programming assistant written in Java. Based on the ReAct (Reason + Act) pattern, it helps you accomplish tasks through natural language interaction.

## Features

- **Programming Assistant** - Write, debug, and refactor code in multiple languages
- **File Management** - Create, edit, and organize files via bash commands
- **Command Execution** - Run system commands with user approval for security
- **ReAct Pattern** - Think step by step, execute one command at a time
- **Persistent Context** - Conversation history is preserved across tasks
- **Multi-Provider Support** - Compatible with OpenAI and MiniMax (Anthropic-compatible) APIs

## Requirements

- Java 17+
- Maven 3.6+
- API Key (OpenAI or MiniMax)

## Installation

```bash
# Clone or navigate to the project directory

# Build the project
mvn clean package

# Run
java -jar target\agent-1.0.0.jar
```

## Configuration

Create a `.env` file in the project directory:

```bash
# For MiniMax (Anthropic-compatible)
BASE_URL=https://api.minimaxi.com/anthropic
API_KEY=your-api-key
MODEL=MiniMax-M2.1

# For OpenAI
# BASE_URL=https://api.openai.com/v1
# API_KEY=your-api-key
# MODEL=gpt-4o-mini
```

## Usage

```
> help                     # Show help
> create a Python file     # Describe your task
> quit                     # Exit the program
```

## Interaction

When SelfAgent provides a command, you can:

| Input | Action |
|-------|--------|
| Enter / y / yes | Execute the command |
| n / no | Skip the command |
| m + message | Send a message directly |
| q / quit | Exit the program |

## Workflow

1. You describe a task in natural language
2. SelfAgent analyzes and provides a bash command
3. You approve or modify
4. Execution result is fed back to SelfAgent
5. Repeat until `<TASK_DONE>`

## Architecture

```
User Input → Agent (ReAct Loop)
              ↓
        LLM API Call
              ↓
    Parse Response
              ↓
    Command Execution (with user approval)
              ↓
    Feedback to LLM
```

## Project Structure

```
ogent/
├── src/main/java/com/agent/
│   ├── Main.java           # Entry point
│   ├── Config.java         # Configuration management
│   ├── Agent.java          # Main agent logic (ReAct loop)
│   ├── LLMClient.java      # API client
│   └── CommandExecutor.java # Command parsing & execution
├── pom.xml                 # Maven configuration
├── .env                    # API configuration
├── sys_prompt.txt          # System prompt
└── header.txt              # ASCII art header
```

## Supported Models

| Provider | Model | API Format |
|----------|-------|------------|
| MiniMax | MiniMax-M2.1 | Anthropic-compatible |
| OpenAI | gpt-4o-mini, gpt-4o | OpenAI |

## Security

- All commands require user approval before execution
- API keys are loaded from local `.env` file
- No remote code execution without consent

## License

MIT License

