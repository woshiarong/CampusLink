package com.campus.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.forum.dto.ModerationActionRequest;
import com.campus.forum.entity.ModerationLog;
import com.campus.forum.entity.Post;
import com.campus.forum.entity.Report;
import com.campus.forum.entity.User;
import com.campus.forum.mapper.ModerationLogMapper;
import com.campus.forum.mapper.PostMapper;
import com.campus.forum.mapper.ReportMapper;
import com.campus.forum.mapper.UserMapper;
import com.campus.forum.utils.BusinessException;
import com.campus.forum.vo.ReportVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ModerationService {

    private final ReportMapper reportMapper;
    private final PostMapper postMapper;
    private final ModerationLogMapper moderationLogMapper;
    private final UserMapper userMapper;

    public ModerationService(ReportMapper reportMapper, PostMapper postMapper, ModerationLogMapper moderationLogMapper,
                             UserMapper userMapper) {
        this.reportMapper = reportMapper;
        this.postMapper = postMapper;
        this.moderationLogMapper = moderationLogMapper;
        this.userMapper = userMapper;
    }

    public List<Report> listReports() {
        return reportMapper.selectList(new LambdaQueryWrapper<Report>().orderByDesc(Report::getUpdatedAt));
    }

    public List<ReportVO> listReportVOs(String status) {
        LambdaQueryWrapper<Report> q = new LambdaQueryWrapper<Report>().orderByDesc(Report::getCreatedAt);
        if (StringUtils.hasText(status)) {
            q.eq(Report::getStatus, status);
        }
        List<Report> reports = reportMapper.selectList(q);
        if (reports.isEmpty()) return List.of();
        Set<Long> postIds = reports.stream().map(Report::getPostId).collect(Collectors.toSet());
        Set<Long> reporterIds = reports.stream().map(Report::getReporterId).collect(Collectors.toSet());
        Map<Long, Post> postMap = postMapper.selectBatchIds(postIds).stream().collect(Collectors.toMap(Post::getId, p -> p));
        Map<Long, String> reporterNameMap = userMapper.selectBatchIds(reporterIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u.getNickname() != null ? u.getNickname() : u.getUsername()));
        return reports.stream().map(r -> {
            ReportVO vo = new ReportVO();
            vo.setId(r.getId());
            vo.setPostId(r.getPostId());
            vo.setReporterId(r.getReporterId());
            vo.setReporterName(reporterNameMap.get(r.getReporterId()));
            vo.setReason(r.getReason());
            vo.setStatus(r.getStatus());
            vo.setReviewRemark(r.getReviewRemark());
            vo.setReviewerId(r.getReviewerId());
            vo.setCreatedAt(r.getCreatedAt());
            vo.setUpdatedAt(r.getUpdatedAt());
            Post p = postMap.get(r.getPostId());
            if (p != null) {
                vo.setPostTitle(p.getTitle());
                String content = p.getContent();
                if (content != null) {
                    String plain = content.replaceAll("<[^>]+>", "").trim();
                    vo.setPostContentSummary(plain.length() > 100 ? plain.substring(0, 100) + "..." : plain);
                }
            }
            return vo;
        }).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> review(Long reviewerId, ModerationActionRequest request) {
        Report report = reportMapper.selectById(request.getReportId());
        if (report == null) {
            throw new BusinessException("举报记录不存在");
        }
        if (!"IN_REVIEW".equals(report.getStatus())) {
            throw new BusinessException("当前举报状态不可审核");
        }

        Post post = postMapper.selectById(report.getPostId());
        if (post == null) {
            throw new BusinessException("关联帖子不存在");
        }

        String action = request.getAction();
        if (!"HIDE_POST".equals(action) && !"REJECT_REPORT".equals(action) && !"IGNORE_REPORT".equals(action)) {
            throw new BusinessException("不支持的审核动作");
        }

        report.setReviewerId(reviewerId);
        report.setReviewRemark(request.getReviewRemark());
        report.setStatus("LOG_RECORDED");
        reportMapper.updateById(report);

        if ("HIDE_POST".equals(action)) {
            post.setStatus("MODERATED");
            postMapper.updateById(post);
        } else if ("REJECT_REPORT".equals(action)) {
            post.setStatus("VIEWABLE");
            postMapper.updateById(post);
        }
        // IGNORE_REPORT: 仅关闭举报，不修改帖子

        ModerationLog log = new ModerationLog();
        log.setReportId(report.getId());
        log.setAction(action);
        log.setOperatorId(reviewerId);
        log.setDetail(request.getReviewRemark());
        moderationLogMapper.insert(log);

        Map<String, Object> result = new HashMap<>();
        result.put("reportId", report.getId());
        result.put("postId", post.getId());
        result.put("postStatus", post.getStatus());
        return result;
    }
}
