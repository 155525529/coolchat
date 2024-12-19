package com.coolchat.service.impl;

import com.coolchat.domain.po.ChatSession;
import com.coolchat.mapper.ChatSessionMapper;
import com.coolchat.service.IChatSessionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yayibai
 * @since 2024-12-03
 */
@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements IChatSessionService {

}
