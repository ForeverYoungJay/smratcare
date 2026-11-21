package com.zhiyangyun.care.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("chat_member")
public class ChatMember {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long conversationId;
  private Long staffId;
  private String memberRole;
  private LocalDateTime joinedAt;
  private LocalDateTime leftAt;
  private Integer muted;
  private Long lastReadMessageId;
}
