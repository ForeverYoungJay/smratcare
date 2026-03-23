package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.oa.entity.OaDocument;
import com.zhiyangyun.care.oa.entity.OaDocumentFolder;
import com.zhiyangyun.care.oa.mapper.OaDocumentFolderMapper;
import com.zhiyangyun.care.oa.mapper.OaDocumentMapper;
import com.zhiyangyun.care.oa.model.OaBatchIdsRequest;
import com.zhiyangyun.care.oa.model.OaDocumentFolderRequest;
import com.zhiyangyun.care.oa.model.OaDocumentFolderTreeNode;
import com.zhiyangyun.care.oa.model.OaDocumentRequest;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
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
@RequestMapping("/api/oa/document")
public class OaDocumentController {
  private static final String FOLDER_STATUS_ENABLED = "ENABLED";
  private static final String FOLDER_STATUS_DISABLED = "DISABLED";
  private static final String FOLDER_VISIBILITY_PUBLIC = "PUBLIC";
  private static final String FOLDER_VISIBILITY_PRIVATE = "PRIVATE";
  private static final int MAX_NAME_LENGTH = 255;
  private static final int MAX_FOLDER_LENGTH = 128;
  private static final int MAX_URL_LENGTH = 1024;
  private static final int MAX_UPLOADER_LENGTH = 64;
  private static final int MAX_REMARK_LENGTH = 500;

  private final OaDocumentMapper documentMapper;
  private final OaDocumentFolderMapper folderMapper;
  private final JdbcTemplate jdbcTemplate;
  private final Map<String, Boolean> schemaCapabilityCache = new ConcurrentHashMap<>();

  public OaDocumentController(
      OaDocumentMapper documentMapper,
      OaDocumentFolderMapper folderMapper,
      JdbcTemplate jdbcTemplate) {
    this.documentMapper = documentMapper;
    this.folderMapper = folderMapper;
    this.jdbcTemplate = jdbcTemplate;
  }

  @GetMapping("/page")
  public Result<IPage<OaDocument>> page(
      @RequestParam(required = false) Long pageNo,
      @RequestParam(required = false) Long pageSize,
      @RequestParam(required = false) Long page,
      @RequestParam(required = false) Long size,
      @RequestParam(required = false) Long folderId,
      @RequestParam(required = false) String folder,
      @RequestParam(required = false) String folderVisibility,
      @RequestParam(required = false) String regionCode,
      @RequestParam(required = false) String keyword) {
    long resolvedPageNo = pageNo != null && pageNo > 0 ? pageNo : (page != null && page > 0 ? page : 1);
    long resolvedPageSize = pageSize != null && pageSize > 0 ? pageSize : (size != null && size > 0 ? size : 20);
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    String normalizedFolderVisibility = normalizeFolderVisibilityFilter(folderVisibility);
    String normalizedRegionCode = normalizeOptionalText(regionCode);
    boolean folderIdSupported = hasDocumentFolderIdColumn();
    List<Long> accessibleFolderIds =
        listAccessibleFolderIds(orgId, staffId, normalizedFolderVisibility, normalizedRegionCode);
    var wrapper = documentQuery(orgId)
        .eq(folderIdSupported && folderId != null, OaDocument::getFolderId, folderId)
        .eq((!folderIdSupported || folderId == null) && folder != null && !folder.isBlank(), OaDocument::getFolder, folder.trim());
    if (folderIdSupported) {
      if (folderId == null && (folder == null || folder.isBlank()) && !accessibleFolderIds.isEmpty()) {
        wrapper.and(w -> w.isNull(OaDocument::getFolderId).or().in(OaDocument::getFolderId, accessibleFolderIds));
      } else if (folderId != null && !accessibleFolderIds.contains(folderId)) {
        return Result.ok(new Page<>(resolvedPageNo, resolvedPageSize, 0));
      }
    }
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaDocument::getName, keyword)
          .or().like(OaDocument::getUploaderName, keyword));
    }
    wrapper.orderByDesc(OaDocument::getUploadedAt).orderByDesc(OaDocument::getCreateTime);
    return Result.ok(documentMapper.selectPage(new Page<>(resolvedPageNo, resolvedPageSize), wrapper));
  }

  @GetMapping("/folder/tree")
  public Result<List<OaDocumentFolderTreeNode>> folderTree() {
    if (!hasDocumentFolderTable()) {
      return Result.ok(List.of());
    }
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    List<OaDocumentFolder> folders = folderMapper.selectList(accessibleFolderQuery(orgId, staffId)
        .orderByAsc(OaDocumentFolder::getSortNo)
        .orderByAsc(OaDocumentFolder::getCreateTime));
    List<OaDocument> docs = hasDocumentFolderIdColumn()
        ? documentMapper.selectList(documentQuery(orgId).isNotNull(OaDocument::getFolderId))
        : List.of();
    Map<Long, Integer> folderDocCountMap = new HashMap<>();
    for (OaDocument doc : docs) {
      Long folderId = doc.getFolderId();
      if (folderId == null) {
        continue;
      }
      folderDocCountMap.put(folderId, folderDocCountMap.getOrDefault(folderId, 0) + 1);
    }

    Map<Long, OaDocumentFolderTreeNode> nodeMap = new HashMap<>();
    for (OaDocumentFolder folder : folders) {
      OaDocumentFolderTreeNode node = new OaDocumentFolderTreeNode();
      node.setId(folder.getId());
      node.setName(folder.getName());
      node.setParentId(normalizeParentId(folder.getParentId()));
      node.setSortNo(folder.getSortNo());
      node.setStatus(folder.getStatus());
      node.setVisibility(folder.getVisibility());
      node.setRegionCode(folder.getRegionCode());
      node.setRemark(folder.getRemark());
      node.setDocumentCount(folderDocCountMap.getOrDefault(folder.getId(), 0));
      nodeMap.put(node.getId(), node);
    }

    List<OaDocumentFolderTreeNode> roots = new ArrayList<>();
    for (OaDocumentFolderTreeNode node : nodeMap.values()) {
      Long parentId = node.getParentId();
      if (parentId == null || parentId <= 0) {
        roots.add(node);
      } else {
        OaDocumentFolderTreeNode parent = nodeMap.get(parentId);
        if (parent == null) {
          roots.add(node);
        } else {
          parent.getChildren().add(node);
        }
      }
    }
    sortFolderNodes(roots);
    return Result.ok(roots);
  }

  @PostMapping("/folder")
  public Result<OaDocumentFolder> createFolder(@Valid @RequestBody OaDocumentFolderRequest request) {
    ensureFolderLibraryEnabled();
    Long orgId = AuthContext.getOrgId();
    Long parentId = normalizeParentId(request.getParentId());
    if (parentId != null && parentId > 0) {
      OaDocumentFolder parent = findAccessibleFolder(parentId);
      if (parent == null) {
        throw new IllegalArgumentException("上级档案夹不存在");
      }
    }
    String name = normalizeFolderName(request.getName());
    ensureFolderNameUnique(null, parentId, name);
    OaDocumentFolder folder = new OaDocumentFolder();
    folder.setTenantId(orgId);
    folder.setOrgId(orgId);
    folder.setName(name);
    folder.setParentId(parentId == null ? 0L : parentId);
    folder.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
    folder.setStatus(normalizeFolderStatus(request.getStatus(), FOLDER_STATUS_ENABLED));
    folder.setVisibility(normalizeFolderVisibility(request.getVisibility(), FOLDER_VISIBILITY_PUBLIC));
    folder.setRegionCode(normalizeOptionalText(request.getRegionCode()));
    folder.setRemark(request.getRemark());
    folder.setCreatedBy(AuthContext.getStaffId());
    folderMapper.insert(folder);
    return Result.ok(folder);
  }

  @PutMapping("/folder/{id}")
  public Result<OaDocumentFolder> updateFolder(
      @PathVariable Long id,
      @Valid @RequestBody OaDocumentFolderRequest request) {
    ensureFolderLibraryEnabled();
    OaDocumentFolder folder = findAccessibleFolder(id);
    if (folder == null) {
      return Result.ok(null);
    }
    Long parentId = normalizeParentId(request.getParentId());
    if (parentId != null && parentId > 0) {
      if (Objects.equals(parentId, folder.getId())) {
        throw new IllegalArgumentException("档案夹不能设置自己为上级");
      }
      OaDocumentFolder parent = findAccessibleFolder(parentId);
      if (parent == null) {
        throw new IllegalArgumentException("上级档案夹不存在");
      }
      if (isDescendant(parentId, folder.getId())) {
        throw new IllegalArgumentException("不能将档案夹移动到自己的子级下");
      }
    }
    String name = normalizeFolderName(request.getName());
    ensureFolderNameUnique(folder.getId(), parentId, name);
    folder.setName(name);
    folder.setParentId(parentId == null ? 0L : parentId);
    folder.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
    folder.setStatus(normalizeFolderStatus(request.getStatus(), folder.getStatus()));
    folder.setVisibility(normalizeFolderVisibility(request.getVisibility(), folder.getVisibility()));
    folder.setRegionCode(normalizeOptionalText(request.getRegionCode()));
    folder.setRemark(request.getRemark());
    folderMapper.updateById(folder);
    return Result.ok(folder);
  }

  @DeleteMapping("/folder/{id}")
  public Result<Void> deleteFolder(@PathVariable Long id) {
    ensureFolderLibraryEnabled();
    OaDocumentFolder folder = findAccessibleFolder(id);
    if (folder == null) {
      return Result.ok(null);
    }
    Long orgId = AuthContext.getOrgId();
    Long childCount = folderMapper.selectCount(Wrappers.lambdaQuery(OaDocumentFolder.class)
        .eq(OaDocumentFolder::getIsDeleted, 0)
        .eq(orgId != null, OaDocumentFolder::getOrgId, orgId)
        .eq(OaDocumentFolder::getParentId, folder.getId()));
    if (childCount != null && childCount > 0) {
      throw new IllegalArgumentException("请先删除子档案夹");
    }
    Long docCount = hasDocumentFolderIdColumn()
        ? documentMapper.selectCount(Wrappers.lambdaQuery(OaDocument.class)
            .eq(OaDocument::getIsDeleted, 0)
            .eq(orgId != null, OaDocument::getOrgId, orgId)
            .eq(OaDocument::getFolderId, folder.getId()))
        : 0L;
    if (docCount != null && docCount > 0) {
      throw new IllegalArgumentException("档案夹内存在文档，无法删除");
    }
    folder.setIsDeleted(1);
    folderMapper.updateById(folder);
    return Result.ok(null);
  }

  @PostMapping
  public Result<OaDocument> create(@Valid @RequestBody OaDocumentRequest request) {
    Long orgId = AuthContext.getOrgId();
    validateSize(request.getSizeBytes());
    validatePayload(request);
    OaDocument doc = new OaDocument();
    doc.setTenantId(orgId);
    doc.setOrgId(orgId);
    String normalizedUrl = normalizeText(request.getUrl(), MAX_URL_LENGTH);
    String normalizedFolder = normalizeText(request.getFolder(), MAX_FOLDER_LENGTH);
    Long resolvedFolderId = hasDocumentFolderIdColumn() ? resolveFolderId(request.getFolderId(), normalizedFolder) : null;
    doc.setName(normalizeText(resolveDocumentName(request.getName(), normalizedUrl), MAX_NAME_LENGTH));
    doc.setFolderId(resolvedFolderId);
    doc.setFolder(resolveFolderName(resolvedFolderId, normalizedFolder));
    doc.setUrl(normalizedUrl);
    doc.setSizeBytes(request.getSizeBytes());
    doc.setUploaderId(request.getUploaderId());
    String uploaderName = request.getUploaderName() == null ? AuthContext.getUsername() : request.getUploaderName();
    doc.setUploaderName(normalizeText(uploaderName, MAX_UPLOADER_LENGTH));
    doc.setUploadedAt(request.getUploadedAt() == null ? LocalDateTime.now() : request.getUploadedAt());
    doc.setRemark(normalizeText(request.getRemark(), MAX_REMARK_LENGTH));
    doc.setCreatedBy(AuthContext.getStaffId());
    documentMapper.insert(doc);
    return Result.ok(doc);
  }

  @PutMapping("/{id}")
  public Result<OaDocument> update(@PathVariable Long id, @Valid @RequestBody OaDocumentRequest request) {
    OaDocument doc = findAccessibleDocument(id);
    if (doc == null) {
      return Result.ok(null);
    }
    validateSize(request.getSizeBytes());
    validatePayload(request);
    String normalizedUrl = normalizeText(request.getUrl(), MAX_URL_LENGTH);
    String normalizedFolder = normalizeText(request.getFolder(), MAX_FOLDER_LENGTH);
    Long resolvedFolderId = hasDocumentFolderIdColumn() ? resolveFolderId(request.getFolderId(), normalizedFolder) : null;
    doc.setName(normalizeText(resolveDocumentName(request.getName(), normalizedUrl), MAX_NAME_LENGTH));
    doc.setFolderId(resolvedFolderId);
    doc.setFolder(resolveFolderName(resolvedFolderId, normalizedFolder));
    doc.setUrl(normalizedUrl);
    doc.setSizeBytes(request.getSizeBytes());
    doc.setUploaderId(request.getUploaderId());
    String updateUploaderName = normalizeText(request.getUploaderName(), MAX_UPLOADER_LENGTH);
    if (updateUploaderName != null) {
      doc.setUploaderName(updateUploaderName);
    } else if (doc.getUploaderName() == null || doc.getUploaderName().isBlank()) {
      doc.setUploaderName(normalizeText(AuthContext.getUsername(), MAX_UPLOADER_LENGTH));
    }
    doc.setUploadedAt(request.getUploadedAt() == null ? doc.getUploadedAt() : request.getUploadedAt());
    doc.setRemark(normalizeText(request.getRemark(), MAX_REMARK_LENGTH));
    documentMapper.updateById(doc);
    return Result.ok(doc);
  }

  @DeleteMapping("/batch")
  public Result<Integer> batchDelete(@RequestBody OaBatchIdsRequest request) {
    List<Long> ids = sanitizeIds(request == null ? null : request.getIds());
    if (ids.isEmpty()) {
      return Result.ok(0);
    }
    Long orgId = AuthContext.getOrgId();
    List<OaDocument> docs = documentMapper.selectList(documentQuery(orgId)
        .in(OaDocument::getId, ids));
    for (OaDocument doc : docs) {
      doc.setIsDeleted(1);
      documentMapper.updateById(doc);
    }
    return Result.ok(docs.size());
  }

  @GetMapping(value = "/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> export(
      @RequestParam(required = false) Long folderId,
      @RequestParam(required = false) String folder,
      @RequestParam(required = false) String folderVisibility,
      @RequestParam(required = false) String regionCode,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    String normalizedFolderVisibility = normalizeFolderVisibilityFilter(folderVisibility);
    String normalizedRegionCode = normalizeOptionalText(regionCode);
    boolean folderIdSupported = hasDocumentFolderIdColumn();
    List<Long> accessibleFolderIds =
        listAccessibleFolderIds(orgId, staffId, normalizedFolderVisibility, normalizedRegionCode);
    if (folderIdSupported && folderId != null && !accessibleFolderIds.contains(folderId)) {
      return csvResponse("oa-document",
          List.of("ID", "文件名", "目录", "URL", "大小(B)", "上传人", "上传时间", "备注"),
          List.of());
    }
    var wrapper = documentQuery(orgId)
        .eq(folderIdSupported && folderId != null, OaDocument::getFolderId, folderId)
        .eq((!folderIdSupported || folderId == null) && folder != null && !folder.isBlank(), OaDocument::getFolder, folder.trim());
    if (folderIdSupported && folderId == null && (folder == null || folder.isBlank()) && !accessibleFolderIds.isEmpty()) {
      wrapper.and(w -> w.isNull(OaDocument::getFolderId).or().in(OaDocument::getFolderId, accessibleFolderIds));
    }
    wrapper.orderByDesc(OaDocument::getUploadedAt).orderByDesc(OaDocument::getCreateTime);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaDocument::getName, keyword).or().like(OaDocument::getUploaderName, keyword));
    }
    List<OaDocument> docs = documentMapper.selectList(wrapper);
    List<String> headers = List.of("ID", "文件名", "目录", "URL", "大小(B)", "上传人", "上传时间", "备注");
    List<List<String>> rows = docs.stream()
        .map(item -> List.of(
            safe(item.getId()),
            safe(item.getName()),
            safe(item.getFolder()),
            safe(item.getUrl()),
            safe(item.getSizeBytes()),
            safe(item.getUploaderName()),
            formatDateTime(item.getUploadedAt()),
            safe(item.getRemark())))
        .toList();
    return csvResponse("oa-document", headers, rows);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    OaDocument doc = findAccessibleDocument(id);
    if (doc != null) {
      doc.setIsDeleted(1);
      documentMapper.updateById(doc);
    }
    return Result.ok(null);
  }

  private OaDocument findAccessibleDocument(Long id) {
    Long orgId = AuthContext.getOrgId();
    return documentMapper.selectOne(documentQuery(orgId)
        .eq(OaDocument::getId, id)
        .eq(OaDocument::getIsDeleted, 0)
        .last("LIMIT 1"));
  }

  private OaDocumentFolder findAccessibleFolder(Long id) {
    if (!hasDocumentFolderTable()) {
      return null;
    }
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    return folderMapper.selectOne(accessibleFolderQuery(orgId, staffId)
        .eq(OaDocumentFolder::getId, id)
        .last("LIMIT 1"));
  }

  private boolean isDescendant(Long checkFolderId, Long ancestorId) {
    Long parentId = checkFolderId;
    int guard = 0;
    while (parentId != null && parentId > 0 && guard < 50) {
      if (Objects.equals(parentId, ancestorId)) {
        return true;
      }
      OaDocumentFolder current = findAccessibleFolder(parentId);
      if (current == null) {
        break;
      }
      parentId = normalizeParentId(current.getParentId());
      guard++;
    }
    return false;
  }

  private void validateSize(Long sizeBytes) {
    if (sizeBytes != null && sizeBytes < 0) {
      throw new IllegalArgumentException("sizeBytes 不能小于 0");
    }
  }

  private void validatePayload(OaDocumentRequest request) {
    String normalizedName = normalizeText(request.getName(), MAX_NAME_LENGTH);
    String normalizedUrl = normalizeText(request.getUrl(), MAX_URL_LENGTH);
    if ((normalizedName == null || normalizedName.isBlank())
        && (normalizedUrl == null || normalizedUrl.isBlank())) {
      throw new IllegalArgumentException("文件名或文件地址至少填写一项");
    }
  }

  private String resolveDocumentName(String name, String url) {
    if (name != null && !name.isBlank()) {
      return name.trim();
    }
    if (url == null || url.isBlank()) {
      return "未命名文档";
    }
    String normalized = url;
    int queryStart = normalized.indexOf('?');
    if (queryStart >= 0) {
      normalized = normalized.substring(0, queryStart);
    }
    int slash = normalized.lastIndexOf('/');
    if (slash >= 0 && slash + 1 < normalized.length()) {
      return normalized.substring(slash + 1);
    }
    return normalized;
  }

  private Long resolveFolderId(Long folderId, String folderName) {
    if (!hasDocumentFolderTable()) {
      return null;
    }
    if (folderId != null && folderId > 0) {
      OaDocumentFolder folder = findAccessibleFolder(folderId);
      if (folder != null) {
        if (FOLDER_STATUS_DISABLED.equals(folder.getStatus())) {
          throw new IllegalArgumentException("档案夹已停用，无法保存文档");
        }
        return folder.getId();
      }
    }
    if (folderName == null || folderName.isBlank()) {
      return null;
    }
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    OaDocumentFolder sameName = folderMapper.selectOne(accessibleFolderQuery(orgId, staffId)
        .eq(OaDocumentFolder::getName, folderName.trim())
        .last("LIMIT 1"));
    if (sameName == null || FOLDER_STATUS_DISABLED.equals(sameName.getStatus())) {
      return null;
    }
    return sameName.getId();
  }

  private String resolveFolderName(Long folderId, String fallback) {
    if (folderId != null && folderId > 0) {
      OaDocumentFolder folder = findAccessibleFolder(folderId);
      if (folder != null) {
        return folder.getName();
      }
    }
    if (fallback == null || fallback.isBlank()) {
      return null;
    }
    return fallback.trim();
  }

  private String normalizeFolderName(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("档案夹名称不能为空");
    }
    return name.trim();
  }

  private String normalizeFolderStatus(String status, String defaultStatus) {
    if (status == null || status.isBlank()) {
      return defaultStatus == null ? FOLDER_STATUS_ENABLED : defaultStatus;
    }
    String normalized = status.trim().toUpperCase();
    if (!FOLDER_STATUS_ENABLED.equals(normalized) && !FOLDER_STATUS_DISABLED.equals(normalized)) {
      throw new IllegalArgumentException("档案夹状态仅支持 ENABLED/DISABLED");
    }
    return normalized;
  }

  private String normalizeFolderVisibility(String visibility, String defaultVisibility) {
    if (visibility == null || visibility.isBlank()) {
      return defaultVisibility == null ? FOLDER_VISIBILITY_PUBLIC : defaultVisibility;
    }
    String normalized = visibility.trim().toUpperCase();
    if (!FOLDER_VISIBILITY_PUBLIC.equals(normalized) && !FOLDER_VISIBILITY_PRIVATE.equals(normalized)) {
      throw new IllegalArgumentException("档案夹可见性仅支持 PUBLIC/PRIVATE");
    }
    return normalized;
  }

  private String normalizeFolderVisibilityFilter(String visibility) {
    if (visibility == null || visibility.isBlank()) {
      return null;
    }
    String normalized = visibility.trim().toUpperCase();
    if (!FOLDER_VISIBILITY_PUBLIC.equals(normalized) && !FOLDER_VISIBILITY_PRIVATE.equals(normalized)) {
      throw new IllegalArgumentException("档案夹可见性仅支持 PUBLIC/PRIVATE");
    }
    return normalized;
  }

  private void ensureFolderNameUnique(Long selfId, Long parentId, String name) {
    Long orgId = AuthContext.getOrgId();
    OaDocumentFolder sameName = folderMapper.selectOne(folderQuery(orgId)
        .eq(OaDocumentFolder::getIsDeleted, 0)
        .eq(OaDocumentFolder::getParentId, parentId == null ? 0L : parentId)
        .eq(OaDocumentFolder::getName, name)
        .last("LIMIT 1"));
    if (sameName != null && !Objects.equals(sameName.getId(), selfId)) {
      throw new IllegalArgumentException("同级目录下已存在同名档案夹");
    }
  }

  private Long normalizeParentId(Long parentId) {
    if (parentId == null || parentId <= 0) {
      return 0L;
    }
    return parentId;
  }

  private String normalizeOptionalText(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

  private String normalizeText(String value, int maxLength) {
    if (value == null) {
      return null;
    }
    String normalized = value.trim();
    if (normalized.isEmpty()) {
      return null;
    }
    if (maxLength > 0 && normalized.length() > maxLength) {
      return normalized.substring(0, maxLength);
    }
    return normalized;
  }

  private void sortFolderNodes(List<OaDocumentFolderTreeNode> nodes) {
    nodes.sort(Comparator
        .comparing(OaDocumentFolderTreeNode::getSortNo, Comparator.nullsLast(Integer::compareTo))
        .thenComparing(OaDocumentFolderTreeNode::getName, Comparator.nullsLast(String::compareTo)));
    for (OaDocumentFolderTreeNode node : nodes) {
      if (node.getChildren() == null || node.getChildren().isEmpty()) {
        continue;
      }
      sortFolderNodes(node.getChildren());
      int childCount = node.getChildren().stream()
          .map(item -> item.getDocumentCount() == null ? 0 : item.getDocumentCount())
          .reduce(0, Integer::sum);
      node.setDocumentCount((node.getDocumentCount() == null ? 0 : node.getDocumentCount()) + childCount);
    }
  }

  private List<Long> sanitizeIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    return ids.stream().filter(id -> id != null && id > 0).distinct().collect(Collectors.toList());
  }

  private List<Long> listAccessibleFolderIds(Long orgId, Long staffId, String visibility, String regionCode) {
    if (!hasDocumentFolderTable() || !hasDocumentFolderIdColumn()) {
      return List.of();
    }
    return folderMapper.selectList(accessibleFolderQuery(orgId, staffId)
            .eq(hasFolderVisibilityColumn() && visibility != null, OaDocumentFolder::getVisibility, visibility)
            .like(hasFolderRegionCodeColumn() && regionCode != null && !regionCode.isBlank(), OaDocumentFolder::getRegionCode, regionCode))
        .stream()
        .map(OaDocumentFolder::getId)
        .filter(Objects::nonNull)
        .toList();
  }

  private void ensureFolderLibraryEnabled() {
    if (!hasDocumentFolderTable() || !hasFolderVisibilityColumn()) {
      throw new IllegalArgumentException("当前数据库未启用完整档案夹功能，请先执行 OA 文档目录迁移");
    }
  }

  private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OaDocument> documentQuery(Long orgId) {
    var wrapper = Wrappers.lambdaQuery(OaDocument.class)
        .select(documentSelectColumns())
        .eq(OaDocument::getIsDeleted, 0)
        .eq(orgId != null, OaDocument::getOrgId, orgId);
    return wrapper;
  }

  private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OaDocumentFolder> folderQuery(Long orgId) {
    return Wrappers.lambdaQuery(OaDocumentFolder.class)
        .select(folderSelectColumns())
        .eq(OaDocumentFolder::getIsDeleted, 0)
        .eq(orgId != null, OaDocumentFolder::getOrgId, orgId);
  }

  private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OaDocumentFolder> accessibleFolderQuery(
      Long orgId, Long staffId) {
    var wrapper = folderQuery(orgId);
    if (hasFolderVisibilityColumn()) {
      wrapper.and(w -> w.eq(OaDocumentFolder::getVisibility, FOLDER_VISIBILITY_PUBLIC)
          .or().isNull(OaDocumentFolder::getVisibility)
          .or().eq(staffId != null, OaDocumentFolder::getCreatedBy, staffId));
    }
    return wrapper;
  }

  @SuppressWarnings("unchecked")
  private SFunction<OaDocument, ?>[] documentSelectColumns() {
    List<SFunction<OaDocument, ?>> columns = new ArrayList<>();
    columns.add(OaDocument::getId);
    columns.add(OaDocument::getTenantId);
    columns.add(OaDocument::getOrgId);
    columns.add(OaDocument::getName);
    columns.add(OaDocument::getFolder);
    if (hasDocumentFolderIdColumn()) {
      columns.add(OaDocument::getFolderId);
    }
    columns.add(OaDocument::getUrl);
    columns.add(OaDocument::getSizeBytes);
    columns.add(OaDocument::getUploaderId);
    columns.add(OaDocument::getUploaderName);
    columns.add(OaDocument::getUploadedAt);
    columns.add(OaDocument::getRemark);
    columns.add(OaDocument::getCreatedBy);
    columns.add(OaDocument::getCreateTime);
    columns.add(OaDocument::getUpdateTime);
    columns.add(OaDocument::getIsDeleted);
    return columns.toArray(new SFunction[0]);
  }

  @SuppressWarnings("unchecked")
  private SFunction<OaDocumentFolder, ?>[] folderSelectColumns() {
    List<SFunction<OaDocumentFolder, ?>> columns = new ArrayList<>();
    columns.add(OaDocumentFolder::getId);
    columns.add(OaDocumentFolder::getTenantId);
    columns.add(OaDocumentFolder::getOrgId);
    columns.add(OaDocumentFolder::getName);
    columns.add(OaDocumentFolder::getParentId);
    columns.add(OaDocumentFolder::getSortNo);
    columns.add(OaDocumentFolder::getStatus);
    if (hasFolderVisibilityColumn()) {
      columns.add(OaDocumentFolder::getVisibility);
    }
    if (hasFolderRegionCodeColumn()) {
      columns.add(OaDocumentFolder::getRegionCode);
    }
    columns.add(OaDocumentFolder::getRemark);
    columns.add(OaDocumentFolder::getCreatedBy);
    columns.add(OaDocumentFolder::getCreateTime);
    columns.add(OaDocumentFolder::getUpdateTime);
    columns.add(OaDocumentFolder::getIsDeleted);
    return columns.toArray(new SFunction[0]);
  }

  private boolean hasDocumentFolderTable() {
    return hasTable("oa_document_folder");
  }

  private boolean hasDocumentFolderIdColumn() {
    return hasColumn("oa_document", "folder_id");
  }

  private boolean hasFolderVisibilityColumn() {
    return hasColumn("oa_document_folder", "visibility");
  }

  private boolean hasFolderRegionCodeColumn() {
    return hasColumn("oa_document_folder", "region_code");
  }

  private boolean hasTable(String tableName) {
    return schemaCapabilityCache.computeIfAbsent("table:" + tableName, key -> {
      Integer count = jdbcTemplate.queryForObject(
          """
              SELECT COUNT(1)
              FROM information_schema.TABLES
              WHERE TABLE_SCHEMA = DATABASE()
                AND TABLE_NAME = ?
              """,
          Integer.class,
          tableName);
      return count != null && count > 0;
    });
  }

  private boolean hasColumn(String tableName, String columnName) {
    return schemaCapabilityCache.computeIfAbsent("column:" + tableName + ":" + columnName, key -> {
      Integer count = jdbcTemplate.queryForObject(
          """
              SELECT COUNT(1)
              FROM information_schema.COLUMNS
              WHERE TABLE_SCHEMA = DATABASE()
                AND TABLE_NAME = ?
                AND COLUMN_NAME = ?
              """,
          Integer.class,
          tableName,
          columnName);
      return count != null && count > 0;
    });
  }

  private String safe(Object value) {
    return value == null ? "" : String.valueOf(value);
  }

  private String formatDateTime(LocalDateTime value) {
    if (value == null) {
      return "";
    }
    return value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }

  private ResponseEntity<byte[]> csvResponse(String filenameBase, List<String> headers, List<List<String>> rows) {
    StringBuilder sb = new StringBuilder();
    sb.append('\uFEFF');
    sb.append(toCsvLine(headers)).append('\n');
    for (List<String> row : rows) {
      sb.append(toCsvLine(row)).append('\n');
    }
    byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
    String filename = filenameBase + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    HttpHeaders headersObj = new HttpHeaders();
    headersObj.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename + ".csv");
    headersObj.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
    headersObj.setContentLength(bytes.length);
    return ResponseEntity.ok().headers(headersObj).body(bytes);
  }

  private String toCsvLine(List<String> fields) {
    List<String> escaped = new ArrayList<>();
    for (String field : fields) {
      String value = field == null ? "" : field;
      value = value.replace("\"", "\"\"");
      if (value.contains(",") || value.contains("\n") || value.contains("\r") || value.contains("\"")) {
        value = "\"" + value + "\"";
      }
      escaped.add(value);
    }
    return String.join(",", escaped);
  }
}
