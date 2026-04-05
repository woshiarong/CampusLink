package com.campus.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.forum.entity.Post;
import com.campus.forum.entity.PostLike;
import com.campus.forum.entity.User;
import com.campus.forum.mapper.PostMapper;
import com.campus.forum.mapper.PostLikeMapper;
import com.campus.forum.mapper.UserMapper;
import com.campus.forum.utils.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeService {

    private final PostLikeMapper postLikeMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final MessageService messageService;

    public LikeService(PostLikeMapper postLikeMapper, PostMapper postMapper, UserMapper userMapper, MessageService messageService) {
        this.postLikeMapper = postLikeMapper;
        this.postMapper = postMapper;
        this.userMapper = userMapper;
        this.messageService = messageService;
    }

    public long countByPostId(Long postId) {
        return postLikeMapper.selectCount(new LambdaQueryWrapper<PostLike>().eq(PostLike::getPostId, postId));
    }

    public boolean hasLiked(Long postId, Long userId) {
        if (userId == null) return false;
        return postLikeMapper.selectCount(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getPostId, postId).eq(PostLike::getUserId, userId)) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean toggle(Long postId, Long userId) {
        if (userId == null) throw new BusinessException("请先登录");
        LambdaQueryWrapper<PostLike> q = new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getPostId, postId).eq(PostLike::getUserId, userId);
        PostLike existing = postLikeMapper.selectOne(q);
        if (existing != null) {
            postLikeMapper.deleteById(existing.getId());
            return false;
        }
        PostLike like = new PostLike();
        like.setPostId(postId);
        like.setUserId(userId);
        postLikeMapper.insert(like);
        // 点赞成功后通知帖子作者（自己给自己点赞不通知）
        Post post = postMapper.selectById(postId);
        if (post != null && post.getUserId() != null && !post.getUserId().equals(userId)) {
            User actor = userMapper.selectById(userId);
            String actorName = actor != null
                    ? (actor.getNickname() != null ? actor.getNickname() : actor.getUsername())
                    : "有用户";
            String title = "你的帖子收到了新点赞";
            String content = actorName + " 点赞了你的帖子《" + (post.getTitle() == null ? "" : post.getTitle()) + "》";
            messageService.create(post.getUserId(), "LIKE", title, content, postId);
        }
        return true;
    }
}
