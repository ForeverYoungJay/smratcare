package com.zhiyangyun.care.oa.model;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.Data;

@Data
public class OaAlbumRequest {
  @NotBlank
  private String title;

  private String category;

  private String folderName;

  private String coverUrl;

  private String photosJson;

  private Integer photoCount;

  private LocalDate shootDate;

  private String status;

  private String remark;
}
