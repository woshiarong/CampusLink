package com.campus.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.forum.entity.Message;
import com.campus.forum.mapper.MessageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageService {

    private final MessageMapper messageMapper;

    public MessageService(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(Long userId, String type, String title, String content, Long refId) {
        if (userId == null) return;
        Message m = new Message();
        m.setUserId(userId);
        m.setType(type);
        m.setTitle(title);
        m.setContent(content);
        m.setRefId(refId);
        m.setIsRead(0);
        messageMapper.insert(m);
    }

    public List<Message> listByUser(Long userId) {
        return messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getUserId, userId)
                .orderByDesc(Message::getCreatedAt));
    }

    public long countUnread(Long userId) {
        return messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                .eq(Message::getUserId, userId)
                .eq(Message::getIsRead, 0));
    }

    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long userId, Long messageId) {
        Message m = messageMapper.selectById(messageId);
        if (m == null || !userId.equals(m.getUserId())) return;
        m.setIsRead(1);
        messageMapper.updateById(m);
    }

    @Transactional(rollbackFor = Exception.class)
    public void markAllRead(Long userId) {
        List<Message> all = messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getUserId, userId)
                .eq(Message::getIsRead, 0));
        for (Message m : all) {
            m.setIsRead(1);
            messageMapper.updateById(m);
        }
    }
}

