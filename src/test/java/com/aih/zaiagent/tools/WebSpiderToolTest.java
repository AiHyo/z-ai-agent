package com.aih.zaiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebSpiderToolTest {

    @Test
    void scrapeWebPage() {
        WebSpiderTool webSpiderTool = new WebSpiderTool();
        String url = "https://www.zhihu.com/";
        String res = webSpiderTool.scrapeWebPage(url);
        System.out.println(res);
        assertNotNull(res);
    }
}
