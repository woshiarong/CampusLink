package com.campus.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.forum.entity.Favorite;
import com.campus.forum.entity.Post;
import com.campus.forum.mapper.FavoriteMapper;
import com.campus.forum.mapper.PostMapper;
import com.campus.forum.utils.BusinessException;
import com.campus.forum.vo.PostVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final PostMapper postMapper;

    public FavoriteService(FavoriteMapper favoriteMapper, PostMapper postMapper) {
        this.favoriteMapper = favoriteMapper;
        this.postMapper = postMapper;
    }

    public boolean hasFavorited(Long postId, Long userId) {
        if (userId == null) return false;
        return favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getPostId, postId).eq(Favorite::getUserId, userId)) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean toggle(Long postId, Long userId) {
        if (userId == null) throw new BusinessException("请先登录");
        LambdaQueryWrapper<Favorite> q = new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getPostId, postId).eq(Favorite::getUserId, userId);
        Favorite existing = favoriteMapper.selectOne(q);
        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            return false;
        }
        Favorite f = new Favorite();
        f.setPostId(postId);
        f.setUserId(userId);
        favoriteMapper.insert(f);
        return true;
    }

    public List<Long> listPostIdsByUser(Long userId) {
        return favoriteMapper.selectList(new LambdaQueryWrapper<Favorite>().eq(Favorite::getUserId, userId))
                .stream().map(Favorite::getPostId).toList();
    }
}
