package com.zhiyangyun.care.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("chat_message")
public class ChatMessage {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long conversationId;
  private Long senderId;
  private String senderName;
  private String messageType;
  private String body;
  private Long replyToId;
  private Long taskId;
  private String clientMessageId;
  private LocalDateTime recalledAt;
  private LocalDateTime createTime;
}
