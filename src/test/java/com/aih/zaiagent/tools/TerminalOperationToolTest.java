package com.aih.zaiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TerminalOperationToolTest {

    @Test
    void executeTerminalCommand() {
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        // String command = "echo Hello, World!";
        String command = "dir";
        String result = terminalOperationTool.executeTerminalCommand(command);
        System.out.println(result);
        assertNotNull(result);
    }
}
