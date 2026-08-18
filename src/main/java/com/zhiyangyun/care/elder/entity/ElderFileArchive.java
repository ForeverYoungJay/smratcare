package com.zhiyangyun.care.elder.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 长者电子文件存档：CONTRACT 合同 / MEDICAL 医疗 / CERTIFICATE 证件 / OTHER 其他。 */
@Data
@TableName("elder_file_archive")
public class ElderFileArchive {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long elderId;
  private String category;
  private String fileName;
  private String fileUrl;
  private Long fileSize;
  private String remark;
  private Long uploadedBy;
  private LocalDateTime uploadedAt;
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
