# Z-AI-Agent：智能AI代理平台

<div align="center">
  <img src="https://img.shields.io/badge/Java-21-orange" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen" alt="Spring Boot 3.4.5"/>
  <img src="https://img.shields.io/badge/Spring%20AI-1.0.0--M8-blue" alt="Spring AI 1.0.0-M8"/>
  <img src="https://img.shields.io/badge/LangChain4j-1.0.0--beta4-yellow" alt="LangChain4j 1.0.0-beta4"/>
  <img src="https://img.shields.io/badge/PGVector-向量数据库-blueviolet" alt="PGVector"/>
</div>

## 📝 项目介绍

Z-AI-Agent 是一个基于 Java 21 和 Spring Boot 3 的智能AI代理平台，整合Spring AI和LangChain4j框架，实现了智能对话、知识库检索、工具调用、多智能体协作等功能。项目支持通义千问、Ollama等多种大模型接入，并通过MCP协议扩展AI能力边界，适用于各类AI应用场景开发。

## 🌟 核心特性

- **多模型集成**：快速接入通义千问、Ollama等多种AI大模型，实现模型灵活切换
- **多轮对话记忆**：支持基于文件、内存和数据库的多种对话记忆实现，保持对话连贯性
- **RAG知识库检索**：基于PGVector实现向量检索增强生成，提升AI回答质量
- **Tool Calling工具集**：丰富的工具调用能力，包括搜索、PDF生成、网页爬取等
- **MCP模型上下文协议**：集成高德地图等MCP服务，实现标准化的模型与工具交互
- **ReAct智能体**：基于"思考-行动"模式构建智能体，具备自主决策和工具选择能力
- **SSE异步推送**：实现服务器发送事件的流式响应，提升用户体验

## 🛠️ 技术实现

### AI大模型集成
- 利用Spring AI框架实现多模型统一接口，支持通义千问、Ollama等模型
- 基于Ollama实现本地大模型部署，降低API调用成本
- 应用Prompt优化技术提升AI回答质量

### 对话记忆与上下文管理
- 通过MessageChatMemoryAdvisor实现多轮对话上下文记忆
- 自主实现FileBaseChatMemory，结合Kryo序列化保存对话历史
- 开发DatabaseChatMemory支持数据库持久化对话记忆
- 实现MyLoggerAdvisor和ReReadingAdvisor提升对话质量

### RAG知识增强生成
- 使用PgVectorStore实现向量存储与检索
- 基于MarkdownDocumentReader处理恋爱知识文档
- 自定义文档切片和元数据增强提升检索精准度
- 实现QueryRewriter优化用户查询质量

### 工具调用能力
- 开发WebSearchTool提供基于SearchAPI的网络搜索能力
- 实现PDFGenerationTool支持PDF报告生成
- 集成JSoup开发WebSpiderTool支持网页内容抓取
- 提供文件操作和终端操作等工具扩展AI能力

### MCP协议集成
- 通过Spring AI MCP Client集成高德地图服务
- 实现自定义图片搜索MCP服务，支持Stdio和SSE两种传输模式
- 配置mcp-servers.json管理多种MCP服务接入

### ReAct智能体架构
- 实现BaseAgent、ReActAgent和TooCallAgent分层架构
- 基于"思考-行动"循环模式实现自主决策
- 开发AgentState状态管理防止Agent循环执行
- 设计工具调用流程，支持AI自主选择合适工具

### 系统架构与部署
- 基于Spring Boot 3构建核心架构，整合Hutool和MyBatis-Plus
- 使用Knife4j提供API文档，Sa-Token实现权限认证
- 实现全局异常处理增强系统健壮性
- 支持SSE流式响应提升用户体验

## 📋 应用场景

### 恋爱顾问（LoveApp）
- 基于RAG知识库的智能恋爱咨询系统
- 支持单身、恋爱中、已婚多种场景问题解答
- 提供恋爱报告生成，支持结构化输出
- 集成多种工具为用户提供全方位恋爱建议

### Manus智能体
- 基于ReAct模式的多功能智能助手
- 支持自主规划和工具选择完成复杂任务
- 结合MCP协议提供地图、搜索等增强功能
- 通过SSE实现推理过程实时输出

## 🚀 快速开始

### 环境要求
- JDK 21+
- PostgreSQL 14+（启用pgvector扩展）
- Node.js 16+（前端开发）
- Ollama（可选，用于本地模型部署）

### 配置步骤
1. 克隆项目代码
```bash
git clone https://github.com/your-username/z-ai-agent.git
cd z-ai-agent
```

2. 配置数据库
- 创建PostgreSQL数据库并启用pgvector扩展
- 执行`schema.sql`初始化数据库结构

3. 修改配置文件
- 编辑`application.yml`设置数据库连接信息
- 配置AI模型API密钥（通义千问等）
- 配置腾讯云COS和SearchAPI（可选）

4. 构建与启动
```bash
# 后端服务构建
mvn clean package

# 运行后端服务
java -jar target/z-ai-agent-0.0.1-SNAPSHOT.jar

# 前端开发
cd z-ai-agent-frontend/z-ai-agent-frontend
npm install
npm run dev
```

## 💡 项目亮点

- **模块化设计**：分层架构设计，实现功能模块解耦，便于扩展和维护
- **多模型支持**：灵活切换不同AI大模型，适应不同场景需求
- **知识检索增强**：基于向量数据库的RAG实现，提升AI回答质量
- **智能体自主决策**：ReAct模式智能体可自主规划和执行复杂任务
- **丰富工具集成**：提供网络搜索、PDF生成、地图服务等多种工具
- **流式响应体验**：SSE实现流式响应，提升用户体验


