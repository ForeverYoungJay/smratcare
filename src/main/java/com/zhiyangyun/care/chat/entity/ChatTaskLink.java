package com.zhiyangyun.care.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("chat_task_link")
public class ChatTaskLink {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long orgId;
  private Long taskId;
  private Long conversationId;
  private Long messageId;
  private Long createdBy;
  private LocalDateTime createTime;
}
