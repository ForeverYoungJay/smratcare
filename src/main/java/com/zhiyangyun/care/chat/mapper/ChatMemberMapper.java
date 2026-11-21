package com.zhiyangyun.care.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhiyangyun.care.chat.entity.ChatMember;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMemberMapper extends BaseMapper<ChatMember> {
}
