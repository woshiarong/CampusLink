package com.campus.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.forum.dto.CommentCreateRequest;
import com.campus.forum.entity.Comment;
import com.campus.forum.entity.Post;
import com.campus.forum.entity.SystemSetting;
import com.campus.forum.entity.User;
import com.campus.forum.mapper.CommentMapper;
import com.campus.forum.mapper.PostMapper;
import com.campus.forum.mapper.SystemSettingMapper;
import com.campus.forum.mapper.UserMapper;
import com.campus.forum.utils.BusinessException;
import com.campus.forum.vo.CommentVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final SystemSettingMapper systemSettingMapper;
    private final MessageService messageService;

    public CommentService(CommentMapper commentMapper,
                           UserMapper userMapper,
                           PostMapper postMapper,
                           SystemSettingMapper systemSettingMapper,
                           MessageService messageService) {
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
        this.postMapper = postMapper;
        this.systemSettingMapper = systemSettingMapper;
        this.messageService = messageService;
    }

    public long countByPostId(Long postId) {
        return commentMapper.selectCount(new LambdaQueryWrapper<Comment>().eq(Comment::getPostId, postId));
    }

    public List<CommentVO> listByPostId(Long postId) {
        List<Comment> all = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>().eq(Comment::getPostId, postId).orderByAsc(Comment::getCreatedAt));
        if (all.isEmpty()) return List.of();
        List<Long> userIds = all.stream().map(Comment::getUserId).distinct().toList();
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));
        List<CommentVO> roots = new ArrayList<>();
        List<CommentVO> children = new ArrayList<>();
        for (Comment c : all) {
            CommentVO vo = toVO(c, userMap);
            if (c.getParentId() == null) {
                vo.setReplies(new ArrayList<>());
                roots.add(vo);
            } else {
                children.add(vo);
            }
        }
        for (CommentVO child : children) {
            roots.stream().filter(r -> r.getId().equals(child.getParentId())).findFirst()
                    .ifPresent(r -> r.getReplies().add(child));
        }
        return roots;
    }

    @Transactional(rollbackFor = Exception.class)
    public CommentVO create(Long userId, CommentCreateRequest request) {
        Post post = postMapper.selectById(request.getPostId());
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }

        validateSensitiveWords(request.getContent());

        Comment c = new Comment();
        c.setPostId(request.getPostId());
        c.setUserId(userId);
        c.setParentId(request.getParentId());
        c.setContent(request.getContent());
        commentMapper.insert(c);
        User u = userMapper.selectById(userId);
        Map<Long, User> userMap = new HashMap<>();
        userMap.put(userId, u);
        CommentVO vo = toVO(c, userMap);
        vo.setReplies(List.of());
        String actorName = u != null ? (u.getNickname() != null ? u.getNickname() : u.getUsername()) : "有用户";
        // 回复评论：通知被回复的人；普通评论：通知帖子作者（都排除自己给自己通知）
        if (request.getParentId() != null) {
            Comment parent = commentMapper.selectById(request.getParentId());
            if (parent != null && parent.getUserId() != null && !parent.getUserId().equals(userId)) {
                String title = "你的评论有新回复";
                String content = actorName + " 回复了你的评论：" + request.getContent();
                messageService.create(parent.getUserId(), "REPLY", title, content, request.getPostId());
            }
        } else if (post.getUserId() != null && !post.getUserId().equals(userId)) {
            String title = "你的帖子有新评论";
            String content = actorName + " 评论了你的帖子《" + (post.getTitle() == null ? "" : post.getTitle()) + "》";
            messageService.create(post.getUserId(), "COMMENT", title, content, request.getPostId());
        }
        return vo;
    }

    private CommentVO toVO(Comment c, Map<Long, User> userMap) {
        CommentVO vo = new CommentVO();
        vo.setId(c.getId());
        vo.setPostId(c.getPostId());
        vo.setUserId(c.getUserId());
        vo.setParentId(c.getParentId());
        vo.setContent(c.getContent());
        vo.setCreatedAt(c.getCreatedAt());
        User u = userMap.get(c.getUserId());
        vo.setUserName(u != null ? u.getNickname() : "匿名");
        vo.setUserAvatar(u != null ? u.getAvatar() : null);
        return vo;
    }

    private void validateSensitiveWords(String content) {
        SystemSetting filterSwitch = systemSettingMapper.selectOne(new LambdaQueryWrapper<SystemSetting>()
                .eq(SystemSetting::getSettingKey, "forum.sensitiveWordFilter")
                .last("limit 1"));
        boolean enabled = filterSwitch != null
                && "true".equalsIgnoreCase(String.valueOf(filterSwitch.getSettingValue()).trim());
        if (!enabled) return;

        SystemSetting wordsSetting = systemSettingMapper.selectOne(new LambdaQueryWrapper<SystemSetting>()
                .eq(SystemSetting::getSettingKey, "forum.sensitiveWords")
                .last("limit 1"));
        String raw = wordsSetting == null ? "" : String.valueOf(wordsSetting.getSettingValue());
        if (raw == null || raw.isBlank()) return;

        String text = (content == null ? "" : content).toLowerCase();
        List<String> hitWords = new ArrayList<>();
        for (String token : raw.split(",")) {
            String word = token == null ? "" : token.trim();
            if (word.isBlank()) continue;
            if (text.contains(word.toLowerCase()) && !hitWords.contains(word)) {
                hitWords.add(word);
            }
        }
        if (!hitWords.isEmpty()) {
            throw new BusinessException("评论内容包含敏感词：" + String.join("、", hitWords) + "，请修改后再发布");
        }
    }
}
