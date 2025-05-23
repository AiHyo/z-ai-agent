package com.aih.zaiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PDFGenerationToolTest {

    @Test
    void generatePDF() {
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        String content = "This is a test PDF content. 中文中文支持测试 ";
        String fileName = "test.pdf";
        String result = pdfGenerationTool.generatePDF(fileName, content);
        System.out.println(result);
        assertTrue(result.contains("PDF generated successfully"));
    }
}
