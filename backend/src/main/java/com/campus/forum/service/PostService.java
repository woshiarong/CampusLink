package com.campus.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.dto.AttachmentDto;
import com.campus.forum.dto.PostCreateRequest;
import com.campus.forum.dto.PostUpdateRequest;
import com.campus.forum.entity.*;
import com.campus.forum.mapper.*;
import com.campus.forum.utils.BusinessException;
import com.campus.forum.vo.PostVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostService {

    private static final Set<String> VIEWABLE_STATES = Set.of("VIEWABLE", "INTERACTED", "REPUBLISHED");

    private final PostMapper postMapper;
    private final BoardMapper boardMapper;
    private final UserMapper userMapper;
    private final PostTagMapper postTagMapper;
    private final PostAttachmentMapper postAttachmentMapper;
    private final ReportMapper reportMapper;
    private final SystemSettingMapper systemSettingMapper;
    private final AdminLogService adminLogService;
    private final CommentService commentService;
    private final LikeService likeService;
    private final FavoriteService favoriteService;

    public PostService(PostMapper postMapper,
                       BoardMapper boardMapper,
                       UserMapper userMapper,
                       PostTagMapper postTagMapper,
                       PostAttachmentMapper postAttachmentMapper,
                       ReportMapper reportMapper,
                       SystemSettingMapper systemSettingMapper,
                       AdminLogService adminLogService,
                       CommentService commentService,
                       LikeService likeService,
                       FavoriteService favoriteService) {
        this.postMapper = postMapper;
        this.boardMapper = boardMapper;
        this.userMapper = userMapper;
        this.postTagMapper = postTagMapper;
        this.postAttachmentMapper = postAttachmentMapper;
        this.reportMapper = reportMapper;
        this.systemSettingMapper = systemSettingMapper;
        this.adminLogService = adminLogService;
        this.commentService = commentService;
        this.likeService = likeService;
        this.favoriteService = favoriteService;
    }

    public List<PostVO> listViewablePosts() {
        return listViewablePosts(null);
    }

    public List<PostVO> listViewablePosts(Long currentUserId) {
        List<Post> posts = postMapper.selectList(new LambdaQueryWrapper<Post>()
                .in(Post::getStatus, VIEWABLE_STATES)
                .orderByDesc(Post::getIsPinned)
                .orderByDesc(Post::getUpdatedAt));
        return buildListWithExtra(posts, currentUserId);
    }

    /** 分页查询可查看帖子（帖子广场分页），支持版块、关键词筛选 */
    public IPage<PostVO> listViewablePostsPage(int pageNum, int pageSize, Long currentUserId,
                                               Long boardId, String keyword) {
        Page<Post> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Post> q = new LambdaQueryWrapper<Post>()
                .in(Post::getStatus, VIEWABLE_STATES)
                .orderByDesc(Post::getIsPinned)
                .orderByDesc(Post::getUpdatedAt);
        if (boardId != null && boardId > 0) {
            q.eq(Post::getBoardId, boardId);
        }
        if (keyword != null && !keyword.isBlank()) {
            q.and(w -> w.like(Post::getTitle, keyword).or().like(Post::getContent, keyword));
        }
        IPage<Post> result = postMapper.selectPage(page, q);
        List<PostVO> list = buildListWithExtra(result.getRecords(), currentUserId);
        Page<PostVO> voPage = new Page<>(pageNum, pageSize, result.getTotal());
        voPage.setRecords(list);
        return voPage;
    }

    private List<PostVO> buildListWithExtra(List<Post> posts, Long currentUserId) {
        List<PostVO> list = buildPostVOList(posts);
        if (list.isEmpty()) return list;
        for (PostVO vo : list) {
            vo.setLikeCount(likeService.countByPostId(vo.getId()));
            vo.setCommentCount(commentService.countByPostId(vo.getId()));
            vo.setLikedByMe(currentUserId != null && likeService.hasLiked(vo.getId(), currentUserId));
            vo.setFavoritedByMe(currentUserId != null && favoriteService.hasFavorited(vo.getId(), currentUserId));
        }
        return list;
    }

    public PostVO getPostDetail(Long postId, Long currentUserId, boolean isAdmin) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在或不可查看");
        }
        boolean canView = VIEWABLE_STATES.contains(post.getStatus())
                || (currentUserId != null && Objects.equals(post.getUserId(), currentUserId))
                || isAdmin;
        if (!canView) {
            throw new BusinessException("帖子不存在或不可查看");
        }
        PostVO vo = buildPostVOList(List.of(post)).get(0);
        vo.setLikeCount(likeService.countByPostId(postId));
        vo.setCommentCount(commentService.countByPostId(postId));
        vo.setLikedByMe(currentUserId != null && likeService.hasLiked(postId, currentUserId));
        vo.setFavoritedByMe(currentUserId != null && favoriteService.hasFavorited(postId, currentUserId));
        return vo;
    }

    public List<PostVO> getPostsByIds(List<Long> postIds, Long currentUserId) {
        if (postIds == null || postIds.isEmpty()) return List.of();
        List<Post> posts = postMapper.selectBatchIds(postIds);
        Map<Long, Post> postMap = posts.stream().collect(Collectors.toMap(Post::getId, p -> p));
        List<Post> ordered = postIds.stream().map(postMap::get).filter(Objects::nonNull).toList();
        List<PostVO> list = buildPostVOList(ordered);
        for (PostVO vo : list) {
            vo.setLikeCount(likeService.countByPostId(vo.getId()));
            vo.setCommentCount(commentService.countByPostId(vo.getId()));
            vo.setLikedByMe(currentUserId != null && likeService.hasLiked(vo.getId(), currentUserId));
            vo.setFavoritedByMe(currentUserId != null && favoriteService.hasFavorited(vo.getId(), currentUserId));
        }
        return list;
    }

    public List<PostVO> listMyPosts(Long userId) {
        return listMyPosts(userId, null);
    }

    public List<PostVO> listMyPosts(Long userId, String statusFilter) {
        LambdaQueryWrapper<Post> query = new LambdaQueryWrapper<Post>()
                .eq(Post::getUserId, userId)
                .orderByDesc(Post::getUpdatedAt);
        if (statusFilter != null && !statusFilter.isBlank()) {
            if ("PUBLISHED".equals(statusFilter)) {
                query.in(Post::getStatus, VIEWABLE_STATES);
            } else {
                query.eq(Post::getStatus, statusFilter);
            }
        }
        List<Post> posts = postMapper.selectList(query);
        return buildPostVOList(posts);
    }

    @Transactional(rollbackFor = Exception.class)
    public PostVO createDraft(Long userId, PostCreateRequest request) {
        Board board = boardMapper.selectById(request.getBoardId());
        if (board == null) {
            throw new BusinessException("版块不存在");
        }
        validatePostLimits(request.getContent(), request.getAttachments());
        validateSensitiveWords(request.getTitle(), request.getContent());

        Post post = new Post();
        post.setUserId(userId);
        post.setBoardId(request.getBoardId());
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        boolean autoPublish = getBoolSetting("forum.postAutoPublish", false);
        post.setStatus(autoPublish ? "VIEWABLE" : "DRAFTING");
        postMapper.insert(post);

        saveTagsAndAttachments(post.getId(), request.getTags(), request.getAttachments());
        return getPostById(post.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public PostVO submit(Long userId, Long postId) {
        Post post = mustGetPost(postId);
        if (!Objects.equals(post.getUserId(), userId)) {
            throw new BusinessException("无权提交该帖子");
        }
        if (!("DRAFTING".equals(post.getStatus()) || "UPDATED".equals(post.getStatus()))) {
            throw new BusinessException("当前状态不可提交");
        }
        List<PostAttachment> attachments = postAttachmentMapper.selectList(
                new LambdaQueryWrapper<PostAttachment>().eq(PostAttachment::getPostId, postId));
        List<AttachmentDto> attachmentDtos = attachments.stream().map(a -> {
            AttachmentDto dto = new AttachmentDto();
            dto.setFileName(a.getFileName());
            dto.setFileUrl(a.getFileUrl());
            return dto;
        }).toList();
        validatePostLimits(post.getContent(), attachmentDtos);
        validateSensitiveWords(post.getTitle(), post.getContent());

        boolean autoPublish = getBoolSetting("forum.postAutoPublish", false);
        post.setStatus(autoPublish ? "VIEWABLE" : "UNDER_REVIEW");
        postMapper.updateById(post);
        return getPostById(postId);
    }

    @Transactional(rollbackFor = Exception.class)
    public PostVO interact(Long postId) {
        Post post = mustGetPost(postId);
        if (!"VIEWABLE".equals(post.getStatus())) {
            throw new BusinessException("仅可与可查看帖子交互");
        }
        post.setStatus("INTERACTED");
        postMapper.updateById(post);
        return getPostById(postId);
    }

    @Transactional(rollbackFor = Exception.class)
    public PostVO update(Long userId, Long postId, PostUpdateRequest request) {
        Post post = mustGetPost(postId);
        if (!Objects.equals(post.getUserId(), userId)) {
            throw new BusinessException("无权编辑该帖子");
        }
        if (!("INTERACTED".equals(post.getStatus()) || "VIEWABLE".equals(post.getStatus()))) {
            throw new BusinessException("当前状态不可更新");
        }
        validatePostLimits(request.getContent(), request.getAttachments());
        validateSensitiveWords(request.getTitle(), request.getContent());

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        // 学生更新后系统自动流转 UPDATED -> REPUBLISHED -> VIEWABLE
        post.setStatus("VIEWABLE");
        postMapper.updateById(post);

        postTagMapper.delete(new LambdaQueryWrapper<PostTag>().eq(PostTag::getPostId, postId));
        postAttachmentMapper.delete(new LambdaQueryWrapper<PostAttachment>().eq(PostAttachment::getPostId, postId));
        saveTagsAndAttachments(postId, request.getTags(), request.getAttachments());
        return getPostById(postId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteByOwner(Long userId, Long postId) {
        Post post = mustGetPost(postId);
        if (!Objects.equals(post.getUserId(), userId)) {
            throw new BusinessException("无权删除该帖子");
        }
        postTagMapper.delete(new LambdaQueryWrapper<PostTag>().eq(PostTag::getPostId, postId));
        postAttachmentMapper.delete(new LambdaQueryWrapper<PostAttachment>().eq(PostAttachment::getPostId, postId));
        postMapper.deleteById(postId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void report(Long userId, Long postId, String reason) {
        Post post = mustGetPost(postId);
        if (!VIEWABLE_STATES.contains(post.getStatus())) {
            throw new BusinessException("当前状态不可举报");
        }

        Report report = new Report();
        report.setPostId(postId);
        report.setReporterId(userId);
        report.setReason(reason);
        report.setStatus("IN_REVIEW");
        reportMapper.insert(report);

        post.setStatus("UNDER_REVIEW");
        postMapper.updateById(post);
    }

    public PostVO getPostById(Long postId) {
        return buildPostVOList(List.of(mustGetPost(postId))).stream().findFirst()
                .orElseThrow(() -> new BusinessException("帖子不存在"));
    }

    public IPage<PostVO> listAdminPosts(int pageNum, int pageSize, Long boardId, String status, String sortBy) {
        LambdaQueryWrapper<Post> q = new LambdaQueryWrapper<>();
        if (boardId != null && boardId > 0) q.eq(Post::getBoardId, boardId);
        if (status != null && !status.isBlank()) q.eq(Post::getStatus, status);
        if ("oldest".equals(sortBy)) {
            q.orderByAsc(Post::getCreatedAt);
        } else {
            q.orderByDesc(Post::getUpdatedAt);
        }
        Page<Post> page = new Page<>(pageNum, pageSize);
        IPage<Post> result = postMapper.selectPage(page, q);
        return result.convert(p -> buildPostVOList(List.of(p)).get(0));
    }

    @Transactional(rollbackFor = Exception.class)
    public void setPinned(Long postId, int pinned, Long operatorId) {
        Post post = mustGetPost(postId);
        post.setIsPinned(pinned);
        postMapper.updateById(post);
        adminLogService.log(operatorId, "POST_PIN", "post", postId, pinned == 1 ? "置顶" : "取消置顶");
    }

    @Transactional(rollbackFor = Exception.class)
    public void setFeatured(Long postId, int featured, Long operatorId) {
        Post post = mustGetPost(postId);
        post.setIsFeatured(featured);
        postMapper.updateById(post);
        adminLogService.log(operatorId, "POST_FEATURE", "post", postId, featured == 1 ? "加精" : "取消加精");
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminDelete(Long postId, Long operatorId) {
        Post post = mustGetPost(postId);
        postMapper.deleteById(postId);
        adminLogService.log(operatorId, "POST_DELETE", "post", postId, post.getTitle());
    }

    @Transactional(rollbackFor = Exception.class)
    public void moveBoard(Long postId, Long boardId, Long operatorId) {
        Post post = mustGetPost(postId);
        if (boardMapper.selectById(boardId) == null) {
            throw new BusinessException("目标版块不存在");
        }
        post.setBoardId(boardId);
        postMapper.updateById(post);
        adminLogService.log(operatorId, "POST_MOVE", "post", postId, "boardId=" + boardId);
    }

    /** 管理员审核通过：待审核 -> 可查看 */
    @Transactional(rollbackFor = Exception.class)
    public void approvePost(Long postId, Long operatorId) {
        Post post = mustGetPost(postId);
        if (!"UNDER_REVIEW".equals(post.getStatus())) {
            throw new BusinessException("仅待审核帖子可执行审核通过");
        }
        post.setStatus("VIEWABLE");
        postMapper.updateById(post);
        adminLogService.log(operatorId, "POST_APPROVE", "post", postId, "审核通过");
    }

    private void saveTagsAndAttachments(Long postId, List<String> tags, List<AttachmentDto> attachments) {
        List<String> safeTags = tags == null ? new ArrayList<>() : tags;
        List<AttachmentDto> safeAttachments = attachments == null ? new ArrayList<>() : attachments;

        for (String tag : safeTags) {
            if (tag == null || tag.isBlank()) {
                continue;
            }
            PostTag postTag = new PostTag();
            postTag.setPostId(postId);
            postTag.setTagName(tag.trim());
            postTagMapper.insert(postTag);
        }

        for (AttachmentDto attachment : safeAttachments) {
            if (attachment == null) {
                continue;
            }
            PostAttachment postAttachment = new PostAttachment();
            postAttachment.setPostId(postId);
            postAttachment.setFileName(attachment.getFileName());
            postAttachment.setFileUrl(attachment.getFileUrl());
            postAttachmentMapper.insert(postAttachment);
        }
    }

    private Post mustGetPost(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        return post;
    }

    private void validateSensitiveWords(String title, String content) {
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

        String text = ((title == null ? "" : title) + "\n" + (content == null ? "" : content)).toLowerCase();
        List<String> hitWords = new ArrayList<>();
        for (String token : raw.split(",")) {
            String word = token == null ? "" : token.trim();
            if (word.isBlank()) continue;
            if (text.contains(word.toLowerCase()) && !hitWords.contains(word)) {
                hitWords.add(word);
            }
        }
        if (!hitWords.isEmpty()) {
            throw new BusinessException("内容包含敏感词：" + String.join("、", hitWords) + "，请修改后再提交");
        }
    }

    private void validatePostLimits(String content, List<AttachmentDto> attachments) {
        int minLength = getIntSetting("forum.postMinLength", 0);
        int maxAttachments = getIntSetting("forum.maxAttachments", Integer.MAX_VALUE);

        String text = content == null ? "" : content.replaceAll("<[^>]*>", "").trim();
        int contentLength = text.length();
        if (minLength > 0 && contentLength < minLength) {
            throw new BusinessException("帖子内容至少需要输入 " + minLength + " 个字");
        }

        int attachmentCount = attachments == null ? 0 : attachments.size();
        if (attachmentCount > maxAttachments) {
            throw new BusinessException("附件数量超出限制，最多允许上传 " + maxAttachments + " 个附件");
        }
    }

    private int getIntSetting(String key, int defaultValue) {
        SystemSetting setting = systemSettingMapper.selectOne(new LambdaQueryWrapper<SystemSetting>()
                .eq(SystemSetting::getSettingKey, key)
                .last("limit 1"));
        if (setting == null || setting.getSettingValue() == null) return defaultValue;
        try {
            return Integer.parseInt(setting.getSettingValue().trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private boolean getBoolSetting(String key, boolean defaultValue) {
        SystemSetting setting = systemSettingMapper.selectOne(new LambdaQueryWrapper<SystemSetting>()
                .eq(SystemSetting::getSettingKey, key)
                .last("limit 1"));
        if (setting == null || setting.getSettingValue() == null) {
            return defaultValue;
        }
        String v = setting.getSettingValue().trim();
        if (v.isEmpty()) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(v)
                || "1".equals(v)
                || "yes".equalsIgnoreCase(v)
                || "on".equalsIgnoreCase(v)
                || "开启".equals(v);
    }

    private List<PostVO> buildPostVOList(List<Post> posts) {
        if (posts.isEmpty()) {
            return List.of();
        }

        List<Long> boardIds = posts.stream().map(Post::getBoardId).distinct().toList();
        List<Long> userIds = posts.stream().map(Post::getUserId).distinct().toList();
        List<Long> postIds = posts.stream().map(Post::getId).toList();

        Map<Long, String> boardNameMap = boardMapper.selectBatchIds(boardIds).stream()
                .collect(Collectors.toMap(Board::getId, Board::getName));
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        Map<Long, List<String>> tagMap = postTagMapper.selectList(new LambdaQueryWrapper<PostTag>()
                        .in(PostTag::getPostId, postIds)).stream()
                .collect(Collectors.groupingBy(PostTag::getPostId,
                        Collectors.mapping(PostTag::getTagName, Collectors.toList())));

        Map<Long, List<PostAttachment>> attachmentMap = postAttachmentMapper.selectList(new LambdaQueryWrapper<PostAttachment>()
                        .in(PostAttachment::getPostId, postIds)).stream()
                .collect(Collectors.groupingBy(PostAttachment::getPostId));

        return posts.stream().map(post -> {
            PostVO vo = new PostVO();
            vo.setId(post.getId());
            vo.setUserId(post.getUserId());
            User author = userMap.get(post.getUserId());
            vo.setAuthorName(author != null ? (author.getNickname() != null ? author.getNickname() : "匿名") : "匿名");
            vo.setAuthorAvatar(author != null ? author.getAvatar() : null);
            vo.setBoardId(post.getBoardId());
            vo.setBoardName(boardNameMap.getOrDefault(post.getBoardId(), "未知版块"));
            vo.setTitle(post.getTitle());
            vo.setContent(post.getContent());
            vo.setStatus(post.getStatus());
            vo.setIsPinned(post.getIsPinned() != null ? post.getIsPinned() : 0);
            vo.setIsFeatured(post.getIsFeatured() != null ? post.getIsFeatured() : 0);
            vo.setCreatedAt(post.getCreatedAt());
            vo.setUpdatedAt(post.getUpdatedAt());
            vo.setTags(tagMap.getOrDefault(post.getId(), List.of()));

            List<PostVO.AttachmentVO> attachmentVOList = attachmentMap.getOrDefault(post.getId(), List.of())
                    .stream()
                    .map(item -> {
                        PostVO.AttachmentVO attachmentVO = new PostVO.AttachmentVO();
                        attachmentVO.setFileName(item.getFileName());
                        attachmentVO.setFileUrl(item.getFileUrl());
                        return attachmentVO;
                    })
                    .collect(Collectors.toList());
            vo.setAttachments(attachmentVOList);
            return vo;
        }).collect(Collectors.toList());
    }
}
