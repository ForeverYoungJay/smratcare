package com.zhiyangyun.care.asset.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class AssetTreeNode {
  private String type;
  private Long id;
  private String name;
  private Integer status;
  private String roomNo;
  private String bedNo;
  private String qrCode;
  private List<AssetTreeNode> children = new ArrayList<>();
}
