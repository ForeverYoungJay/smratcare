package com.zhiyangyun.care.digitaltwin.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.asset.entity.Building;
import com.zhiyangyun.care.asset.entity.Floor;
import com.zhiyangyun.care.asset.mapper.BuildingMapper;
import com.zhiyangyun.care.asset.mapper.FloorMapper;
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
import com.zhiyangyun.care.fire.entity.FireLocation;
import com.zhiyangyun.care.fire.entity.FireSafetyRecord;
import com.zhiyangyun.care.fire.mapper.FireLocationMapper;
import com.zhiyangyun.care.fire.mapper.FireSafetyRecordMapper;
import com.zhiyangyun.care.life.entity.MaintenanceRequest;
import com.zhiyangyun.care.life.mapper.MaintenanceRequestMapper;
import com.zhiyangyun.care.logistics.entity.LogisticsCamera;
import com.zhiyangyun.care.logistics.mapper.LogisticsCameraMapper;
import com.zhiyangyun.care.smart.entity.SmartDevice;
import com.zhiyangyun.care.smart.mapper.SmartDeviceMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
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
 * 业务汇总只读既有主数据与业务表（床位、报修、消防、设备、摄像头台账），不复制业务状态。绑定只有 ADMIN/SYS_ADMIN 能建立、发布、停用，并写审计。
 * 不提供任何设备控制、长者身份或视频画面。
 */
@RestController
@RequestMapping("/api/digital-twin")
public class DigitalTwinController {
  static final Set<String> MODES = Set.of("campus", "occupancy", "fire", "facility", "video", "analytics");
  private static final Set<String> LOGISTICS = Set.of("LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER", "DIRECTOR", "ADMIN");
  /**
   * 各模式按源业务模块的可见口径开放（只给汇总数字，不含长者身份）；系统管理员不默认看业务数据。
   * 床位：护理/医务/后勤/市场；消防、视频：后勤；维修：后勤 + 护理（护理员常报修）；运营分析：各部长与院长。
   */
  private static final Map<String, Set<String>> MODE_ROLES = Map.of(
      "occupancy", Set.of("NURSING_EMPLOYEE", "NURSING_MINISTER", "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER",
          "LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER", "MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "ADMIN"),
      "fire", LOGISTICS,
      "video", LOGISTICS,
      "facility", Set.of("LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "DIRECTOR", "ADMIN"),
      "analytics", Set.of("NURSING_MINISTER", "MEDICAL_MINISTER", "LOGISTICS_MINISTER", "MARKETING_MINISTER", "DIRECTOR", "ADMIN"));
  private static final String CAMPUS_CODE = "GF-CAMPUS-001";

  private final DtSceneVersionMapper sceneMapper;
  private final DtSpatialAssetMapper assetMapper;
  private final DtBusinessBindingMapper bindingMapper;
  private final BuildingMapper buildingMapper;
  private final RoomMapper roomMapper;
  private final BedMapper bedMapper;
  private final AuditLogService auditLogService;
  private final FloorMapper floorMapper;
  private final FireLocationMapper fireLocationMapper;
  private final FireSafetyRecordMapper fireRecordMapper;
  private final MaintenanceRequestMapper maintenanceMapper;
  private final SmartDeviceMapper deviceMapper;
  private final LogisticsCameraMapper cameraMapper;

  public DigitalTwinController(DtSceneVersionMapper sceneMapper, DtSpatialAssetMapper assetMapper,
      DtBusinessBindingMapper bindingMapper, BuildingMapper buildingMapper, RoomMapper roomMapper,
      BedMapper bedMapper, AuditLogService auditLogService, FloorMapper floorMapper,
      FireLocationMapper fireLocationMapper, FireSafetyRecordMapper fireRecordMapper,
      MaintenanceRequestMapper maintenanceMapper, SmartDeviceMapper deviceMapper, LogisticsCameraMapper cameraMapper) {
    this.floorMapper = floorMapper;
    this.fireLocationMapper = fireLocationMapper;
    this.fireRecordMapper = fireRecordMapper;
    this.maintenanceMapper = maintenanceMapper;
    this.deviceMapper = deviceMapper;
    this.cameraMapper = cameraMapper;
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
    row.put("canViewOccupancy", canView("occupancy"));
    row.put("visibleModes", MODES.stream().filter(m -> "campus".equals(m) || canView(m)).sorted().toList());
    return Result.ok(row);
  }

  @GetMapping("/assets")
  public Result<List<Map<String, Object>>> assets(@RequestParam(required = false) String type) {
    syncMissingBuildings(); // 楼栋主数据新增的楼栋自动接入，不再等管理员手动同步
    List<DtSpatialAsset> rows = assetMapper.selectList(Wrappers.lambdaQuery(DtSpatialAsset.class)
        .eq(DtSpatialAsset::getOrgId, orgId())
        .eq(type != null && !type.isBlank(), DtSpatialAsset::getAssetType, type)
        .orderByAsc(DtSpatialAsset::getSortNo));
    Map<String, Long> bound = boundBuildings();
    Map<Long, Building> masters = bound.isEmpty() ? Map.of() : buildingMapper.selectList(Wrappers.lambdaQuery(Building.class)
        .eq(Building::getOrgId, orgId()).eq(Building::getIsDeleted, 0).in(Building::getId, bound.values()))
        .stream().collect(Collectors.toMap(Building::getId, Function.identity()));
    Map<Long, Long> floors = countBy(bound.isEmpty() ? List.of() : floorMapper.selectList(Wrappers.lambdaQuery(Floor.class)
        .select(Floor::getBuildingId).eq(Floor::getOrgId, orgId()).eq(Floor::getIsDeleted, 0)
        .in(Floor::getBuildingId, bound.values())), Floor::getBuildingId);
    Map<Long, Long> rooms = countBy(bound.isEmpty() ? List.of() : roomMapper.selectList(Wrappers.lambdaQuery(Room.class)
        .select(Room::getBuildingId).eq(Room::getOrgId, orgId()).eq(Room::getIsDeleted, 0)
        .in(Room::getBuildingId, bound.values())), Room::getBuildingId);
    return Result.ok(rows.stream().map(a -> {
      Map<String, Object> row = view(a, bound.containsKey(a.getAssetCode()));
      Long buildingId = bound.get(a.getAssetCode());
      Building master = buildingId == null ? null : masters.get(buildingId);
      if (master != null) {
        // 绑定楼栋的名称、楼层数、房间数直接取主数据，不在孪生侧另存
        row.put("buildingName", master.getName());
        row.put("floorCount", floors.getOrDefault(master.getId(), 0L));
        row.put("roomCount", rooms.getOrDefault(master.getId(), 0L));
      }
      return row;
    }).toList());
  }

  @GetMapping("/assets/{assetId}")
  public Result<Map<String, Object>> asset(@PathVariable String assetId) {
    DtSpatialAsset asset = requireAsset(assetId);
    return Result.ok(view(asset, boundBuilding(asset) != null));
  }

  /** 楼栋着色：每个已绑定楼栋一个数值（入住率 % 或未完成维修单数），未绑定的不出现 */
  @GetMapping("/overlay")
  public Result<List<Map<String, Object>>> overlay(@RequestParam String mode) {
    if (!Set.of("occupancy", "facility").contains(mode)) {
      throw new IllegalArgumentException("该模式没有楼栋着色");
    }
    requireMode(mode);
    List<Map<String, Object>> out = new ArrayList<>();
    for (Map.Entry<String, Long> e : boundBuildings().entrySet()) {
      List<Long> roomIds = roomIds(e.getValue());
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("assetId", e.getKey());
      if ("occupancy".equals(mode)) {
        long[] beds = bedCounts(roomIds);
        long usable = beds[0] + beds[1] + beds[2];
        row.put("value", usable == 0 ? null : Math.round(beds[1] * 100.0 / usable));
        row.put("text", usable == 0 ? "无床位" : beds[1] + "/" + usable + " 床");
      } else {
        long open = openMaintenance(roomIds);
        row.put("value", open);
        row.put("text", open + " 单未完成");
      }
      out.add(row);
    }
    return Result.ok(out);
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
    if (!"campus".equals(mode)) {
      requireMode(mode);
    }
    if (!"CAMPUS".equals(asset.getAssetType()) && !"BUILDING".equals(asset.getAssetType())) {
      throw new IllegalArgumentException("该资产类型暂不支持汇总");
    }
    boolean campus = "CAMPUS".equals(asset.getAssetType());
    Long buildingId = campus ? null : boundBuilding(asset);
    if (!campus && buildingId == null) {
      out.put("dataMode", "UNBOUND");
      out.put("message", "该楼栋尚未与楼栋主数据核实绑定，不显示数字");
      out.put("metrics", List.of());
      return Result.ok(out);
    }
    List<Long> roomIds = campus ? null : roomIds(buildingId);
    return Result.ok(switch (mode) {
      case "occupancy" -> occupancy(roomIds, out);
      case "fire" -> campus ? fire(out) : campusOnly(out, "消防点位与记录按文字地点登记，尚未与楼栋核实映射；请在园区层级查看全园口径", "/fire/overview");
      case "facility" -> facility(roomIds, out);
      case "video" -> campus ? video(out) : campusOnly(out, "摄像头只登记文字位置，尚未与楼栋核实映射；请在园区层级查看", "/fire/video");
      case "analytics" -> analytics(roomIds, out);
      default -> masterData(buildingId, out);
    });
  }

  // ---------- 管理员：从楼栋主数据同步 ----------

  /**
   * 为每栋尚未绑定的楼栋主数据生成一个空间资产并直接生效绑定（资产就是从这条主数据派生的，不存在对错位的风险）。
   * assetId 用 building.id 派生，楼栋改名不变；名称、楼层、房间每次读主数据。三维位置仍是示意布局。
   */
  @PostMapping("/admin/sync-buildings")
  @Transactional
  public Result<Map<String, Object>> syncBuildings() {
    requireBindingAdmin();
    int created = syncMissingBuildings();
    long buildings = buildingMapper.selectCount(Wrappers.lambdaQuery(Building.class)
        .eq(Building::getOrgId, orgId()).eq(Building::getIsDeleted, 0));
    return Result.ok(Map.of("created", created, "buildings", buildings));
  }

  /** shortcut: synchronized 只防单实例内并发重复创建；多实例部署时改用数据库唯一约束或分布式锁 */
  private synchronized int syncMissingBuildings() {
    Set<Long> bound = new HashSet<>(boundBuildings().values());
    DtSceneVersion scene = sceneMapper.selectOne(Wrappers.lambdaQuery(DtSceneVersion.class)
        .eq(DtSceneVersion::getOrgId, orgId()).eq(DtSceneVersion::getStatus, "PUBLISHED")
        .orderByDesc(DtSceneVersion::getPublishedAt).last("LIMIT 1"));
    List<Building> buildings = buildingMapper.selectList(Wrappers.lambdaQuery(Building.class)
        .eq(Building::getOrgId, orgId()).eq(Building::getIsDeleted, 0)
        .orderByAsc(Building::getSortNo).orderByAsc(Building::getId));
    int created = 0;
    for (Building b : buildings) {
      if (bound.contains(b.getId())) {
        continue;
      }
      String code = "GF-BLD-B" + b.getId();
      DtSpatialAsset asset = assetMapper.selectOne(Wrappers.lambdaQuery(DtSpatialAsset.class)
          .eq(DtSpatialAsset::getOrgId, orgId()).eq(DtSpatialAsset::getAssetCode, code));
      if (asset == null) {
        asset = new DtSpatialAsset();
        asset.setOrgId(orgId());
        asset.setAssetCode(code);
        asset.setName(b.getName());
        asset.setAssetType("BUILDING");
        asset.setParentCode(CAMPUS_CODE);
        asset.setModelNodeKey("BLD_M_" + b.getId());
        asset.setModelVersionId(scene == null ? null : scene.getId());
        asset.setVerificationStatus("MASTER_DATA");
        asset.setSourceReference("楼栋主数据 building#" + b.getId());
        asset.setSortNo(1000 + (b.getSortNo() == null ? 0 : b.getSortNo()));
        asset.setCreateTime(LocalDateTime.now());
        asset.setUpdateTime(LocalDateTime.now());
        assetMapper.insert(asset);
      }
      DtBusinessBinding binding = new DtBusinessBinding();
      binding.setOrgId(orgId());
      binding.setAssetCode(code);
      binding.setSourceModule("ASSET");
      binding.setEntityType("BUILDING");
      binding.setExternalEntityId(b.getId());
      binding.setBindingStatus("ACTIVE");
      binding.setCreatedBy(AuthContext.getStaffId());
      binding.setVerifiedBy(AuthContext.getStaffId());
      binding.setVerifiedAt(LocalDateTime.now());
      binding.setRemark("由楼栋主数据同步生成");
      binding.setCreateTime(LocalDateTime.now());
      bindingMapper.insert(binding);
      audit("DT_ASSET_SYNC", binding, code + " ← 楼栋主数据 " + b.getName());
      created++;
    }
    return created;
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

  private Map<String, Object> occupancy(List<Long> roomIds, Map<String, Object> out) {
    long[] c = bedCounts(roomIds);
    long usable = c[0] + c[1] + c[2];
    return live(out, roomIds, "床位主数据（按床位业务状态，不以总数减在住推算）", "/elder/bed-panorama", List.of(
        metric("totalBeds", "床位总数", c[0] + c[1] + c[2] + c[3], "床"),
        metric("occupancyRate", "入住率", usable == 0 ? null : Math.round(c[1] * 1000.0 / usable) / 10.0, "%"),
        metric("occupiedBeds", "在住", c[1], "床"),
        metric("availableBeds", "空闲", c[0], "床"),
        tone(metric("maintenanceBeds", "维修", c[2], "床"), c[2] > 0 ? "warn" : null),
        metric("disabledBeds", "停用", c[3], "床")));
  }

  private Map<String, Object> fire(Map<String, Object> out) {
    long points = fireLocationMapper.selectCount(Wrappers.lambdaQuery(FireLocation.class)
        .eq(FireLocation::getOrgId, orgId()).eq(FireLocation::getIsDeleted, 0).eq(FireLocation::getEnabled, 1));
    long highRisk = fireLocationMapper.selectCount(Wrappers.lambdaQuery(FireLocation.class)
        .eq(FireLocation::getOrgId, orgId()).eq(FireLocation::getIsDeleted, 0).eq(FireLocation::getEnabled, 1)
        .eq(FireLocation::getRiskLevel, "HIGH"));
    long open = fireRecords(null, "OPEN");
    long faults = fireRecords("FAULT_MAINTENANCE", "OPEN");
    long recent = fireRecordMapper.selectCount(Wrappers.lambdaQuery(FireSafetyRecord.class)
        .eq(FireSafetyRecord::getOrgId, orgId()).eq(FireSafetyRecord::getIsDeleted, 0)
        .ge(FireSafetyRecord::getCheckTime, LocalDateTime.now().minusDays(30)));
    return live(out, null, "消防点位台账与消防记录（只读）", "/fire/overview", List.of(
        tone(metric("openRecords", "未闭环记录", open, "条"), open > 0 ? "danger" : null),
        tone(metric("openFaults", "故障待修", faults, "条"), faults > 0 ? "danger" : null),
        metric("recentChecks", "近30天检查", recent, "次"),
        metric("points", "巡检点位", points, "个"),
        tone(metric("highRiskPoints", "高风险点位", highRisk, "个"), highRisk > 0 ? "warn" : null)));
  }

  private Map<String, Object> facility(List<Long> roomIds, Map<String, Object> out) {
    long open = maintenance(roomIds, List.of("OPEN"), false);
    long processing = maintenance(roomIds, List.of("PROCESSING"), false);
    long urgent = maintenance(roomIds, List.of("OPEN", "PROCESSING"), true);
    long done = roomIds != null && roomIds.isEmpty() ? 0 : maintenanceMapper.selectCount(Wrappers.lambdaQuery(MaintenanceRequest.class)
        .eq(MaintenanceRequest::getOrgId, orgId()).eq(MaintenanceRequest::getIsDeleted, 0)
        .eq(MaintenanceRequest::getStatus, "COMPLETED").in(roomIds != null, MaintenanceRequest::getRoomId, roomIds)
        .ge(MaintenanceRequest::getCompletedAt, LocalDateTime.now().minusDays(30)));
    List<Map<String, Object>> metrics = new ArrayList<>(List.of(
        tone(metric("openRequests", "待处理报修", open, "单"), open > 0 ? "warn" : null),
        metric("processingRequests", "维修中", processing, "单"),
        tone(metric("urgentRequests", "高优先级未完成", urgent, "单"), urgent > 0 ? "danger" : null),
        metric("completed30d", "近30天完成", done, "单")));
    if (roomIds == null) {
      // 智能设备只登记文字位置，只能给全园口径
      long devices = devices(null);
      long online = devices("ONLINE");
      metrics.add(metric("devices", "智能设备", devices, "台"));
      metrics.add(metric("onlineDevices", "在线", online, "台"));
      metrics.add(tone(metric("offlineDevices", "离线", devices("OFFLINE"), "台"), devices - online > 0 ? "warn" : null));
    }
    return live(out, roomIds, roomIds == null ? "报修工单 + 智能设备台账" : "报修工单（按房间归属到楼栋）",
        "/logistics/assets/maintenance-record", metrics);
  }

  private Map<String, Object> video(Map<String, Object> out) {
    long total = cameraMapper.selectCount(Wrappers.lambdaQuery(LogisticsCamera.class).eq(LogisticsCamera::getOrgId, orgId()));
    long enabled = cameraMapper.selectCount(Wrappers.lambdaQuery(LogisticsCamera.class)
        .eq(LogisticsCamera::getOrgId, orgId()).eq(LogisticsCamera::getEnabled, 1));
    out.put("message", "只汇总摄像头台账，三维页不播放视频画面；调阅请到视频模块并留痕");
    return live(out, null, "摄像头台账", "/fire/video", List.of(
        metric("cameras", "摄像头", total, "路"),
        metric("enabledCameras", "启用", enabled, "路"),
        tone(metric("disabledCameras", "停用", total - enabled, "路"), total - enabled > 0 ? "warn" : null)));
  }

  private Map<String, Object> analytics(List<Long> roomIds, Map<String, Object> out) {
    long[] c = bedCounts(roomIds);
    long usable = c[0] + c[1] + c[2];
    long openMaint = openMaintenance(roomIds);
    List<Map<String, Object>> metrics = new ArrayList<>(List.of(
        metric("occupancyRate", "入住率", usable == 0 ? null : Math.round(c[1] * 1000.0 / usable) / 10.0, "%"),
        metric("availableBeds", "可售空床", c[0], "床"),
        tone(metric("openMaintenance", "未完成维修", openMaint, "单"), openMaint > 0 ? "warn" : null)));
    if (roomIds == null) {
      long openFire = fireRecords(null, "OPEN");
      metrics.add(tone(metric("openFire", "消防未闭环", openFire, "条"), openFire > 0 ? "danger" : null));
    }
    return live(out, roomIds, "床位主数据 + 报修工单" + (roomIds == null ? " + 消防记录" : ""), "/stats/org/monthly-operation", metrics);
  }

  /** 园区总览：楼栋/楼层/房间数量直接来自主数据 */
  private Map<String, Object> masterData(Long buildingId, Map<String, Object> out) {
    List<Map<String, Object>> metrics = new ArrayList<>();
    if (buildingId == null) {
      metrics.add(metric("buildings", "楼栋", buildingMapper.selectCount(Wrappers.lambdaQuery(Building.class)
          .eq(Building::getOrgId, orgId()).eq(Building::getIsDeleted, 0)), "栋"));
    }
    metrics.add(metric("floors", "楼层", floorMapper.selectCount(Wrappers.lambdaQuery(Floor.class)
        .eq(Floor::getOrgId, orgId()).eq(Floor::getIsDeleted, 0).eq(buildingId != null, Floor::getBuildingId, buildingId)), "层"));
    metrics.add(metric("rooms", "房间", roomMapper.selectCount(Wrappers.lambdaQuery(Room.class)
        .eq(Room::getOrgId, orgId()).eq(Room::getIsDeleted, 0).eq(buildingId != null, Room::getBuildingId, buildingId)), "间"));
    return live(out, buildingId == null ? null : List.of(), "楼栋 / 楼层 / 房间主数据", "/logistics/assets/building-management", metrics);
  }

  private Map<String, Object> campusOnly(Map<String, Object> out, String message, String link) {
    out.put("dataMode", "NOT_CONNECTED");
    out.put("message", message);
    out.put("link", link);
    out.put("metrics", List.of());
    return out;
  }

  private static Map<String, Object> live(Map<String, Object> out, List<Long> roomIds, String source, String link,
      List<Map<String, Object>> metrics) {
    out.put("dataMode", "LIVE");
    out.put("source", source);
    out.put("scope", roomIds == null ? "本机构全园" : "已绑定楼栋");
    out.put("updatedAt", LocalDateTime.now());
    out.put("link", link);
    out.put("metrics", metrics);
    return out;
  }

  /** [空闲, 在住, 维修, 停用]；roomIds 为 null 表示全园，空列表表示该楼栋没有房间 */
  private long[] bedCounts(List<Long> roomIds) {
    int[] statuses = {BedStatus.AVAILABLE, BedStatus.OCCUPIED, BedStatus.MAINTENANCE, BedStatus.DISABLED};
    long[] out = new long[4];
    for (int i = 0; i < 4; i++) {
      int status = statuses[i];
      out[i] = roomIds != null && roomIds.isEmpty() ? 0 : bedMapper.selectCount(Wrappers.lambdaQuery(Bed.class)
          .eq(Bed::getOrgId, orgId()).eq(Bed::getIsDeleted, 0).eq(Bed::getStatus, status)
          .in(roomIds != null, Bed::getRoomId, roomIds));
    }
    return out;
  }

  private long openMaintenance(List<Long> roomIds) {
    return maintenance(roomIds, List.of("OPEN", "PROCESSING"), false);
  }

  private long maintenance(List<Long> roomIds, List<String> statuses, boolean highOnly) {
    if (roomIds != null && roomIds.isEmpty()) {
      return 0;
    }
    return maintenanceMapper.selectCount(Wrappers.lambdaQuery(MaintenanceRequest.class)
        .eq(MaintenanceRequest::getOrgId, orgId()).eq(MaintenanceRequest::getIsDeleted, 0)
        .in(MaintenanceRequest::getStatus, statuses).eq(highOnly, MaintenanceRequest::getPriority, "HIGH")
        .in(roomIds != null, MaintenanceRequest::getRoomId, roomIds));
  }

  private long fireRecords(String type, String status) {
    return fireRecordMapper.selectCount(Wrappers.lambdaQuery(FireSafetyRecord.class)
        .eq(FireSafetyRecord::getOrgId, orgId()).eq(FireSafetyRecord::getIsDeleted, 0)
        .eq(FireSafetyRecord::getStatus, status).eq(type != null, FireSafetyRecord::getRecordType, type));
  }

  private long devices(String onlineStatus) {
    return deviceMapper.selectCount(Wrappers.lambdaQuery(SmartDevice.class)
        .eq(SmartDevice::getOrgId, orgId()).eq(SmartDevice::getIsDeleted, 0)
        .eq(onlineStatus != null, SmartDevice::getOnlineStatus, onlineStatus));
  }

  private List<Long> roomIds(Long buildingId) {
    return roomMapper.selectList(Wrappers.lambdaQuery(Room.class).select(Room::getId)
        .eq(Room::getOrgId, orgId()).eq(Room::getBuildingId, buildingId).eq(Room::getIsDeleted, 0))
        .stream().map(Room::getId).toList();
  }

  /** assetCode → 已生效绑定的 building.id */
  private Map<String, Long> boundBuildings() {
    Map<String, Long> out = new HashMap<>();
    for (DtBusinessBinding b : activeBindings(null)) {
      if ("BUILDING".equals(b.getEntityType())) {
        out.put(b.getAssetCode(), b.getExternalEntityId());
      }
    }
    return out;
  }

  private Long boundBuilding(DtSpatialAsset asset) {
    return "BUILDING".equals(asset.getAssetType()) ? boundBuildings().get(asset.getAssetCode()) : null;
  }

  private static <T> Map<Long, Long> countBy(List<T> rows, Function<T, Long> key) {
    return rows.stream().filter(r -> key.apply(r) != null).collect(Collectors.groupingBy(key, Collectors.counting()));
  }

  private static Map<String, Object> metric(String key, String label, Object value, String unit) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("key", key);
    m.put("label", label);
    m.put("value", value);
    m.put("unit", unit);
    return m;
  }

  private static Map<String, Object> tone(Map<String, Object> metric, String tone) {
    if (tone != null) {
      metric.put("tone", tone);
    }
    return metric;
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

  private static boolean canView(String mode) {
    return MODE_ROLES.getOrDefault(mode, Set.of()).stream().anyMatch(AuthContext::hasRole);
  }

  private static void requireMode(String mode) {
    if (!canView(mode)) {
      throw new AccessDeniedException("当前岗位无权查看该模式的汇总");
    }
  }

  private static void requireBindingAdmin() {
    if (!canManageBindings()) {
      throw new AccessDeniedException("只有管理员可以配置空间绑定");
    }
  }
}
