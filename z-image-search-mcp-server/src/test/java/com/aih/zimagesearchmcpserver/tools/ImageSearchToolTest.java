package com.aih.zimagesearchmcpserver.tools;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ImageSearchToolTest {

    @Resource
    private ImageSearchTool imageSearchTool;

    @Test
    void searchImage() {
        String searchImage = imageSearchTool.searchImage("computer");
        assertNotNull(searchImage);
    }
}
