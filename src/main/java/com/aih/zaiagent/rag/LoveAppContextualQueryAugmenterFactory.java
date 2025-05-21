package com.aih.zaiagent.rag;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;

/**
 * 工厂类：上下文查询增强器
 * @author AiHyo
 */
public class LoveAppContextualQueryAugmenterFactory {

    // 上下文查询增强器的创建方法
    public static ContextualQueryAugmenter createInstance() {
        // 创建模板
        PromptTemplate emptyContextPromptTemplate = new PromptTemplate("""
                你应该输出下面的内容：
                抱歉，我只能回答恋爱相关的问题，别的没办法帮到您哦
                """);
        // 创建上下文查询增强器，当上下文为空时，使用对应模板进行处理
        return ContextualQueryAugmenter.builder()
                .allowEmptyContext(false)
                .emptyContextPromptTemplate(emptyContextPromptTemplate)
                .build();
    }
}
