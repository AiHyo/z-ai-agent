package com.aih.zaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;

/**
 * 终止工具（用于结束交互）
 * @author AiHyo
 */
public class TerminateTool {
    @Tool(description = """
            Terminate the interaction when the request is met OR if the assistant cannot proceed further with the task.
            "When you have finished all the tasks, call this tool to end the work.
            """)
    public String doTerminate() {
        return "任务结束";
    }
}
