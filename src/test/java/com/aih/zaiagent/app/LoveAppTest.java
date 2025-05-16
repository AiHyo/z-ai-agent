package com.aih.zaiagent.app;

import cn.hutool.Hutool;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
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
        String message = "你好，我是znq,我的女朋友是hjw,我想让我们更加相爱~我可以怎么做";
        LoveApp.LoveReport loveReport = loveApp.doChatWithReport(message, chatId);
        System.out.println(loveReport);

    }
}
