package com.zhiyangyun.care.asset.service;

import com.zhiyangyun.care.asset.model.AssetTreeNode;
import java.util.List;

public interface AssetTreeService {
  List<AssetTreeNode> getTree(Long tenantId);
}
