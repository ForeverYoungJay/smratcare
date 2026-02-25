package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.oa.entity.OaAlbum;
import com.zhiyangyun.care.oa.mapper.OaAlbumMapper;
import com.zhiyangyun.care.oa.model.OaAlbumRequest;
import jakarta.validation.Valid;
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

  public OaAlbumController(OaAlbumMapper albumMapper) {
    this.albumMapper = albumMapper;
  }

  @GetMapping("/page")
  public Result<IPage<OaAlbum>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String status) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(OaAlbum.class)
        .eq(OaAlbum::getIsDeleted, 0)
        .eq(orgId != null, OaAlbum::getOrgId, orgId)
        .eq(category != null && !category.isBlank(), OaAlbum::getCategory, category)
        .eq(status != null && !status.isBlank(), OaAlbum::getStatus, status);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaAlbum::getTitle, keyword)
          .or().like(OaAlbum::getRemark, keyword));
    }
    wrapper.orderByDesc(OaAlbum::getShootDate).orderByDesc(OaAlbum::getCreateTime);
    return Result.ok(albumMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping
  public Result<OaAlbum> create(@Valid @RequestBody OaAlbumRequest request) {
    Long orgId = AuthContext.getOrgId();
    OaAlbum album = new OaAlbum();
    album.setTenantId(orgId);
    album.setOrgId(orgId);
    album.setTitle(request.getTitle());
    album.setCategory(request.getCategory());
    album.setCoverUrl(request.getCoverUrl());
    album.setPhotoCount(request.getPhotoCount() == null ? 0 : request.getPhotoCount());
    album.setShootDate(request.getShootDate());
    album.setStatus(request.getStatus() == null ? "DRAFT" : request.getStatus());
    album.setRemark(request.getRemark());
    album.setCreatedBy(AuthContext.getStaffId());
    albumMapper.insert(album);
    return Result.ok(album);
  }

  @PutMapping("/{id}")
  public Result<OaAlbum> update(@PathVariable Long id, @Valid @RequestBody OaAlbumRequest request) {
    OaAlbum album = albumMapper.selectById(id);
    if (album == null) {
      return Result.ok(null);
    }
    album.setTitle(request.getTitle());
    album.setCategory(request.getCategory());
    album.setCoverUrl(request.getCoverUrl());
    album.setPhotoCount(request.getPhotoCount());
    album.setShootDate(request.getShootDate());
    album.setStatus(request.getStatus());
    album.setRemark(request.getRemark());
    albumMapper.updateById(album);
    return Result.ok(album);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    OaAlbum album = albumMapper.selectById(id);
    if (album != null) {
      album.setIsDeleted(1);
      albumMapper.updateById(album);
    }
    return Result.ok(null);
  }
}
