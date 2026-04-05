package com.campus.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.forum.entity.Board;
import com.campus.forum.entity.Post;
import com.campus.forum.entity.Report;
import com.campus.forum.entity.User;
import com.campus.forum.mapper.BoardMapper;
import com.campus.forum.mapper.PostMapper;
import com.campus.forum.mapper.ReportMapper;
import com.campus.forum.mapper.UserMapper;
import com.campus.forum.vo.StatisticsVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class StatisticsService {

    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final ReportMapper reportMapper;
    private final BoardMapper boardMapper;

    public StatisticsService(UserMapper userMapper, PostMapper postMapper, ReportMapper reportMapper,
                            BoardMapper boardMapper) {
        this.userMapper = userMapper;
        this.postMapper = postMapper;
        this.reportMapper = reportMapper;
        this.boardMapper = boardMapper;
    }

    public StatisticsVO dashboard() {
        StatisticsVO vo = new StatisticsVO();
        vo.setTotalUsers(userMapper.selectCount(null));
        vo.setTotalPosts(postMapper.selectCount(null));

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        vo.setTodayNewUsers(userMapper.selectCount(
                new LambdaQueryWrapper<User>().ge(User::getCreatedAt, todayStart)));
        vo.setTodayNewPosts(postMapper.selectCount(
                new LambdaQueryWrapper<Post>().ge(Post::getCreatedAt, todayStart)));

        vo.setPendingReviewCount(postMapper.selectCount(
                new LambdaQueryWrapper<Post>().eq(Post::getStatus, "UNDER_REVIEW")));
        vo.setReportCount(reportMapper.selectCount(
                new LambdaQueryWrapper<Report>().eq(Report::getStatus, "IN_REVIEW")));

        List<StatisticsVO.DailyStat> recent = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            LocalDateTime start = d.atStartOfDay();
            LocalDateTime end = d.atTime(LocalTime.MAX);
            long uc = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .ge(User::getCreatedAt, start).le(User::getCreatedAt, end));
            long pc = postMapper.selectCount(new LambdaQueryWrapper<Post>()
                    .ge(Post::getCreatedAt, start).le(Post::getCreatedAt, end));
            StatisticsVO.DailyStat ds = new StatisticsVO.DailyStat();
            ds.setDate(d.toString());
            ds.setUserCount(uc);
            ds.setPostCount(pc);
            recent.add(ds);
        }
        vo.setRecentActivity(recent);

        // 各版块帖子数：按版块在库中统计，保证 各版块帖子数之和 = 总帖子数
        long totalPostsInDb = vo.getTotalPosts();
        List<Board> boards = boardMapper.selectList(null);
        List<StatisticsVO.BoardStat> boardStats = new ArrayList<>();
        long sumByBoard = 0;
        for (Board b : boards) {
            long count = postMapper.selectCount(
                    new LambdaQueryWrapper<Post>().eq(Post::getBoardId, b.getId()));
            sumByBoard += count;
            StatisticsVO.BoardStat bs = new StatisticsVO.BoardStat();
            bs.setBoardName(b.getName());
            bs.setPostCount(count);
            boardStats.add(bs);
        }
        // 未归属任何版块的帖子归为「其他」，饼图 7 项（6 版块 + 其他）之和 = 总帖子数
        long other = totalPostsInDb - sumByBoard;
        if (other > 0) {
            StatisticsVO.BoardStat bs = new StatisticsVO.BoardStat();
            bs.setBoardName("其他");
            bs.setPostCount(other);
            boardStats.add(bs);
        }
        vo.setPostCountByBoard(boardStats);
        return vo;
    }
}
