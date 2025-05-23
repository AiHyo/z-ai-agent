package com.aih.zaiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileOperationToolTest {

    @Test
    void readFile() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        String fileName = "test.txt";
        String res = fileOperationTool.readFile(fileName);
        System.out.println(res);
        assertNotNull(res);
    }

    @Test
    void writeFile() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        String fileName = "test.txt";
        String content = "Hello, World!";
        String res = fileOperationTool.writeFile(fileName, content);
        assertNotNull(res);
        this.readFile();
    }
}
