package com.zhiyangyun.care.oa.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class OaDocumentFolderTreeNode {
  private Long id;
  private String name;
  private Long parentId;
  private Integer sortNo;
  private String status;
  private String visibility;
  private String regionCode;
  private String remark;
  private Integer documentCount;
  private List<OaDocumentFolderTreeNode> children = new ArrayList<>();
}
