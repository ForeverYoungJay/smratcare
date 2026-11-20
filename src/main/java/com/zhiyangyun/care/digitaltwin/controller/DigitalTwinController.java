package com.zhiyangyun.care.digitaltwin.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.asset.entity.Building;
import com.zhiyangyun.care.asset.mapper.BuildingMapper;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.common.web.ResourceNotFoundException;
import com.zhiyangyun.care.digitaltwin.entity.DtBusinessBinding;
import com.zhiyangyun.care.digitaltwin.entity.DtSceneVersion;
import com.zhiyangyun.care.digitaltwin.entity.DtSpatialAsset;
import com.zhiyangyun.care.digitaltwin.mapper.DtBusinessBindingMapper;
import com.zhiyangyun.care.digitaltwin.mapper.DtSceneVersionMapper;
import com.zhiyangyun.care.digitaltwin.mapper.DtSpatialAssetMapper;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.elder.model.BedStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数字孪生中心（只读）。所有查询都按登录人所属机构过滤，assetId 是稳定编码而非模型节点名；
 * 业务汇总只读既有床位主数据，不复制业务状态。绑定只有 ADMIN/SYS_ADMIN 能建立、发布、停用，并写审计。
 * 不提供任何设备控制、长者身份或视频画面。
 */
@RestController
@RequestMapping("/api/digital-twin")
public class DigitalTwinController {
  static final Set<String> MODES = Set.of("campus", "occupancy", "fire", "facility", "video", "analytics");
  /** 床位汇总按床位业务口径开放；财务、人事、系统管理员不默认可见 */
  private static final Set<String> OCCUPANCY_ROLES = Set.of("NURSING_EMPLOYEE", "NURSING_MINISTER",
      "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER",
      "MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "ADMIN");
  private static final Map<String, String[]> NOT_CONNECTED = Map.of(
      "fire", new String[] {"消防事件尚未与空间资产建立已核实的点位映射，未定位事件不在三维中标注", "/fire/overview"},
      "facility", new String[] {"维修工单只记录房间号，尚未与楼栋资产核实映射", "/logistics/assets/maintenance-record"},
      "video", new String[] {"视频网关与摄像头点位尚未接入；三维页不播放视频画面", ""},
      "analytics", new String[] {"运营分析需在楼栋绑定核实后按源系统口径汇总", "/stats/org/monthly-operation"});

  private final DtSceneVersionMapper sceneMapper;
  private final DtSpatialAssetMapper assetMapper;
  private final DtBusinessBindingMapper bindingMapper;
  private final BuildingMapper buildingMapper;
  private final RoomMapper roomMapper;
  private final BedMapper bedMapper;
  private final AuditLogService auditLogService;

  public DigitalTwinController(DtSceneVersionMapper sceneMapper, DtSpatialAssetMapper assetMapper,
      DtBusinessBindingMapper bindingMapper, BuildingMapper buildingMapper, RoomMapper roomMapper,
      BedMapper bedMapper, AuditLogService auditLogService) {
    this.sceneMapper = sceneMapper;
    this.assetMapper = assetMapper;
    this.bindingMapper = bindingMapper;
    this.buildingMapper = buildingMapper;
    this.roomMapper = roomMapper;
    this.bedMapper = bedMapper;
    this.auditLogService = auditLogService;
  }

  @GetMapping("/scene")
  public Result<Map<String, Object>> scene() {
    DtSceneVersion scene = sceneMapper.selectOne(Wrappers.lambdaQuery(DtSceneVersion.class)
        .eq(DtSceneVersion::getOrgId, orgId()).eq(DtSceneVersion::getStatus, "PUBLISHED")
        .orderByDesc(DtSceneVersion::getPublishedAt).last("LIMIT 1"));
    if (scene == null) {
      throw new ResourceNotFoundException("本机构尚未发布数字孪生场景");
    }
    Map<String, Object> row = new LinkedHashMap<>();
    row.put("version", scene.getVersion());
    row.put("modelUrl", scene.getModelUrl());
    row.put("sourceType", scene.getSourceType());
    row.put("verificationStatus", scene.getVerificationStatus());
    row.put("coordinateNote", scene.getCoordinateNote());
    row.put("publishedAt", scene.getPublishedAt());
    row.put("dataMode", "CONCEPT".equals(scene.getVerificationStatus()) ? "DEMO" : "LIVE");
    row.put("canManageBindings", canManageBindings());
    row.put("canViewOccupancy", canViewOccupancy());
    return Result.ok(row);
  }

  @GetMapping("/assets")
  public Result<List<Map<String, Object>>> assets(@RequestParam(required = false) String type) {
    List<DtSpatialAsset> rows = assetMapper.selectList(Wrappers.lambdaQuery(DtSpatialAsset.class)
        .eq(DtSpatialAsset::getOrgId, orgId())
        .eq(type != null && !type.isBlank(), DtSpatialAsset::getAssetType, type)
        .orderByAsc(DtSpatialAsset::getSortNo));
    Set<String> bound = activeBindings(null).stream().map(DtBusinessBinding::getAssetCode)
        .collect(java.util.stream.Collectors.toSet());
    return Result.ok(rows.stream().map(a -> view(a, bound.contains(a.getAssetCode()))).toList());
  }

  @GetMapping("/assets/{assetId}")
  public Result<Map<String, Object>> asset(@PathVariable String assetId) {
    DtSpatialAsset asset = requireAsset(assetId);
    return Result.ok(view(asset, !activeBindings(asset.getAssetCode()).isEmpty()));
  }

  @GetMapping("/assets/{assetId}/summary")
  public Result<Map<String, Object>> summary(@PathVariable String assetId, @RequestParam String mode) {
    if (!MODES.contains(mode)) {
      throw new IllegalArgumentException("未知模式");
    }
    DtSpatialAsset asset = requireAsset(assetId);
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("assetId", asset.getAssetCode());
    out.put("mode", mode);
    if ("occupancy".equals(mode)) {
      if (!canViewOccupancy()) {
        throw new AccessDeniedException("无权查看床位汇总");
      }
      return Result.ok(occupancy(asset, out));
    }
    String[] reason = NOT_CONNECTED.getOrDefault(mode, new String[] {"园区总览不汇总业务数据", ""});
    out.put("dataMode", "NOT_CONNECTED");
    out.put("message", reason[0]);
    out.put("link", reason[1]);
    out.put("metrics", List.of());
    return Result.ok(out);
  }

  // ---------- 管理员：业务绑定（SecurityConfig 已限定 ADMIN/SYS_ADMIN，这里再校验一次） ----------

  @GetMapping("/admin/bindings")
  public Result<List<Map<String, Object>>> bindings(@RequestParam(required = false) String assetId) {
    requireBindingAdmin();
    List<DtBusinessBinding> rows = bindingMapper.selectList(Wrappers.lambdaQuery(DtBusinessBinding.class)
        .eq(DtBusinessBinding::getOrgId, orgId())
        .eq(assetId != null && !assetId.isBlank(), DtBusinessBinding::getAssetCode, assetId)
        .orderByDesc(DtBusinessBinding::getCreateTime));
    return Result.ok(rows.stream().map(this::bindingView).toList());
  }

  public record CreateBindingRequest(String assetId, String entityType, Long externalEntityId, String remark) {}

  /** 首期只允许把楼栋资产绑定到既有楼栋主数据（building.id），先建为 PENDING，发布后才生效 */
  @PostMapping("/admin/bindings")
  @Transactional
  public Result<Map<String, Object>> createBinding(@RequestBody CreateBindingRequest request) {
    requireBindingAdmin();
    if (!"BUILDING".equals(request.entityType()) || request.externalEntityId() == null) {
      throw new IllegalArgumentException("首期只支持绑定楼栋主数据");
    }
    DtSpatialAsset asset = requireAsset(request.assetId());
    if (!"BUILDING".equals(asset.getAssetType())) {
      throw new IllegalArgumentException("只有楼栋资产可以绑定楼栋主数据");
    }
    Building building = buildingMapper.selectOne(Wrappers.lambdaQuery(Building.class)
        .eq(Building::getId, request.externalEntityId()).eq(Building::getOrgId, orgId())
        .eq(Building::getIsDeleted, 0));
    if (building == null) {
      throw new ResourceNotFoundException("楼栋不存在");
    }
    DtBusinessBinding binding = new DtBusinessBinding();
    binding.setOrgId(orgId());
    binding.setAssetCode(asset.getAssetCode());
    binding.setSourceModule("ASSET");
    binding.setEntityType("BUILDING");
    binding.setExternalEntityId(building.getId());
    binding.setBindingStatus("PENDING");
    binding.setCreatedBy(AuthContext.getStaffId());
    binding.setRemark(request.remark() == null ? null : request.remark().strip());
    binding.setCreateTime(LocalDateTime.now());
    bindingMapper.insert(binding);
    audit("DT_BINDING_CREATE", binding, asset.getAssetCode() + " → 楼栋 " + building.getName());
    return Result.ok(bindingView(binding));
  }

  /** 发布：同一资产或同一楼栋只能有一条 ACTIVE，旧的自动停用 */
  @PostMapping("/admin/bindings/{id}/publish")
  @Transactional
  public Result<Map<String, Object>> publishBinding(@PathVariable Long id) {
    requireBindingAdmin();
    DtBusinessBinding binding = requireBinding(id);
    if (!"PENDING".equals(binding.getBindingStatus())) {
      throw new IllegalArgumentException("只有待发布的绑定可以发布");
    }
    // shortcut: 唯一 ACTIVE 靠事务内先停用再发布保证，没有数据库唯一约束；并发发布需要时再加锁
    for (DtBusinessBinding old : bindingMapper.selectList(Wrappers.lambdaQuery(DtBusinessBinding.class)
        .eq(DtBusinessBinding::getOrgId, binding.getOrgId()).eq(DtBusinessBinding::getBindingStatus, "ACTIVE")
        .and(w -> w.eq(DtBusinessBinding::getAssetCode, binding.getAssetCode())
            .or(o -> o.eq(DtBusinessBinding::getEntityType, binding.getEntityType())
                .eq(DtBusinessBinding::getExternalEntityId, binding.getExternalEntityId()))))) {
      retire(old, "发布新绑定时自动停用");
    }
    binding.setBindingStatus("ACTIVE");
    binding.setVerifiedBy(AuthContext.getStaffId());
    binding.setVerifiedAt(LocalDateTime.now());
    bindingMapper.updateById(binding);
    audit("DT_BINDING_PUBLISH", binding, binding.getAssetCode() + " 绑定生效");
    return Result.ok(bindingView(binding));
  }

  @PostMapping("/admin/bindings/{id}/retire")
  @Transactional
  public Result<Map<String, Object>> retireBinding(@PathVariable Long id) {
    requireBindingAdmin();
    DtBusinessBinding binding = requireBinding(id);
    if ("RETIRED".equals(binding.getBindingStatus())) {
      throw new IllegalArgumentException("绑定已停用");
    }
    retire(binding, "管理员停用");
    return Result.ok(bindingView(binding));
  }

  // ---------- 内部 ----------

  private Map<String, Object> occupancy(DtSpatialAsset asset, Map<String, Object> out) {
    List<Long> roomIds = null;
    if ("BUILDING".equals(asset.getAssetType())) {
      List<DtBusinessBinding> active = activeBindings(asset.getAssetCode());
      if (active.isEmpty()) {
        out.put("dataMode", "UNBOUND");
        out.put("message", "该示例楼栋尚未与床位主数据中的楼栋核实绑定，不显示数字");
        out.put("metrics", List.of());
        return out;
      }
      roomIds = roomMapper.selectList(Wrappers.lambdaQuery(Room.class).select(Room::getId)
          .eq(Room::getOrgId, orgId()).eq(Room::getBuildingId, active.get(0).getExternalEntityId())
          .eq(Room::getIsDeleted, 0)).stream().map(Room::getId).toList();
    } else if (!"CAMPUS".equals(asset.getAssetType())) {
      throw new IllegalArgumentException("该资产类型暂不支持床位汇总");
    }
    List<Map<String, Object>> metrics = new ArrayList<>();
    long total = 0;
    Object[][] statuses = {{"availableBeds", "空闲", BedStatus.AVAILABLE}, {"occupiedBeds", "在住", BedStatus.OCCUPIED},
        {"maintenanceBeds", "维修", BedStatus.MAINTENANCE}, {"disabledBeds", "停用", BedStatus.DISABLED}};
    for (Object[] s : statuses) {
      long count = roomIds != null && roomIds.isEmpty() ? 0 : bedMapper.selectCount(Wrappers.lambdaQuery(Bed.class)
          .eq(Bed::getOrgId, orgId()).eq(Bed::getIsDeleted, 0).eq(Bed::getStatus, s[2])
          .in(roomIds != null, Bed::getRoomId, roomIds));
      total += count;
      metrics.add(metric((String) s[0], (String) s[1], count));
    }
    metrics.add(0, metric("totalBeds", "床位总数", total));
    out.put("dataMode", "LIVE");
    out.put("source", "床位主数据（按床位业务状态，不以总数减在住推算）");
    out.put("scope", "CAMPUS".equals(asset.getAssetType()) ? "本机构全部床位" : "已绑定楼栋的床位");
    out.put("updatedAt", LocalDateTime.now());
    out.put("link", "/elder/bed-panorama");
    out.put("metrics", metrics);
    return out;
  }

  private static Map<String, Object> metric(String key, String label, long value) {
    return Map.of("key", key, "label", label, "value", value, "unit", "床");
  }

  private Map<String, Object> view(DtSpatialAsset a, boolean bound) {
    Map<String, Object> row = new LinkedHashMap<>();
    row.put("assetId", a.getAssetCode());
    row.put("assetType", a.getAssetType());
    row.put("displayName", a.getName());
    row.put("parentAssetId", a.getParentCode());
    row.put("modelNodeKey", a.getModelNodeKey());
    row.put("verificationStatus", a.getVerificationStatus());
    row.put("sourceReference", a.getSourceReference());
    row.put("verifiedAt", a.getVerifiedAt());
    row.put("bound", bound);
    return row;
  }

  private Map<String, Object> bindingView(DtBusinessBinding b) {
    Map<String, Object> row = new LinkedHashMap<>();
    row.put("id", String.valueOf(b.getId()));
    row.put("assetId", b.getAssetCode());
    row.put("entityType", b.getEntityType());
    row.put("externalEntityId", String.valueOf(b.getExternalEntityId()));
    row.put("bindingStatus", b.getBindingStatus());
    row.put("verifiedAt", b.getVerifiedAt());
    row.put("createTime", b.getCreateTime());
    row.put("remark", b.getRemark());
    return row;
  }

  private void retire(DtBusinessBinding binding, String reason) {
    binding.setBindingStatus("RETIRED");
    binding.setRetiredAt(LocalDateTime.now());
    bindingMapper.updateById(binding);
    audit("DT_BINDING_RETIRE", binding, binding.getAssetCode() + " 绑定停用：" + reason);
  }

  private void audit(String action, DtBusinessBinding binding, String detail) {
    auditLogService.record(binding.getOrgId(), binding.getOrgId(), AuthContext.getStaffId(), AuthContext.getUsername(),
        action, "DT_BUSINESS_BINDING", binding.getId(), detail);
  }

  private List<DtBusinessBinding> activeBindings(String assetCode) {
    return bindingMapper.selectList(Wrappers.lambdaQuery(DtBusinessBinding.class)
        .eq(DtBusinessBinding::getOrgId, orgId()).eq(DtBusinessBinding::getBindingStatus, "ACTIVE")
        .eq(assetCode != null, DtBusinessBinding::getAssetCode, assetCode));
  }

  /** 资产不存在与属于其他机构一律 404，不暴露存在性 */
  private DtSpatialAsset requireAsset(String assetId) {
    DtSpatialAsset asset = assetId == null ? null : assetMapper.selectOne(Wrappers.lambdaQuery(DtSpatialAsset.class)
        .eq(DtSpatialAsset::getOrgId, orgId()).eq(DtSpatialAsset::getAssetCode, assetId));
    if (asset == null) {
      throw new ResourceNotFoundException("空间资产不存在");
    }
    return asset;
  }

  private DtBusinessBinding requireBinding(Long id) {
    DtBusinessBinding binding = bindingMapper.selectOne(Wrappers.lambdaQuery(DtBusinessBinding.class)
        .eq(DtBusinessBinding::getId, id).eq(DtBusinessBinding::getOrgId, orgId()));
    if (binding == null) {
      throw new ResourceNotFoundException("绑定不存在");
    }
    return binding;
  }

  private static Long orgId() {
    Long orgId = AuthContext.getOrgId();
    if (orgId == null) {
      throw new AccessDeniedException("无法确定所属机构");
    }
    return orgId;
  }

  private static boolean canManageBindings() {
    return AuthContext.hasRole("ADMIN") || AuthContext.hasRole("SYS_ADMIN");
  }

  private static boolean canViewOccupancy() {
    return OCCUPANCY_ROLES.stream().anyMatch(AuthContext::hasRole);
  }

  private static void requireBindingAdmin() {
    if (!canManageBindings()) {
      throw new AccessDeniedException("只有管理员可以配置空间绑定");
    }
  }
}
