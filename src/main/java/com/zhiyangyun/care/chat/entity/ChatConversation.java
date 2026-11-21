package com.zhiyangyun.care.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("chat_conversation")
public class ChatConversation {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long orgId;
  private String convType;
  private String title;
  private Long departmentId;
  private String directKey;
  private Long createdBy;
  private Long lastMessageId;
  private String lastMessagePreview;
  private LocalDateTime lastMessageAt;
  private LocalDateTime archivedAt;
  private LocalDateTime createTime;
}
