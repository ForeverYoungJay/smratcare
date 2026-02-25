package com.zhiyangyun.care.oa.model;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.Data;

@Data
public class OaAlbumRequest {
  @NotBlank
  private String title;

  private String category;

  private String coverUrl;

  private Integer photoCount;

  private LocalDate shootDate;

  private String status;

  private String remark;
}
