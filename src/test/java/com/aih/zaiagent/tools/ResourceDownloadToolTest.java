package com.aih.zaiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceDownloadToolTest {

    @Test
    void downloadResource() {
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        String url = "https://gitee.com/jiuxiaotu/Timo/raw/master/my1-2023-9-1609:23:53.jpg";
        String fileName = "avatar.jpg";
        String result = resourceDownloadTool.downloadResource(url, fileName);
        System.out.println(result);
        assertTrue(result.contains("Resource downloaded successfully"));
    }
}
