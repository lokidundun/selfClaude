# SelfAgent


---
一个基于 Java 编写的多功能 CLI 编程助手。采用 ReAct（思考 + 执行）模式，通过自然语言交互帮助你完成任务。

## 功能特点

- **编程助手** - 多语言代码编写、调试、重构
- **文件管理** - 通过 bash 命令创建、编辑、组织文件
- **命令执行** - 需用户批准，安全可控
- **ReAct 模式** - 逐步思考，每次只执行一条命令
- **上下文保留** - 对话历史跨任务保留
- **多模型支持** - 支持 OpenAI 和 MiniMax（Anthropic 兼容）API

## 环境要求

- Java 17+
- Maven 3.6+
- API 密钥（OpenAI 或 MiniMax）

## 安装

```bash
# 进入项目目录

# 构建项目
mvn clean package

# 运行
java -jar target\agent-1.0.0.jar
```

## 配置

在项目目录下创建 `.env` 文件：

```bash
# MiniMax 配置（Anthropic 兼容）
BASE_URL=https://api.minimaxi.com/anthropic
API_KEY=你的API密钥
MODEL=MiniMax-M2.1

# OpenAI 配置
# BASE_URL=https://api.openai.com/v1
# API_KEY=你的API密钥
# MODEL=gpt-4o-mini
```

## 使用方法

```
> help                     # 显示帮助
> 创建一个 Python 文件     # 描述你的任务
> quit                     # 退出程序
```

## 交互方式

当 SelfAgent 提供命令时：

| 输入 | 操作 |
|------|------|
| 回车 / y / yes | 执行命令 |
| n / no | 跳过命令 |
| m + 内容 | 直接发送消息 |
| q / quit | 退出程序 |

## 工作流程

```
用户输入 → Agent（ReAct 循环）
              ↓
        调用 LLM API
              ↓
    解析响应内容
              ↓
    命令执行（需用户批准）
              ↓
    执行结果反馈给 LLM
```

## 项目结构

```
ogent/
├── src/main/java/com/agent/
│   ├── Main.java           # 程序入口
│   ├── Config.java         # 配置管理
│   ├── Agent.java          # 主体逻辑（ReAct 循环）
│   ├── LLMClient.java      # API 客户端
│   └── CommandExecutor.java # 命令解析与执行
├── pom.xml                 # Maven 配置
├── .env                    # API 配置
├── sys_prompt.txt          # 系统提示词
└── header.txt              # ASCII 艺术字
```

## 支持的模型

| 提供商 | 模型 | API 格式 |
|--------|------|----------|
| MiniMax | MiniMax-M2.1 | Anthropic 兼容 |
| OpenAI | gpt-4o-mini, gpt-4o | OpenAI 格式 |

## 安全说明

- 所有命令执行前需用户批准
- API 密钥从本地 `.env` 文件加载
- 未经同意不会执行远程代码

## 开源协议

MIT License

