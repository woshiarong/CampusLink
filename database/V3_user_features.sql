-- 用户端功能扩展：评论、点赞、收藏、用户资料、消息、积分
USE campus_forum;

-- 1. 用户表扩展：头像、简介
ALTER TABLE sys_user ADD COLUMN avatar VARCHAR(500) DEFAULT NULL AFTER email;
ALTER TABLE sys_user ADD COLUMN bio VARCHAR(500) DEFAULT NULL AFTER avatar;
ALTER TABLE sys_user ADD COLUMN credit INT NOT NULL DEFAULT 0 AFTER bio;

-- 2. 评论表
CREATE TABLE forum_comment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  parent_id BIGINT DEFAULT NULL,
  content TEXT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_comment_post (post_id),
  INDEX idx_comment_user (user_id),
  INDEX idx_comment_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 帖子点赞表
CREATE TABLE forum_post_like (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_like (post_id, user_id),
  INDEX idx_like_post (post_id),
  INDEX idx_like_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. 收藏表
CREATE TABLE forum_favorite (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  post_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_favorite (user_id, post_id),
  INDEX idx_favorite_user (user_id),
  INDEX idx_favorite_post (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. 消息通知表（可选）
CREATE TABLE forum_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  type VARCHAR(32) NOT NULL,
  title VARCHAR(200) NOT NULL,
  content VARCHAR(1000) DEFAULT NULL,
  ref_id BIGINT DEFAULT NULL,
  is_read TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_message_user (user_id),
  INDEX idx_message_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
