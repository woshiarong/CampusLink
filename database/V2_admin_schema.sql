-- 校园论坛管理员功能扩展脚本（在 init.sql 已执行基础上执行，或合并到 init 后整体初始化）
USE campus_forum;

-- 1. 用户表扩展：邮箱、最后登录时间（若已存在请注释对应行）
ALTER TABLE sys_user ADD COLUMN email VARCHAR(128) DEFAULT NULL AFTER nickname;
ALTER TABLE sys_user ADD COLUMN last_login_at DATETIME DEFAULT NULL AFTER updated_at;

-- 2. 版块表扩展：排序、版主、需审核、允许匿名
ALTER TABLE forum_board ADD COLUMN sort_order INT NOT NULL DEFAULT 0 AFTER description;
ALTER TABLE forum_board ADD COLUMN moderator_id BIGINT DEFAULT NULL AFTER sort_order;
ALTER TABLE forum_board ADD COLUMN need_approval TINYINT NOT NULL DEFAULT 0 AFTER moderator_id;
ALTER TABLE forum_board ADD COLUMN allow_anonymous TINYINT NOT NULL DEFAULT 0 AFTER need_approval;

-- 3. 帖子表扩展：置顶、加精
ALTER TABLE forum_post ADD COLUMN is_pinned TINYINT NOT NULL DEFAULT 0 AFTER status;
ALTER TABLE forum_post ADD COLUMN is_featured TINYINT NOT NULL DEFAULT 0 AFTER is_pinned;

-- 4. 管理员操作日志表
CREATE TABLE IF NOT EXISTS sys_admin_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  operator_id BIGINT NOT NULL,
  action VARCHAR(64) NOT NULL,
  target_type VARCHAR(32) DEFAULT NULL,
  target_id BIGINT DEFAULT NULL,
  detail VARCHAR(500) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_admin_log_operator (operator_id),
  INDEX idx_admin_log_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. 新增管理员权限（role_id=1 为管理员，已存在则忽略）
INSERT IGNORE INTO sys_role_permission (role_id, permission_code) VALUES
(1, 'user:list'), (1, 'user:update'), (1, 'user:resetPassword'), (1, 'user:assignRole'),
(1, 'board:list'), (1, 'board:save'), (1, 'board:delete'), (1, 'board:order'),
(1, 'post:adminList'), (1, 'post:pin'), (1, 'post:feature'), (1, 'post:adminDelete'), (1, 'post:move'),
(1, 'statistics:view'), (1, 'adminLog:view');

-- 6. 系统设置扩展项（若 key 已存在则跳过，需应用层或 REPLACE）
INSERT INTO sys_setting (setting_key, setting_value, description) VALUES
('forum.emailVerify', 'false', '注册是否需要邮箱验证'),
('forum.postMinLength', '10', '帖子最小字数限制'),
('forum.imageMaxSizeKb', '2048', '图片上传大小限制(KB)'),
('forum.sensitiveWordFilter', 'false', '敏感词过滤开关'),
('forum.sensitiveWords', '', '敏感词词库，逗号分隔')
ON DUPLICATE KEY UPDATE description = VALUES(description);
