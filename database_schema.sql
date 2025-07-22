-- 机器人数据表结构
-- 基于您的实际数据表结构

-- 用户信息表
CREATE TABLE IF NOT EXISTS joy_profile (
    user_id        BIGINT(20) UNSIGNED NOT NULL COMMENT '用户ID',
    open_id        VARCHAR(100) NOT NULL COMMENT 'wx open_id',
    robot_id       VARCHAR(200) NOT NULL DEFAULT '' COMMENT 'ai 智能体id 不为空即为机器人',
    nick_name      VARCHAR(100) COMMENT '用户昵称',
    phone          VARCHAR(100) COMMENT '手机号',
    sex            INT COMMENT '性别',
    avatar         VARCHAR(200) COMMENT '头像地址',
    birthday       INT COMMENT '生日',
    emotion_status INT COMMENT '情感状态',
    work_status    INT COMMENT '工作状态',
    verify_status  INT COMMENT '认证状态',
    hometown       VARCHAR(100) COMMENT '家乡',
    location       VARCHAR(100) COMMENT '所在地',
    company        VARCHAR(100) COMMENT '公司',
    carrier        VARCHAR(100) COMMENT '职业',
    status         INT COMMENT '状态',
    create_time    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (user_id),
    UNIQUE INDEX (open_id)
) ENGINE = INNODB COMMENT = '用户信息表';

-- 照片表
CREATE TABLE IF NOT EXISTS joy_photo (
    id          BIGINT NOT NULL COMMENT '照片ID',
    user_id     BIGINT NOT NULL COMMENT '用户ID',
    album_id    BIGINT NOT NULL COMMENT '相册ID',
    title       VARCHAR(100) COMMENT '标题',
    description VARCHAR(1000) COMMENT '描述',
    url         VARCHAR(1000) NOT NULL COMMENT '照片地址',
    status      INT NOT NULL COMMENT '图片状态',
    tag         VARCHAR(100) COMMENT '标签',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX (user_id, album_id),
    INDEX (user_id, tag)
) ENGINE = INNODB COMMENT = '照片表'; 