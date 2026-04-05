CREATE DATABASE IF NOT EXISTS campus_forum DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE campus_forum;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS forum_message;
DROP TABLE IF EXISTS forum_favorite;
DROP TABLE IF EXISTS forum_post_like;
DROP TABLE IF EXISTS forum_comment;
DROP TABLE IF EXISTS sys_admin_log;
DROP TABLE IF EXISTS forum_moderation_log;
DROP TABLE IF EXISTS forum_report;
DROP TABLE IF EXISTS forum_post_attachment;
DROP TABLE IF EXISTS forum_post_tag;
DROP TABLE IF EXISTS forum_post;
DROP TABLE IF EXISTS forum_board;
DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_setting;

CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  nickname VARCHAR(64) NOT NULL,
  email VARCHAR(128) DEFAULT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  last_login_at DATETIME DEFAULT NULL,
  avatar VARCHAR(500) DEFAULT NULL,
  bio VARCHAR(500) DEFAULT NULL,
  credit INT NOT NULL DEFAULT 0,
  INDEX idx_user_username (username),
  INDEX idx_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(64) NOT NULL UNIQUE,
  name VARCHAR(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_user_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  UNIQUE KEY uk_user_role (user_id, role_id),
  INDEX idx_user_role_user (user_id),
  INDEX idx_user_role_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_role_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  permission_code VARCHAR(128) NOT NULL,
  UNIQUE KEY uk_role_permission (role_id, permission_code),
  INDEX idx_role_permission_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE forum_board (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(255) DEFAULT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  moderator_id BIGINT DEFAULT NULL,
  need_approval TINYINT NOT NULL DEFAULT 0,
  allow_anonymous TINYINT NOT NULL DEFAULT 0,
  INDEX idx_board_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE forum_post (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  board_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  content TEXT NOT NULL,
  status VARCHAR(32) NOT NULL,
  is_pinned TINYINT NOT NULL DEFAULT 0,
  is_featured TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_post_user (user_id),
  INDEX idx_post_board (board_id),
  INDEX idx_post_status (status),
  INDEX idx_post_pinned (is_pinned),
  FULLTEXT INDEX idx_post_title_content (title, content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE forum_post_tag (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  tag_name VARCHAR(64) NOT NULL,
  INDEX idx_post_tag_post (post_id),
  INDEX idx_post_tag_name (tag_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE forum_post_attachment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  file_url VARCHAR(500) NOT NULL,
  INDEX idx_attachment_post (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE forum_report (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  reporter_id BIGINT NOT NULL,
  reason VARCHAR(500) NOT NULL,
  status VARCHAR(32) NOT NULL,
  review_remark VARCHAR(500) DEFAULT NULL,
  reviewer_id BIGINT DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_report_post (post_id),
  INDEX idx_report_reporter (reporter_id),
  INDEX idx_report_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE forum_moderation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  report_id BIGINT NOT NULL,
  action VARCHAR(64) NOT NULL,
  operator_id BIGINT NOT NULL,
  detail VARCHAR(500) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_moderation_report (report_id),
  INDEX idx_moderation_operator (operator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_setting (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  setting_key VARCHAR(128) NOT NULL UNIQUE,
  setting_value VARCHAR(1000) NOT NULL,
  description VARCHAR(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_admin_log (
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

CREATE TABLE forum_post_like (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_like (post_id, user_id),
  INDEX idx_like_post (post_id),
  INDEX idx_like_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE forum_favorite (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  post_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_favorite (user_id, post_id),
  INDEX idx_favorite_user (user_id),
  INDEX idx_favorite_post (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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

INSERT INTO sys_role (id, code, name) VALUES
(1, 'administrator', '管理员'),
(2, 'student', '学生');

INSERT INTO sys_user (id, username, password, nickname, status) VALUES
(1, 'admin', '123456', '系统管理员', 1),
(2, 'student', '123456', '示例学生', 1);

INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2);

INSERT INTO sys_role_permission (role_id, permission_code) VALUES
(1, 'auth:profile'),
(1, 'auth:logout'),
(1, 'post:view'),
(1, 'post:mine'),
(1, 'post:create'),
(1, 'post:submit'),
(1, 'post:update'),
(1, 'post:interact'),
(1, 'report:create'),
(1, 'moderation:review'),
(1, 'settings:view'),
(1, 'settings:update'),
(1, 'user:list'),
(1, 'user:update'),
(1, 'user:resetPassword'),
(1, 'user:assignRole'),
(1, 'board:list'),
(1, 'board:save'),
(1, 'board:delete'),
(1, 'board:order'),
(1, 'post:adminList'),
(1, 'post:pin'),
(1, 'post:feature'),
(1, 'post:adminDelete'),
(1, 'post:move'),
(1, 'statistics:view'),
(1, 'adminLog:view'),
(2, 'auth:profile'),
(2, 'auth:logout'),
(2, 'post:view'),
(2, 'post:mine'),
(2, 'post:create'),
(2, 'post:submit'),
(2, 'post:update'),
(2, 'post:interact'),
(2, 'report:create');

INSERT INTO forum_board (id, name, description, sort_order) VALUES
(1, '校园生活', '校园日常分享与交流', 0),
(5, '实习与面经', '实习经验、笔试与面试交流', 1),
(9, '课程讨论', '课程学习与项目实践交流', 2);

INSERT INTO forum_post (id, user_id, board_id, title, content, status, is_pinned, is_featured) VALUES
(1, 2, 5, '某大厂暑期实习三轮技术面经', '<p>第一轮问了项目...第二轮算法题是...</p>', 'VIEWABLE', 0, 0);

INSERT INTO forum_post_tag (post_id, tag_name) VALUES
(1, '面试'),
(1, '实习'),
(1, 'Java');

INSERT INTO forum_post_attachment (post_id, file_name, file_url) VALUES
(1, 'code.png', 'https://cdn.example.com/xxx.png');

INSERT INTO sys_setting (setting_key, setting_value, description) VALUES
('forum.allowRegister', 'true', '是否允许新用户注册'),
('forum.postAutoPublish', 'true', '帖子提交后是否自动发布'),
('forum.maxAttachments', '5', '每个帖子最大附件数量'),
('forum.emailVerify', 'false', '注册是否需要邮箱验证'),
('forum.postMinLength', '10', '帖子最小字数限制'),
('forum.imageMaxSizeKb', '2048', '图片上传大小限制(KB)'),
('forum.sensitiveWordFilter', 'false', '敏感词过滤开关'),
('forum.sensitiveWords', '', '敏感词词库，逗号分隔');

SET FOREIGN_KEY_CHECKS = 1;


-- 补充常用版块
INSERT IGNORE INTO forum_board (id, name, description, sort_order) VALUES
(1, '校园生活', '校园日常分享与交流', 0),
(2, '二手交易', '书籍、电动车等二手物品交易区', 3),
(3, '失物招领', '拾到 / 丢失物品信息', 4),
(4, '校园活动', '社团招新、比赛、讲座等活动信息', 5),
(5, '实习与面经', '实习经验、笔试与面试交流', 1),
(9, '课程讨论', '课程学习与项目实践交流', 2);

-- 20 条示例帖子
INSERT INTO forum_post (id, user_id, board_id, title, content, status, is_pinned, is_featured) VALUES
(101, 2, 5, '某大厂暑期实习三轮技术面经',
 '<p>今年暑期参加了某大厂后端实习面试，一共三轮技术 + 一轮 HR。\
 第一轮主要问项目细节和基础知识，如 JVM 内存模型、HashMap 实现等；\
 第二轮以算法为主，现场手写两道中等难度题；第三轮注重系统设计和协作经历。\
 这里整理了详细问题和我的回答思路，供大家参考。</p>', 'VIEWABLE', 1, 1),
(102, 2, 2, '九成新《深入理解Java虚拟机》带详细笔记',
 '<p>转让正版《深入理解Java虚拟机》第三版，九成新，没有划痕和涂改。\
 书里贴满了我整理的便利贴笔记，适合准备校招 / 考研的同学。\
 支持当面交易，地点在图书馆一楼大厅。</p>', 'VIEWABLE', 0, 0),
(103, 2, 9, '动态规划到底怎么学？',
 '<p>最近在刷 LeetCode，总是被动态规划题卡住。\
 求推荐一些系统学习 DP 的资料或者课程，比如经典题目分类、状态定义经验等。\
 欢迎大家分享自己的学习路线。</p>', 'VIEWABLE', 0, 0),
(104, 2, 2, '求购一台 24 寸显示器，预算 500 元以内',
 '<p>想给宿舍配一台二手 24 寸显示器，看网课和写代码用。\
 预算 500 元以内，最好是 IPS 面板，带 HDMI 接口。\
 有出闲置显示器的同学可以留言或者私信我。</p>', 'VIEWABLE', 0, 0),
(105, 2, 4, '黑客马拉松报名组队中！',
 '<p>学校下个月要举办 48 小时黑客马拉松，现在想组一支 4 人队伍，\
 方向偏后端 + 数据可视化，欢迎前端 / 设计 / 产品同学加入。\
 有兴趣的同学可以留言自我介绍一下。</p>', 'VIEWABLE', 1, 1),
(106, 2, 3, '失物招领：一卡通在二教 205',
 '<p>今天下午在二教 205 教室捡到一张学生一卡通，\
 上面名字似乎是“张*婷”，已经交到二教一楼服务台。\
 请失主带证件前往认领。</p>', 'VIEWABLE', 0, 0),
(107, 2, 9, '机器学习入门路线图分享',
 '<p>整理了一份适合本科同学的机器学习入门路线，\
 从线性代数、概率论开始，到 python 基础、sklearn 实战，再到简单的深度学习。\
 文末附上了我用过的一些公开课和资料链接。</p>', 'VIEWABLE', 0, 1),
(108, 2, 1, '宿舍深夜断电怎么办？',
 '<p>最近经常在宿舍熬夜写作业，发现半夜突然断电导致电脑没电关机。\
 想问下大家有没有什么解决办法，比如申请临时用电、移动电源推荐等。</p>', 'VIEWABLE', 0, 0),
(109, 2, 2, '二手电动车转让，骑了半年',
 '<p>毕业了准备出校，转让一辆九成新电动车，骑了大概半年，\
 续航还能跑三十多公里，有正规发票和上牌。\
 有意者可以私信或者在评论区联系。</p>', 'VIEWABLE', 0, 0),
(110, 2, 5, '腾讯后端开发一二三面经',
 '<p>分享一下今年春招拿到腾讯某业务线后端实习 offer 的面经。\
 包括每一面的具体问题、在线笔试题型、以及 HR 面谈注意事项。\
 欢迎一起交流简历和项目。</p>', 'VIEWABLE', 0, 1),
(111, 2, 1, '食堂新品测评：麻辣香锅好吃吗？',
 '<p>中午去一食堂试了新上的麻辣香锅，整体味道还不错，菜品也比较新鲜。\
 就是价格稍微偏高了一点，如果人多一起拼盘会划算很多。\
 大家还有什么推荐的隐藏菜单吗？</p>', 'VIEWABLE', 0, 0),
(112, 2, 9, '考研英语 85 分经验分享',
 '<p>今年考研英语一考了 85 分，这里分享一下我的备考经验：\
 单词、真题、作文模板以及模拟卷选择。\
 特别是翻译和完形的做题顺序，对提升整体正确率很有帮助。</p>', 'VIEWABLE', 0, 1),
(113, 2, 9, '求推荐一款好用的 C++ IDE',
 '<p>目前在用 VSCode 写 C++，但配置调试环境有点麻烦。\
 想问问大家有没有更适合新手的 C++ IDE，比如 CLion、VS 等，\
 欢迎推荐并顺便说说优缺点。</p>', 'VIEWABLE', 0, 0),
(114, 2, 1, '学校健身房开放时间调整通知',
 '<p>教务处发布通知：从下周开始，学校健身房开放至晚 22:30，\
 周末也将开放上午时段。\
 具体规则请同学们以公告栏为准。</p>', 'VIEWABLE', 0, 0),
(115, 2, 2, '出全新《算法导论》第四版',
 '<p>误买了一本《算法导论》第四版英文原版，\
 仅拆封未使用，现低价转让，有需要的同学可以留言。\
 支持当面看书验货。</p>', 'VIEWABLE', 0, 0),
(116, 2, 5, '暑期科研实习机会内推',
 '<p>导师那边有一个暑期科研实习名额，方向是图像处理 + 深度学习。\
 需要有一定 Python 基础和机器学习入门知识。\
 有兴趣的同学可以带上简历私信我。</p>', 'VIEWABLE', 0, 1),
(117, 2, 4, '摄影社招新啦！',
 '<p>校园摄影社本周末举办招新说明会，欢迎喜欢拍照、修图的小伙伴加入。\
 入社后会定期组织外拍、作品分享和摄影比赛。</p>', 'VIEWABLE', 0, 0),
(118, 2, 3, '急寻：黑色钱包丢失在三食堂',
 '<p>昨晚在三食堂吃完饭后发现黑色钱包不见了，里面有一卡通和少量现金，\
 若有同学拾到请联系我，非常感谢！</p>', 'VIEWABLE', 0, 0),
(119, 2, 1, '关于选课系统的吐槽',
 '<p>每次选课系统一开放就卡死，抢课体验非常差。\
 想问问大家有没有什么抢课小技巧，或者有没有同学愿意一起向学院反馈改进建议。</p>', 'VIEWABLE', 0, 0),
(120, 2, 2, '拼单：一起买实验器材更划算',
 '<p>做实验需要买一批耗材，发现某宝上满 200 减 40，\
 想问有没有同专业的同学一起拼单购买，大家都能省点钱。</p>', 'VIEWABLE', 0, 0);

-- 标签
INSERT INTO forum_post_tag (post_id, tag_name) VALUES
(101, '面试'), (101, 'Java'), (101, '算法'),
(102, '书籍'), (102, 'Java'),
(103, '算法'), (103, '求助'),
(104, '显示器'), (104, '求购'),
(105, '比赛'), (105, '组队'),
(106, '一卡通'),
(107, '机器学习'), (107, '资料'),
(108, '求助'),
(109, '电动车'),
(110, '后端'), (110, '腾讯'),
(111, '美食'),
(112, '考研'), (112, '英语'),
(113, 'C++'), (113, '求助'),
(114, '通知'),
(115, '书籍'), (115, '算法'),
(116, '实习'), (116, '内推'),
(117, '社团'),
(118, '钱包'),
(119, '选课'),
(120, '拼单');