package com.aih.zaiagent.app;

import cn.hutool.Hutool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class LoveAppTest {

    @Resource
    private LoveApp loveApp;

    @Test
    void doChat() {
        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doChat("你好，我是znq，我喜欢hjw", chatId);
        assertNotNull(answer);
        loveApp.doChat("我想让我和hjw的感情变得更好", chatId);
        loveApp.doChat("我应该注意些什么，如何做", chatId);
    }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好，我是znq,,我想我和我的女朋友是hjw更加相爱~我可以怎么做";
        LoveApp.LoveReport loveReport = loveApp.doChatWithReport(message, chatId);
        log.info("loveReport: {}", loveReport);
    }

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "我已经结婚了，但婚后关系不太亲密，怎么办？";
        String content = loveApp.doChatWithRag(message, chatId);
        log.info("content: {}", content);
    }

    @Test
    void doChatWithRagCloud() {
        String chatId = UUID.randomUUID().toString();
        String message = "我已经结婚了，但婚后关系不太亲密，怎么办？";
        String content = loveApp.doChatWithRagCloud(message, chatId);
        log.info("content: {}", content);
    }

    @Test
    void doChatWithTools() {
        // 测试联网搜索问题的答案
        // testMessage("周末想带女朋友去上海约会，推荐几个适合情侣的小众打卡地？"); // xiao'z
        //
        // // 测试网页抓取：恋爱案例分析
        // testMessage("最近和对象吵架了，看看其他网站的其他情侣是怎么解决矛盾的？");
        //
        // // 测试资源下载：图片下载
        testMessage("直接下载一张适合做电脑桌面壁纸的永劫无间顾倾寒热门图片为文件");
        //
        // // 测试终端操作：执行代码
        // testMessage("执行 Python3 脚本来生成打印三角形");
        //
        // // 测试文件操作：保存用户档案
        // testMessage("保存我的恋爱档案为文件");

        // 测试 PDF 生成
        testMessage("生成一份‘七夕约会计划’PDF，包含餐厅预订、活动流程和礼物清单");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doChatWithTools(message, chatId);
        assertNotNull(answer);
        log.info("message: {}, answer: {}", message, answer);
    }

    @Test
    void doChatWithMcp() {
        String chatId = UUID.randomUUID().toString();
        // 测试地图 MCP
        // String message = "我的另一半居住在广东白云区，请帮我找到 10 公里内合适的约会地点，请直接推荐我几个地点参考，不要再问我其他内容";
        // String answer =  loveApp.doChatWithMcp(message, chatId);

        // 测试图片搜索 MCP
        String message = "帮我搜索一些哄另一半开心的图片";
        String answer =  loveApp.doChatWithMcp(message, chatId);
        assertNotNull(answer);
        log.info("message: {}, answer: {}", message, answer);
    }
}
