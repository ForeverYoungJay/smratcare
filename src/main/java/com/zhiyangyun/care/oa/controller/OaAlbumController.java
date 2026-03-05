package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.oa.entity.OaAlbum;
import com.zhiyangyun.care.oa.mapper.OaAlbumMapper;
import com.zhiyangyun.care.oa.model.OaAlbumRequest;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oa/album")
public class OaAlbumController {
  private final OaAlbumMapper albumMapper;
  private final ObjectMapper objectMapper;

  public OaAlbumController(OaAlbumMapper albumMapper, ObjectMapper objectMapper) {
    this.albumMapper = albumMapper;
    this.objectMapper = objectMapper;
  }

  @GetMapping("/page")
  public Result<IPage<OaAlbum>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String folderName,
      @RequestParam(required = false) String status) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(OaAlbum.class)
        .eq(OaAlbum::getIsDeleted, 0)
        .eq(orgId != null, OaAlbum::getOrgId, orgId)
        .eq(category != null && !category.isBlank(), OaAlbum::getCategory, category)
        .like(folderName != null && !folderName.isBlank(), OaAlbum::getFolderName, folderName)
        .eq(status != null && !status.isBlank(), OaAlbum::getStatus, status);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaAlbum::getTitle, keyword)
          .or().like(OaAlbum::getRemark, keyword)
          .or().like(OaAlbum::getFolderName, keyword));
    }
    wrapper.orderByDesc(OaAlbum::getShootDate).orderByDesc(OaAlbum::getCreateTime);
    return Result.ok(albumMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @GetMapping("/folders")
  public Result<List<String>> folders() {
    Long orgId = AuthContext.getOrgId();
    List<OaAlbum> rows = albumMapper.selectList(Wrappers.lambdaQuery(OaAlbum.class)
        .select(OaAlbum::getFolderName)
        .eq(OaAlbum::getIsDeleted, 0)
        .eq(orgId != null, OaAlbum::getOrgId, orgId)
        .isNotNull(OaAlbum::getFolderName)
        .orderByDesc(OaAlbum::getUpdateTime)
        .orderByDesc(OaAlbum::getCreateTime));
    LinkedHashSet<String> folders = new LinkedHashSet<>();
    for (OaAlbum row : rows) {
      if (row.getFolderName() == null) {
        continue;
      }
      String folderName = row.getFolderName().trim();
      if (!folderName.isEmpty()) {
        folders.add(folderName);
      }
    }
    return Result.ok(new ArrayList<>(folders));
  }

  @PostMapping
  public Result<OaAlbum> create(@Valid @RequestBody OaAlbumRequest request) {
    Long orgId = AuthContext.getOrgId();
    OaAlbum album = new OaAlbum();
    album.setTenantId(orgId);
    album.setOrgId(orgId);
    album.setTitle(request.getTitle());
    album.setCategory(request.getCategory() == null ? null : request.getCategory().trim());
    album.setFolderName(request.getFolderName() == null ? null : request.getFolderName().trim());
    album.setCoverUrl(request.getCoverUrl());
    album.setPhotosJson(request.getPhotosJson());
    album.setPhotoCount(resolvePhotoCount(request.getPhotoCount(), request.getPhotosJson()));
    album.setShootDate(request.getShootDate());
    album.setStatus(request.getStatus() == null ? "DRAFT" : request.getStatus());
    album.setRemark(request.getRemark());
    album.setCreatedBy(AuthContext.getStaffId());
    albumMapper.insert(album);
    return Result.ok(album);
  }

  @PutMapping("/{id}")
  public Result<OaAlbum> update(@PathVariable Long id, @Valid @RequestBody OaAlbumRequest request) {
    OaAlbum album = findAccessibleAlbum(id);
    if (album == null) {
      return Result.ok(null);
    }
    album.setTitle(request.getTitle());
    album.setCategory(request.getCategory() == null ? null : request.getCategory().trim());
    album.setFolderName(request.getFolderName() == null ? null : request.getFolderName().trim());
    album.setCoverUrl(request.getCoverUrl());
    album.setPhotosJson(request.getPhotosJson());
    album.setPhotoCount(resolvePhotoCount(
        request.getPhotoCount() == null ? album.getPhotoCount() : request.getPhotoCount(),
        request.getPhotosJson()));
    album.setShootDate(request.getShootDate());
    album.setStatus(request.getStatus() == null ? album.getStatus() : request.getStatus());
    album.setRemark(request.getRemark());
    albumMapper.updateById(album);
    return Result.ok(album);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    OaAlbum album = findAccessibleAlbum(id);
    if (album != null) {
      album.setIsDeleted(1);
      albumMapper.updateById(album);
    }
    return Result.ok(null);
  }

  private OaAlbum findAccessibleAlbum(Long id) {
    Long orgId = AuthContext.getOrgId();
    return albumMapper.selectOne(Wrappers.lambdaQuery(OaAlbum.class)
        .eq(OaAlbum::getId, id)
        .eq(OaAlbum::getIsDeleted, 0)
        .eq(orgId != null, OaAlbum::getOrgId, orgId)
        .last("LIMIT 1"));
  }

  private int resolvePhotoCount(Integer incomingCount, String photosJson) {
    int count = incomingCount == null ? 0 : Math.max(0, incomingCount);
    int parsedCount = parsePhotoCount(photosJson);
    return Math.max(count, parsedCount);
  }

  private int parsePhotoCount(String photosJson) {
    if (photosJson == null || photosJson.isBlank()) {
      return 0;
    }
    try {
      JsonNode node = objectMapper.readTree(photosJson);
      if (node != null && node.isArray()) {
        return node.size();
      }
    } catch (Exception ignore) {
      return 0;
    }
    return 0;
  }
}
