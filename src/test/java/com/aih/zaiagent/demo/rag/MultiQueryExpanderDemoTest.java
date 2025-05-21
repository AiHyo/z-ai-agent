package com.aih.zaiagent.demo.rag;

import cn.hutool.core.lang.Assert;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.ai.rag.Query;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class MultiQueryExpanderDemoTest {

    @Resource
    private MultiQueryExpanderDemo multiQueryExpanderDemo;

    @Test
    void expand() {
        List<Query> queries = multiQueryExpanderDemo.expand("你知道github的AiH是谁吗呀，你认识他吗哈哈哈哈哈？");
        log.info("queries: {}", queries);
        // [Query[text=你知道github的AiH是谁吗呀，你认识他吗哈哈哈哈哈？, history=[], context={}],
        // Query[text=谁是GitHub上的AiH，你能告诉我吗？  , history=[], context={}],
        // Query[text=在GitHub社区中，有没有一个叫AiH的人比较出名？  , history=[], context={}],
        // Query[text=关于GitHub用户AiH的信息有哪些，你知道他的项目或贡献吗？, history=[], context={}]]
        assertNotNull(queries);
    }
}
