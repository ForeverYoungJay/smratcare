package com.zhiyangyun.care.store.model.admin;

import java.util.List;
import lombok.Data;

@Data
public class ForbiddenTagsResponse {
  private Long diseaseId;
  private List<Long> selectedTagIds;
  private List<TagGroup> groups;

  @Data
  public static class TagItem {
    private Long id;
    private String tagCode;
    private String tagName;
    private String tagType;
  }

  @Data
  public static class TagGroup {
    private String tagType;
    private List<TagItem> tags;
  }
}
