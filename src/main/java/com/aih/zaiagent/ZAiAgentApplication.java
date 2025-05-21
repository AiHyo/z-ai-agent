package com.aih.zaiagent;

import com.aih.zaiagent.rag.PgVectorVectorStoreConfig;
import org.springframework.ai.autoconfigure.vectorstore.pgvector.PgVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author AiHyo
 */
@SpringBootApplication(exclude = PgVectorStoreAutoConfiguration.class)
public class ZAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZAiAgentApplication.class, args);
    }

}
