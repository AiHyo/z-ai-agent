package com.aih.zaiagent.app;

import cn.hutool.Hutool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
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
}
