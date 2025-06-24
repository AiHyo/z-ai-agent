package com.aih.zaiagent.tools;

import com.aih.zaiagent.service.TencentCosService;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * pdf生成工具
 * @author AiHyo
 */
@Slf4j
@Component
public class PDFGenerationTool {

    @Resource
    private TencentCosService tencentCosService;

    private static final String COS_DIRECTORY = "pdf";

    @Tool(description = "Generate a PDF file with given content", returnDirect = false)
    public String generatePDF(
            @ToolParam(description = "Name of the file to save the generated PDF") String fileName,
            @ToolParam(description = "Content to be included in the PDF") String content) {
        try {
            // 创建临时文件
            File tempFile = File.createTempFile("pdf", ".pdf");
            String tempFilePath = tempFile.getAbsolutePath();

            // 创建 PdfWriter 和 PdfDocument 对象
            try (PdfWriter writer = new PdfWriter(tempFilePath);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {
                // 自定义字体（可以切换字体）
                String fontPath = Paths.get("src/main/resources/static/fonts/dingliezhuhaifont.ttf")
                        .toAbsolutePath().toString();
                PdfFont font = PdfFontFactory.createFont(fontPath,
                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                document.setFont(font);
                // 创建段落
                log.info("content will be added to pdf: {}", content);
                Paragraph paragraph = new Paragraph(content);
                // 添加段落并关闭文档
                document.add(paragraph);
            }

            // 上传到腾讯云COS
            // 确保文件名以.pdf结尾
            if (!fileName.toLowerCase().endsWith(".pdf")) {
                fileName = fileName + ".pdf";
            }
            String fileUrl = tencentCosService.uploadFile(fileName, tempFile, COS_DIRECTORY);
            String fileKey = COS_DIRECTORY + "/" + fileName;

            // 删除临时文件
            tempFile.delete();

            return "PDF generated successfully. Access URL: " + fileUrl;
        } catch (IOException e) {
            return "Error generating PDF: " + e.getMessage();
        }
    }
}
