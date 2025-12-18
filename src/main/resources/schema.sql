-- 用户表
CREATE TABLE IF NOT EXISTS app_user
(
    id            SERIAL PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,
    nickname      VARCHAR(50),
    avatar        VARCHAR(255),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    is_deleted    BOOLEAN   DEFAULT FALSE
);
-- 会话表
CREATE TABLE IF NOT EXISTS conversation
(
    id              VARCHAR(50) PRIMARY KEY, -- 使用UUID或自定义格式
    user_id         INTEGER NOT NULL REFERENCES app_user (id),
    title           VARCHAR(255),            -- 会自动从第一条消息生成
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_message_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    group_id        BIGINT    DEFAULT NULL,  -- 分组ID，NULL表示不属于任何分组
    is_deleted      BOOLEAN   DEFAULT FALSE,
    message_count   INTEGER DEFAULT 0,       -- 消息数量
    ai_type         VARCHAR(20)              -- AI类型："love-app"或"manus"
);
-- 消息表
CREATE TABLE IF NOT EXISTS message
(
    id              SERIAL PRIMARY KEY,
    conversation_id VARCHAR(50) NOT NULL REFERENCES conversation (id),
    is_user         BOOLEAN     NOT NULL, -- true为用户消息，false为AI消息
    content         TEXT        NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- 索引
CREATE INDEX IF NOT EXISTS idx_conversation_user_id ON conversation(user_id);
CREATE INDEX IF NOT EXISTS idx_conversation_group_id ON conversation(group_id);
CREATE INDEX IF NOT EXISTS idx_message_conversation_id ON message(conversation_id);
CREATE INDEX IF NOT EXISTS idx_message_created_at ON message(created_at);
CREATE INDEX IF NOT EXISTS idx_conversation_ai_type ON conversation(ai_type);
-- 会话分组表
CREATE TABLE IF NOT EXISTS conversation_group
(
    id BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    user_id    BIGINT       NOT NULL,
    ai_type    VARCHAR(20),            -- AI类型："love-app"或"manus"，便于前端按类型过滤
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN     NOT NULL DEFAULT 0
);
-- 外键约束
ALTER TABLE conversation
    ADD CONSTRAINT fk_conversation_group
        FOREIGN KEY (group_id) REFERENCES conversation_group (id)
            ON DELETE SET NULL;

-- 添加列（如果不存在）
-- ALTER TABLE conversation_group ADD COLUMN IF NOT EXISTS ai_type VARCHAR(20);
-- ALTER TABLE conversation ADD COLUMN IF NOT EXISTS ai_type VARCHAR(20);
-- 修改列空值（已有数据的情况下）
-- UPDATE conversation_group SET ai_type = 'love-app' WHERE ai_type IS NULL AND id IN (SELECT g.id FROM conversation_group g JOIN conversation c ON g.id = c.group_id WHERE c.ai_type = 'love-app');
-- UPDATE conversation_group SET ai_type = 'manus' WHERE ai_type IS NULL AND id IN (SELECT g.id FROM conversation_group g JOIN conversation c ON g.id = c.group_id WHERE c.ai_type = 'manus');