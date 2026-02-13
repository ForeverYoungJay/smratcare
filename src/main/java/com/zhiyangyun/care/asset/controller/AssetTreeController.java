package com.zhiyangyun.care.asset.controller;

import com.zhiyangyun.care.asset.model.AssetTreeNode;
import com.zhiyangyun.care.asset.service.AssetTreeService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/asset/tree")
public class AssetTreeController {
  private final AssetTreeService assetTreeService;

  public AssetTreeController(AssetTreeService assetTreeService) {
    this.assetTreeService = assetTreeService;
  }

  @GetMapping
  public Result<List<AssetTreeNode>> tree() {
    Long tenantId = AuthContext.getOrgId();
    return Result.ok(assetTreeService.getTree(tenantId));
  }
}
