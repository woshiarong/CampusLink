package com.campus.forum.vo;

import lombok.Data;

import java.util.List;

@Data
public class StatisticsVO {
    private long totalUsers;
    private long totalPosts;
    private long todayNewUsers;
    private long todayNewPosts;
    private long pendingReviewCount;
    private long reportCount;
    private List<StatisticsVO.DailyStat> recentActivity;
    private List<StatisticsVO.BoardStat> postCountByBoard;

    @Data
    public static class DailyStat {
        private String date;
        private long userCount;
        private long postCount;
    }

    @Data
    public static class BoardStat {
        private String boardName;
        private long postCount;
    }
}
