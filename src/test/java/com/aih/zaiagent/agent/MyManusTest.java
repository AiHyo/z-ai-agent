package com.aih.zaiagent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MyManusTest {

    @Resource
    private MyManus myManus;

    @Test
    public void test(){
        String userPrompt = """
                我的女朋友住在广东白云区，请帮我推荐几个5公里内的约会地点，
                并结合一些网络图片，制定一份详细的约会计划，并以PDF格式输出。
                """;
        String result = myManus.run(userPrompt);
        System.out.println("执行结果: " + result);
        assertNotNull(result);
    }


}
