package com.zhiyangyun.care.asset.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.asset.entity.Building;
import com.zhiyangyun.care.asset.entity.Floor;
import com.zhiyangyun.care.asset.mapper.BuildingMapper;
import com.zhiyangyun.care.asset.mapper.FloorMapper;
import com.zhiyangyun.care.asset.model.ResidenceBatchCommitRequest;
import com.zhiyangyun.care.asset.model.ResidenceBatchCommitResponse;
import com.zhiyangyun.care.asset.model.ResidenceBatchGenerationRequest;
import com.zhiyangyun.care.asset.model.ResidenceBatchPreviewItem;
import com.zhiyangyun.care.asset.model.ResidenceBatchPreviewResponse;
import com.zhiyangyun.care.asset.model.ResidenceGenerationFloorRule;
import com.zhiyangyun.care.asset.model.ResidenceGenerationSpecialRoomRule;
import com.zhiyangyun.care.asset.service.ResidenceBatchGenerationService;
import com.zhiyangyun.care.baseconfig.entity.BaseDataItem;
import com.zhiyangyun.care.baseconfig.mapper.BaseDataItemMapper;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderBedRelation;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderBedRelationMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.elder.util.QrCodeUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResidenceBatchGenerationServiceImpl implements ResidenceBatchGenerationService {
  private static final Duration PREVIEW_TTL = Duration.ofMinutes(30);
  private static final Map<String, CachedPreview> PREVIEW_CACHE = new ConcurrentHashMap<>();

  private final BuildingMapper buildingMapper;
  private final FloorMapper floorMapper;
  private final RoomMapper roomMapper;
  private final BedMapper bedMapper;
  private final ElderBedRelationMapper elderBedRelationMapper;
  private final BaseDataItemMapper baseDataItemMapper;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public ResidenceBatchGenerationServiceImpl(
      BuildingMapper buildingMapper,
      FloorMapper floorMapper,
      RoomMapper roomMapper,
      BedMapper bedMapper,
      ElderBedRelationMapper elderBedRelationMapper,
      BaseDataItemMapper baseDataItemMapper) {
    this.buildingMapper = buildingMapper;
    this.floorMapper = floorMapper;
    this.roomMapper = roomMapper;
    this.bedMapper = bedMapper;
    this.elderBedRelationMapper = elderBedRelationMapper;
    this.baseDataItemMapper = baseDataItemMapper;
  }

  @Override
  public ResidenceBatchPreviewResponse preview(Long tenantId, Long operatorId, ResidenceBatchGenerationRequest request) {
    if (tenantId == null) {
      throw new IllegalArgumentException("机构信息缺失，无法预览批量生成");
    }
    purgeExpiredPreview();
    Snapshot snapshot = loadSnapshot(tenantId);
    GenerationPlan plan = buildPlan(tenantId, operatorId, request, snapshot);
    String previewToken = UUID.randomUUID().toString();
    PREVIEW_CACHE.put(previewToken, new CachedPreview(tenantId, operatorId, Instant.now(), plan));
    ResidenceBatchPreviewResponse response = plan.response;
    response.setPreviewToken(previewToken);
    return response;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ResidenceBatchCommitResponse commit(Long tenantId, Long operatorId, ResidenceBatchCommitRequest request) {
    if (tenantId == null) {
      throw new IllegalArgumentException("机构信息缺失，无法提交批量生成");
    }
    purgeExpiredPreview();
    CachedPreview cachedPreview = PREVIEW_CACHE.remove(request.getPreviewToken());
    if (cachedPreview == null || !Objects.equals(cachedPreview.tenantId, tenantId)) {
      throw new IllegalArgumentException("预览已失效，请重新预览后再提交");
    }
    GenerationPlan plan = cachedPreview.plan;
    ResidenceBatchCommitResponse response = new ResidenceBatchCommitResponse();
    Map<String, Long> createdBuildingIds = new HashMap<>();
    Map<String, Long> createdFloorIds = new HashMap<>();
    Map<String, Long> createdRoomIds = new HashMap<>();

    for (BuildingPlan item : plan.buildings) {
      if ("CREATE".equals(item.action)) {
        Building building = new Building();
        building.setTenantId(tenantId);
        building.setOrgId(tenantId);
        building.setName(item.name);
        building.setCode(item.code);
        building.setStatus(1);
        building.setSortNo(item.sortNo);
        building.setCreatedBy(operatorId);
        buildingMapper.insert(building);
        createdBuildingIds.put(item.planKey, building.getId());
        response.setCreatedBuildingCount(response.getCreatedBuildingCount() + 1);
      } else {
        response.setSkippedCount(response.getSkippedCount() + 1);
      }
    }

    for (FloorPlan item : plan.floors) {
      Long buildingId = item.existingBuildingId != null ? item.existingBuildingId : createdBuildingIds.get(item.buildingPlanKey);
      if (buildingId == null) {
        continue;
      }
      if ("CREATE".equals(item.action)) {
        Floor floor = new Floor();
        floor.setTenantId(tenantId);
        floor.setOrgId(tenantId);
        floor.setBuildingId(buildingId);
        floor.setFloorNo(item.floorNo);
        floor.setName(item.name);
        floor.setStatus(1);
        floor.setSortNo(item.sortNo);
        floor.setCreatedBy(operatorId);
        floorMapper.insert(floor);
        createdFloorIds.put(item.planKey, floor.getId());
        response.setCreatedFloorCount(response.getCreatedFloorCount() + 1);
      } else {
        if (item.existingFloorId != null) {
          createdFloorIds.put(item.planKey, item.existingFloorId);
        }
        response.setSkippedCount(response.getSkippedCount() + 1);
      }
    }

    for (RoomPlan item : plan.rooms) {
      Long buildingId = item.existingBuildingId != null ? item.existingBuildingId : createdBuildingIds.get(item.buildingPlanKey);
      Long floorId = item.existingFloorId != null ? item.existingFloorId : createdFloorIds.get(item.floorPlanKey);
      if (buildingId == null || floorId == null) {
        continue;
      }
      if ("CREATE".equals(item.action)) {
        Room room = new Room();
        room.setTenantId(tenantId);
        room.setOrgId(tenantId);
        room.setBuildingId(buildingId);
        room.setFloorId(floorId);
        room.setBuilding(item.buildingName);
        room.setFloorNo(item.floorNo);
        room.setRoomNo(item.roomNo);
        room.setRoomType(item.roomType);
        room.setCapacity(item.capacity);
        room.setStatus(1);
        room.setRoomQrCode(QrCodeUtil.generate());
        room.setRemark(item.remark);
        room.setCreatedBy(operatorId);
        roomMapper.insert(room);
        createdRoomIds.put(item.planKey, room.getId());
        response.setCreatedRoomCount(response.getCreatedRoomCount() + 1);
      } else if ("OVERWRITE_SAFE".equals(item.action) && item.existingRoomId != null) {
        Room room = roomMapper.selectById(item.existingRoomId);
        if (room != null && isRoomSafeToOverwrite(tenantId, room.getId())) {
          room.setRoomType(item.roomType);
          room.setCapacity(item.capacity);
          room.setRemark(item.remark);
          room.setStatus(1);
          roomMapper.updateById(room);
          createdRoomIds.put(item.planKey, room.getId());
          response.setUpdatedRoomCount(response.getUpdatedRoomCount() + 1);
        } else {
          response.getMessages().add("房间已失去安全覆盖条件，已跳过：" + item.roomNo);
          response.setSkippedCount(response.getSkippedCount() + 1);
        }
      } else {
        if (item.existingRoomId != null) {
          createdRoomIds.put(item.planKey, item.existingRoomId);
        }
        response.setSkippedCount(response.getSkippedCount() + 1);
      }
    }

    for (BedPlan item : plan.beds) {
      Long roomId = item.existingRoomId != null ? item.existingRoomId : createdRoomIds.get(item.roomPlanKey);
      if (roomId == null) {
        continue;
      }
      if ("CREATE".equals(item.action)) {
        Bed bed = new Bed();
        bed.setTenantId(tenantId);
        bed.setOrgId(tenantId);
        bed.setRoomId(roomId);
        bed.setBedNo(item.bedNo);
        bed.setBedType(item.bedType);
        bed.setBedQrCode(QrCodeUtil.generate());
        bed.setStatus(1);
        bed.setCreatedBy(operatorId);
        bedMapper.insert(bed);
        response.setCreatedBedCount(response.getCreatedBedCount() + 1);
      } else if ("OVERWRITE_SAFE".equals(item.action) && item.existingBedId != null) {
        Bed bed = bedMapper.selectById(item.existingBedId);
        if (bed != null && isBedSafeToOverwrite(tenantId, bed.getId())) {
          bed.setBedType(item.bedType);
          bed.setStatus(1);
          bedMapper.updateById(bed);
          response.setUpdatedBedCount(response.getUpdatedBedCount() + 1);
        } else {
          response.getMessages().add("床位已失去安全覆盖条件，已跳过：" + item.bedNo);
          response.setSkippedCount(response.getSkippedCount() + 1);
        }
      } else {
        response.setSkippedCount(response.getSkippedCount() + 1);
      }
    }
    response.getMessages().add("批量生成已完成，请刷新房态图和床位管理列表查看结果");
    return response;
  }

  private GenerationPlan buildPlan(Long tenantId, Long operatorId, ResidenceBatchGenerationRequest request, Snapshot snapshot) {
    GenerationPlan plan = new GenerationPlan();
    plan.response.setMode(normalizeMode(request.getMode()));
    plan.response.setStrategy(normalizeStrategy(request.getStrategy()));
    String mode = plan.response.getMode();
    if ("BUILDING_ONLY".equals(mode)) {
      planBuildingOnly(request, snapshot, plan);
    } else if ("FULL_INIT".equals(mode)) {
      planFullInitialization(tenantId, request, snapshot, plan);
    } else if ("FLOOR_ONLY".equals(mode)) {
      planFloorsOnly(tenantId, request, snapshot, plan);
    } else if ("ROOM_ONLY".equals(mode)) {
      planRoomsOnly(tenantId, request, snapshot, plan);
    } else if ("BED_ONLY".equals(mode)) {
      planBedsOnly(tenantId, request, snapshot, plan);
    } else {
      throw new IllegalArgumentException("不支持的生成模式：" + request.getMode());
    }
    sortPreviewItems(plan.response.getItems());
    return plan;
  }

  private void planBuildingOnly(ResidenceBatchGenerationRequest request, Snapshot snapshot, GenerationPlan plan) {
    List<String> buildingNames = resolveBuildingNames(request);
    if (buildingNames.isEmpty()) {
      throw new IllegalArgumentException("请至少配置一个楼栋");
    }
    for (int i = 0; i < buildingNames.size(); i++) {
      String buildingName = buildingNames.get(i);
      String buildingCode = resolveBuildingCode(request, buildingName, i + (request.getBuildingStartNo() == null ? 1 : request.getBuildingStartNo()));
      String buildingPlanKey = "BUILDING:" + buildingName;
      Building existing = snapshot.buildingByName.get(buildingName);
      if (existing == null && snapshot.buildingByCode.containsKey(buildingCode)) {
        addConflict(plan, "BUILDING", buildingName, null, "楼栋编码冲突：" + buildingCode);
        continue;
      }
      if (existing != null) {
        plan.buildings.add(new BuildingPlan("SKIP", buildingPlanKey, existing.getId(), buildingName, buildingCode, safeSortNo(i + 1)));
        addSkip(plan, "BUILDING", buildingName, null, "楼栋已存在，按策略跳过");
      } else {
        plan.buildings.add(new BuildingPlan("CREATE", buildingPlanKey, null, buildingName, buildingCode, safeSortNo(i + 1)));
        addCreate(plan, "BUILDING", buildingName, null, "将新增楼栋");
      }
    }
  }

  private void planFullInitialization(Long tenantId, ResidenceBatchGenerationRequest request, Snapshot snapshot, GenerationPlan plan) {
    List<String> buildingNames = resolveBuildingNames(request);
    if (buildingNames.isEmpty()) {
      throw new IllegalArgumentException("请至少配置一个楼栋");
    }
    for (int i = 0; i < buildingNames.size(); i++) {
      String buildingName = buildingNames.get(i);
      String buildingCode = resolveBuildingCode(request, buildingName, i + (request.getBuildingStartNo() == null ? 1 : request.getBuildingStartNo()));
      String buildingPlanKey = "BUILDING:" + buildingName;
      Building existing = snapshot.buildingByName.get(buildingName);
      if (existing == null && snapshot.buildingByCode.containsKey(buildingCode)) {
        addConflict(plan, "BUILDING", buildingName, null, "楼栋编码冲突：" + buildingCode);
        continue;
      }
      BuildingPlan buildingPlan;
      if (existing != null) {
        buildingPlan = new BuildingPlan("SKIP", buildingPlanKey, existing.getId(), buildingName, buildingCode, safeSortNo(i + 1));
        addSkip(plan, "BUILDING", buildingName, null, "楼栋已存在，按策略跳过");
      } else {
        buildingPlan = new BuildingPlan("CREATE", buildingPlanKey, null, buildingName, buildingCode, safeSortNo(i + 1));
        addCreate(plan, "BUILDING", buildingName, null, "将新增楼栋");
      }
      plan.buildings.add(buildingPlan);
      planFloorsForBuilding(request, snapshot, plan, buildingPlan, buildingName, existing == null ? null : existing.getId());
    }
  }

  private void planFloorsOnly(Long tenantId, ResidenceBatchGenerationRequest request, Snapshot snapshot, GenerationPlan plan) {
    Building building = resolveTargetBuilding(request, snapshot);
    BuildingPlan buildingPlan = new BuildingPlan("SKIP", "BUILDING:" + building.getName(), building.getId(), building.getName(), building.getCode(), building.getSortNo());
    planFloorsForBuilding(request, snapshot, plan, buildingPlan, building.getName(), building.getId());
  }

  private void planRoomsOnly(Long tenantId, ResidenceBatchGenerationRequest request, Snapshot snapshot, GenerationPlan plan) {
    Floor floor = resolveTargetFloor(request, snapshot);
    Building building = snapshot.buildingById.get(floor.getBuildingId());
    BuildingPlan buildingPlan = new BuildingPlan("SKIP", "BUILDING:" + (building == null ? floor.getBuildingId() : building.getName()), floor.getBuildingId(), building == null ? "目标楼栋" : building.getName(), building == null ? "" : building.getCode(), building == null ? 0 : building.getSortNo());
    FloorPlan floorPlan = new FloorPlan("SKIP", buildingPlan.planKey + "/FLOOR:" + floor.getFloorNo(), floor.getId(), buildingPlan.planKey, floor.getBuildingId(), floor.getFloorNo(), defaultFloorName(floor.getFloorNo()), extractFloorNumber(floor.getFloorNo()));
    planRoomsForFloor(request, snapshot, plan, buildingPlan, floorPlan, building == null ? "目标楼栋" : building.getName(), floor);
  }

  private void planBedsOnly(Long tenantId, ResidenceBatchGenerationRequest request, Snapshot snapshot, GenerationPlan plan) {
    Room room = resolveTargetRoom(request, snapshot);
    Floor floor = snapshot.floorById.get(room.getFloorId());
    Building building = snapshot.buildingById.get(room.getBuildingId());
    BuildingPlan buildingPlan = new BuildingPlan("SKIP", "BUILDING:" + room.getBuilding(), room.getBuildingId(), room.getBuilding(), building == null ? "" : building.getCode(), building == null ? 0 : building.getSortNo());
    FloorPlan floorPlan = new FloorPlan("SKIP", buildingPlan.planKey + "/FLOOR:" + room.getFloorNo(), room.getFloorId(), buildingPlan.planKey, room.getBuildingId(), room.getFloorNo(), defaultFloorName(room.getFloorNo()), extractFloorNumber(room.getFloorNo()));
    RoomPlan roomPlan = new RoomPlan("SKIP", floorPlan.planKey + "/ROOM:" + room.getRoomNo(), room.getId(), buildingPlan.planKey, floorPlan.planKey, room.getBuildingId(), room.getFloorId(), room.getBuilding(), room.getFloorNo(), room.getRoomNo(), room.getRoomType(), resolveRoomCapacity(room), room.getRemark());
    planBedsForRoom(request, snapshot, plan, roomPlan);
  }

  private void planFloorsForBuilding(
      ResidenceBatchGenerationRequest request,
      Snapshot snapshot,
      GenerationPlan plan,
      BuildingPlan buildingPlan,
      String buildingName,
      Long existingBuildingId) {
    int start = request.getFloorStartNo() == null ? 1 : request.getFloorStartNo();
    int end = request.getFloorEndNo() == null ? start : request.getFloorEndNo();
    for (int floorNo = start; floorNo <= end; floorNo++) {
      String floorCode = formatFloorNo(floorNo, request.getFloorNameStyle());
      String floorPlanKey = buildingPlan.planKey + "/FLOOR:" + floorCode;
      Floor existing = existingBuildingId == null ? null : snapshot.floorByBuildingAndNo.get(keyOf(existingBuildingId, floorCode));
      FloorPlan floorPlan;
      if (existing != null) {
        floorPlan = new FloorPlan("SKIP", floorPlanKey, existing.getId(), buildingPlan.planKey, existingBuildingId, floorCode, defaultFloorName(floorCode), floorNo);
        addSkip(plan, "FLOOR", floorCode, buildingName, "楼层已存在，按策略跳过");
      } else {
        floorPlan = new FloorPlan("CREATE", floorPlanKey, null, buildingPlan.planKey, existingBuildingId, floorCode, defaultFloorName(floorCode), floorNo);
        addCreate(plan, "FLOOR", floorCode, buildingName, "将新增楼层");
      }
      plan.floors.add(floorPlan);
      ResidenceGenerationFloorRule floorRule = resolveFloorRule(request, floorNo);
      if (Boolean.TRUE.equals(floorRule == null ? false : floorRule.getSkipRoomGeneration())) {
        plan.response.getWarnings().add(floorCode + " 已配置为仅生成楼层，不生成房间");
        continue;
      }
      Floor contextFloor = existing != null ? existing : null;
      planRoomsForFloor(request, snapshot, plan, buildingPlan, floorPlan, buildingName, contextFloor);
    }
  }

  private void planRoomsForFloor(
      ResidenceBatchGenerationRequest request,
      Snapshot snapshot,
      GenerationPlan plan,
      BuildingPlan buildingPlan,
      FloorPlan floorPlan,
      String buildingName,
      Floor existingFloor) {
    int floorNoNumeric = floorPlan.sortNo;
    int roomStart = request.getRoomStartNo() == null ? 1 : request.getRoomStartNo();
    int roomEnd = request.getRoomEndNo() == null ? roomStart : request.getRoomEndNo();
    ResidenceGenerationFloorRule floorRule = resolveFloorRule(request, floorNoNumeric);
    Set<String> excluded = floorRule == null ? Set.of() : floorRule.getExcludedRoomNos().stream().filter(Objects::nonNull).collect(Collectors.toSet());
    for (int roomSeq = roomStart; roomSeq <= roomEnd; roomSeq++) {
      String roomNo = buildRoomNo(buildingPlan.code, floorNoNumeric, roomSeq, request.getRoomSeqWidth(), request.getRoomType());
      if (excluded.contains(roomNo)) {
        addSkip(plan, "ROOM", roomNo, floorPlan.floorNo, "该房号已在楼层规则中标记为跳过");
        continue;
      }
      ResidenceGenerationSpecialRoomRule specialRoom = resolveSpecialRoomRule(request, floorNoNumeric, roomNo);
      String roomPlanKey = floorPlan.planKey + "/ROOM:" + roomNo;
      Room existing = snapshot.roomByNo.get(roomNo);
      String roomType = normalizeValue(specialRoom == null ? null : specialRoom.getRoomType(),
          normalizeValue(floorRule == null ? null : floorRule.getRoomType(), normalizeValue(request.getRoomType(), resolveDefaultRoomType(snapshot.tenantId))));
      int bedCount = resolveBedCount(roomType, specialRoom == null ? null : specialRoom.getBedCount(), request.getBedsPerRoom(), specialRoom == null ? null : specialRoom.getUsageType(), specialRoom != null && Boolean.TRUE.equals(specialRoom.getSkipBedGeneration()));
      String remark = buildRoomRemark(specialRoom == null ? null : specialRoom.getUsageType(),
          normalizeValue(specialRoom == null ? null : specialRoom.getGenderLimit(), floorRule == null ? request.getDefaultGenderLimit() : normalizeValue(floorRule.getGenderLimit(), request.getDefaultGenderLimit())),
          true,
          bedCount);
      RoomPlan roomPlan;
      if (existing == null) {
        roomPlan = new RoomPlan("CREATE", roomPlanKey, null, buildingPlan.planKey, floorPlan.planKey, buildingPlan.existingBuildingId, floorPlan.existingFloorId, buildingName, floorPlan.floorNo, roomNo, roomType, Math.max(bedCount, 0), remark);
        addCreate(plan, "ROOM", roomNo, floorPlan.floorNo, "将新增房间");
      } else {
        String strategy = plan.response.getStrategy();
        boolean safeOverwrite = "OVERWRITE_SAFE".equals(strategy) && isRoomSafeToOverwrite(snapshot.tenantId, existing.getId());
        if (safeOverwrite) {
          roomPlan = new RoomPlan("OVERWRITE_SAFE", roomPlanKey, existing.getId(), buildingPlan.planKey, floorPlan.planKey, existing.getBuildingId(), existing.getFloorId(), existing.getBuilding(), existing.getFloorNo(), existing.getRoomNo(), roomType, Math.max(bedCount, 0), remark);
          addSafeOverwrite(plan, "ROOM", roomNo, floorPlan.floorNo, "房间已存在且无入住数据，可安全覆盖");
        } else {
          roomPlan = new RoomPlan("SKIP", roomPlanKey, existing.getId(), buildingPlan.planKey, floorPlan.planKey, existing.getBuildingId(), existing.getFloorId(), existing.getBuilding(), existing.getFloorNo(), existing.getRoomNo(), existing.getRoomType(), resolveRoomCapacity(existing), existing.getRemark());
          addSkip(plan, "ROOM", roomNo, floorPlan.floorNo, "房间已存在，按策略跳过");
        }
      }
      plan.rooms.add(roomPlan);
      if (bedCount <= 0) {
        plan.response.getWarnings().add(roomNo + " 配置为 0 床房间，不生成床位");
        continue;
      }
      planBedsForRoom(request, snapshot, plan, roomPlan);
    }
  }

  private void planBedsForRoom(ResidenceBatchGenerationRequest request, Snapshot snapshot, GenerationPlan plan, RoomPlan roomPlan) {
    int bedCount = roomPlan.capacity == null ? 0 : roomPlan.capacity;
    for (int index = 1; index <= bedCount; index++) {
      String label = formatBedLabel(index, request.getBedLabelStyle());
      String bedNo = roomPlan.roomNo + normalizeValue(request.getBedNoSeparator(), "-") + label;
      String bedPlanKey = roomPlan.planKey + "/BED:" + bedNo;
      Bed existing = roomPlan.existingRoomId == null ? null : snapshot.bedByRoomAndNo.get(keyOf(roomPlan.existingRoomId, bedNo));
      String bedType = normalizeValue(request.getDefaultBedType(), resolveDefaultBedType(snapshot.tenantId));
      if (existing == null) {
        plan.beds.add(new BedPlan("CREATE", bedPlanKey, null, roomPlan.planKey, roomPlan.existingRoomId, bedNo, bedType));
        addCreate(plan, "BED", bedNo, roomPlan.roomNo, "将新增床位");
      } else {
        String strategy = plan.response.getStrategy();
        boolean safeOverwrite = "OVERWRITE_SAFE".equals(strategy) && isBedSafeToOverwrite(snapshot.tenantId, existing.getId());
        if (safeOverwrite) {
          plan.beds.add(new BedPlan("OVERWRITE_SAFE", bedPlanKey, existing.getId(), roomPlan.planKey, roomPlan.existingRoomId, bedNo, bedType));
          addSafeOverwrite(plan, "BED", bedNo, roomPlan.roomNo, "床位已存在且未入住，可安全覆盖");
        } else {
          plan.beds.add(new BedPlan("SKIP", bedPlanKey, existing.getId(), roomPlan.planKey, roomPlan.existingRoomId, bedNo, existing.getBedType()));
          addSkip(plan, "BED", bedNo, roomPlan.roomNo, "床位已存在，按策略跳过");
        }
      }
    }
  }

  private Snapshot loadSnapshot(Long tenantId) {
    Snapshot snapshot = new Snapshot();
    snapshot.tenantId = tenantId;
    snapshot.buildings = buildingMapper.selectList(Wrappers.lambdaQuery(Building.class)
        .eq(Building::getIsDeleted, 0)
        .eq(Building::getTenantId, tenantId)
        .orderByAsc(Building::getSortNo)
        .orderByAsc(Building::getName));
    snapshot.floors = floorMapper.selectList(Wrappers.lambdaQuery(Floor.class)
        .eq(Floor::getIsDeleted, 0)
        .eq(Floor::getTenantId, tenantId)
        .orderByAsc(Floor::getSortNo)
        .orderByAsc(Floor::getFloorNo));
    snapshot.rooms = roomMapper.selectList(Wrappers.lambdaQuery(Room.class)
        .eq(Room::getIsDeleted, 0)
        .eq(Room::getTenantId, tenantId)
        .orderByAsc(Room::getRoomNo));
    snapshot.beds = bedMapper.selectList(Wrappers.lambdaQuery(Bed.class)
        .eq(Bed::getIsDeleted, 0)
        .eq(Bed::getTenantId, tenantId)
        .orderByAsc(Bed::getRoomId)
        .orderByAsc(Bed::getBedNo));
    snapshot.activeRelations = elderBedRelationMapper.selectList(Wrappers.lambdaQuery(ElderBedRelation.class)
        .eq(ElderBedRelation::getIsDeleted, 0)
        .eq(ElderBedRelation::getTenantId, tenantId)
        .eq(ElderBedRelation::getActiveFlag, 1));

    snapshot.buildingById = snapshot.buildings.stream().collect(Collectors.toMap(Building::getId, item -> item));
    snapshot.buildingByName = snapshot.buildings.stream().collect(Collectors.toMap(Building::getName, item -> item, (a, b) -> a));
    snapshot.buildingByCode = snapshot.buildings.stream().filter(item -> item.getCode() != null && !item.getCode().isBlank())
        .collect(Collectors.toMap(Building::getCode, item -> item, (a, b) -> a));
    snapshot.floorById = snapshot.floors.stream().collect(Collectors.toMap(Floor::getId, item -> item));
    snapshot.floorByBuildingAndNo = snapshot.floors.stream().collect(Collectors.toMap(item -> keyOf(item.getBuildingId(), item.getFloorNo()), item -> item, (a, b) -> a));
    snapshot.roomById = snapshot.rooms.stream().collect(Collectors.toMap(Room::getId, item -> item));
    snapshot.roomByNo = snapshot.rooms.stream().collect(Collectors.toMap(Room::getRoomNo, item -> item, (a, b) -> a));
    snapshot.bedByRoomAndNo = snapshot.beds.stream().collect(Collectors.toMap(item -> keyOf(item.getRoomId(), item.getBedNo()), item -> item, (a, b) -> a));
    snapshot.roomHasActiveOccupancy = snapshot.activeRelations.stream()
        .map(ElderBedRelation::getBedId)
        .filter(Objects::nonNull)
        .map(bedId -> snapshot.beds.stream().filter(bed -> Objects.equals(bed.getId(), bedId)).findFirst().orElse(null))
        .filter(Objects::nonNull)
        .collect(Collectors.groupingBy(Bed::getRoomId, Collectors.counting()))
        .entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() > 0));
    return snapshot;
  }

  private Building resolveTargetBuilding(ResidenceBatchGenerationRequest request, Snapshot snapshot) {
    if (request.getBuildingId() == null) {
      throw new IllegalArgumentException("请先选择目标楼栋");
    }
    Building building = snapshot.buildingById.get(request.getBuildingId());
    if (building == null) {
      throw new IllegalArgumentException("目标楼栋不存在");
    }
    return building;
  }

  private Floor resolveTargetFloor(ResidenceBatchGenerationRequest request, Snapshot snapshot) {
    if (request.getFloorId() == null) {
      throw new IllegalArgumentException("请先选择目标楼层");
    }
    Floor floor = snapshot.floorById.get(request.getFloorId());
    if (floor == null) {
      throw new IllegalArgumentException("目标楼层不存在");
    }
    return floor;
  }

  private Room resolveTargetRoom(ResidenceBatchGenerationRequest request, Snapshot snapshot) {
    if (request.getRoomId() == null) {
      throw new IllegalArgumentException("请先选择目标房间");
    }
    Room room = snapshot.roomById.get(request.getRoomId());
    if (room == null) {
      throw new IllegalArgumentException("目标房间不存在");
    }
    return room;
  }

  private boolean isRoomSafeToOverwrite(Long tenantId, Long roomId) {
    if (roomId == null) {
      return false;
    }
    long occupiedCount = bedMapper.selectCount(Wrappers.lambdaQuery(Bed.class)
        .eq(Bed::getIsDeleted, 0)
        .eq(Bed::getTenantId, tenantId)
        .eq(Bed::getRoomId, roomId)
        .and(wrapper -> wrapper.eq(Bed::getStatus, 2).or().isNotNull(Bed::getElderId)));
    return occupiedCount == 0;
  }

  private boolean isBedSafeToOverwrite(Long tenantId, Long bedId) {
    if (bedId == null) {
      return false;
    }
    Bed bed = bedMapper.selectById(bedId);
    if (bed == null || !Objects.equals(bed.getTenantId(), tenantId)) {
      return false;
    }
    if (bed.getElderId() != null || Integer.valueOf(2).equals(bed.getStatus())) {
      return false;
    }
    return elderBedRelationMapper.selectCount(Wrappers.lambdaQuery(ElderBedRelation.class)
        .eq(ElderBedRelation::getIsDeleted, 0)
        .eq(ElderBedRelation::getTenantId, tenantId)
        .eq(ElderBedRelation::getBedId, bedId)
        .eq(ElderBedRelation::getActiveFlag, 1)) == 0;
  }

  private List<String> resolveBuildingNames(ResidenceBatchGenerationRequest request) {
    if (request.getBuildingNames() != null && !request.getBuildingNames().isEmpty()) {
      return request.getBuildingNames().stream().filter(Objects::nonNull).map(String::trim).filter(item -> !item.isBlank()).toList();
    }
    int count = request.getBuildingCount() == null ? 1 : request.getBuildingCount();
    int startNo = request.getBuildingStartNo() == null ? 1 : request.getBuildingStartNo();
    List<String> result = new ArrayList<>();
    String style = normalizeValue(request.getBuildingNameStyle(), "NUMERIC_SUFFIX");
    for (int i = 0; i < count; i++) {
      int current = startNo + i;
      if ("ALPHA_SUFFIX".equalsIgnoreCase(style)) {
        result.add(indexToAlpha(current) + "栋");
      } else if ("ALPHA".equalsIgnoreCase(style)) {
        result.add(indexToAlpha(current));
      } else if ("NUMERIC".equalsIgnoreCase(style)) {
        result.add(String.valueOf(current));
      } else {
        result.add(current + "号楼");
      }
    }
    return result;
  }

  private String resolveBuildingCode(ResidenceBatchGenerationRequest request, String buildingName, int currentNo) {
    String normalizedStyle = normalizeValue(request.getBuildingNameStyle(), "NUMERIC_SUFFIX");
    if ("ALPHA_SUFFIX".equalsIgnoreCase(normalizedStyle) || "ALPHA".equalsIgnoreCase(normalizedStyle)) {
      return indexToAlpha(currentNo);
    }
    return normalizeValue(request.getBuildingCodePrefix(), "B") + String.format("%02d", currentNo);
  }

  private ResidenceGenerationFloorRule resolveFloorRule(ResidenceBatchGenerationRequest request, int floorNo) {
    if (request.getFloorRules() == null) {
      return null;
    }
    return request.getFloorRules().stream()
        .filter(item -> item != null && Objects.equals(item.getFloorNo(), floorNo))
        .findFirst()
        .orElse(null);
  }

  private ResidenceGenerationSpecialRoomRule resolveSpecialRoomRule(ResidenceBatchGenerationRequest request, int floorNo, String roomNo) {
    if (request.getSpecialRooms() == null) {
      return null;
    }
    return request.getSpecialRooms().stream()
        .filter(item -> item != null && Objects.equals(item.getFloorNo(), floorNo) && Objects.equals(item.getRoomNo(), roomNo))
        .findFirst()
        .orElse(null);
  }

  private String buildRoomNo(String buildingCode, int floorNo, int roomSeq, Integer roomSeqWidth, String roomType) {
    int width = roomSeqWidth == null ? 2 : roomSeqWidth;
    String buildingPrefix = normalizeBuildingCode(buildingCode);
    String floorPrefix = String.valueOf(floorNo);
    String functionalCode = resolveFunctionalRoomCode(roomType);
    if (functionalCode != null) {
      return buildingPrefix + floorPrefix + functionalCode + roomSeq;
    }
    return buildingPrefix + floorPrefix + String.format("%0" + width + "d", roomSeq);
  }

  private String normalizeBuildingCode(String code) {
    if (code == null || code.isBlank()) {
      return "A";
    }
    String normalized = code.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
    return normalized.isBlank() ? "A" : normalized;
  }

  private String resolveFunctionalRoomCode(String roomType) {
    if (roomType == null || roomType.isBlank()) {
      return null;
    }
    String normalized = roomType.trim().toUpperCase(Locale.ROOT);
    if (normalized.contains("NURSING") || normalized.contains("STATION") || roomType.contains("护理站")) {
      return "N";
    }
    if (normalized.contains("WATER") || roomType.contains("开水房")) {
      return "W";
    }
    if (normalized.contains("LAUNDRY") || roomType.contains("洗衣房")) {
      return "L";
    }
    if (normalized.contains("TOILET") || normalized.contains("WC") || roomType.contains("卫生间") || roomType.contains("厕所")) {
      return "T";
    }
    if (normalized.contains("BATH") || roomType.contains("浴室") || roomType.contains("沐浴")) {
      return "B";
    }
    return null;
  }

  private String formatFloorNo(int floorNo, String style) {
    String normalized = normalizeValue(style, "F").toUpperCase(Locale.ROOT);
    if ("LAYER".equals(normalized) || "层".equals(style)) {
      return floorNo + "层";
    }
    return floorNo + "F";
  }

  private String defaultFloorName(String floorNo) {
    return floorNo == null ? "" : floorNo;
  }

  private int extractFloorNumber(String floorNo) {
    if (floorNo == null) {
      return 0;
    }
    String digits = floorNo.replaceAll("[^0-9]", "");
    if (digits.isBlank()) {
      return 0;
    }
    return Integer.parseInt(digits);
  }

  private int resolveBedCount(String roomType, Integer customBedCount, Integer defaultBedsPerRoom, String usageType, boolean skipBedGeneration) {
    if (skipBedGeneration) {
      return 0;
    }
    if (customBedCount != null) {
      return Math.max(customBedCount, 0);
    }
    if (usageType != null) {
      String normalized = usageType.trim().toUpperCase(Locale.ROOT);
      if (Set.of("NURSE_STATION", "STORAGE", "OFFICE", "DISPENSARY", "WAREHOUSE").contains(normalized)) {
        return 0;
      }
    }
    Integer inferred = inferCapacityByRoomType(roomType);
    if (inferred != null) {
      return inferred;
    }
    return defaultBedsPerRoom == null ? 2 : Math.max(defaultBedsPerRoom, 0);
  }

  private Integer inferCapacityByRoomType(String roomType) {
    if (roomType == null || roomType.isBlank()) {
      return null;
    }
    String normalized = roomType.trim().toUpperCase(Locale.ROOT);
    if (normalized.contains("SINGLE") || roomType.contains("单人")) return 1;
    if (normalized.contains("DOUBLE") || roomType.contains("双人")) return 2;
    if (normalized.contains("TRIPLE") || roomType.contains("三人")) return 3;
    if (normalized.contains("NURSING") || roomType.contains("护理")) return 0;
    if (normalized.contains("MEDICAL") || roomType.contains("医疗")) return 0;
    if (normalized.contains("ISOLATION") || roomType.contains("隔离")) return 0;
    return null;
  }

  private int resolveRoomCapacity(Room room) {
    Integer inferred = inferCapacityByRoomType(room.getRoomType());
    if (inferred != null) {
      return inferred;
    }
    return room.getCapacity() == null ? 0 : room.getCapacity();
  }

  private String resolveDefaultRoomType(Long tenantId) {
    return resolveDefaultConfigCode(tenantId, "ADMISSION_ROOM_TYPE", "ROOM_DOUBLE");
  }

  private String resolveDefaultBedType(Long tenantId) {
    return resolveDefaultConfigCode(tenantId, "ADMISSION_BED_TYPE", "BED_STANDARD");
  }

  private String resolveDefaultConfigCode(Long tenantId, String configGroup, String fallback) {
    List<BaseDataItem> items = baseDataItemMapper.selectList(Wrappers.lambdaQuery(BaseDataItem.class)
        .eq(BaseDataItem::getIsDeleted, 0)
        .eq(BaseDataItem::getTenantId, tenantId)
        .eq(BaseDataItem::getConfigGroup, configGroup)
        .eq(BaseDataItem::getStatus, 1)
        .orderByAsc(BaseDataItem::getSortNo));
    if (items.isEmpty()) {
      return fallback;
    }
    String code = items.get(0).getItemCode();
    return code == null || code.isBlank() ? fallback : code;
  }

  private String formatBedLabel(int index, String style) {
    String normalized = normalizeValue(style, "ALPHA").toUpperCase(Locale.ROOT);
    if ("NUMBER".equals(normalized) || "NUMERIC".equals(normalized)) {
      return String.valueOf(index);
    }
    return indexToAlpha(index);
  }

  private String indexToAlpha(int index) {
    int number = Math.max(index, 1);
    StringBuilder builder = new StringBuilder();
    while (number > 0) {
      int mod = (number - 1) % 26;
      builder.insert(0, (char) ('A' + mod));
      number = (number - 1) / 26;
    }
    return builder.toString();
  }

  private String buildRoomRemark(String usageType, String genderLimit, boolean autoGenerated, int bedCount) {
    Map<String, Object> payload = new LinkedHashMap<>();
    Map<String, Object> generation = new LinkedHashMap<>();
    generation.put("autoGenerated", autoGenerated);
    generation.put("bedCount", bedCount);
    if (usageType != null && !usageType.isBlank()) {
      generation.put("usageType", usageType);
    }
    if (genderLimit != null && !genderLimit.isBlank()) {
      generation.put("genderLimit", genderLimit);
    }
    payload.put("generationMeta", generation);
    try {
      return objectMapper.writeValueAsString(payload);
    } catch (JsonProcessingException e) {
      return "{\"generationMeta\":{\"autoGenerated\":true}}";
    }
  }

  private String normalizeMode(String mode) {
    String normalized = normalizeValue(mode, "FULL_INIT").toUpperCase(Locale.ROOT);
    return switch (normalized) {
      case "BUILDING_ONLY", "FULL_INIT", "FLOOR_ONLY", "ROOM_ONLY", "BED_ONLY" -> normalized;
      default -> throw new IllegalArgumentException("不支持的生成模式：" + mode);
    };
  }

  private String normalizeStrategy(String strategy) {
    String normalized = normalizeValue(strategy, "FILL_MISSING").toUpperCase(Locale.ROOT);
    return switch (normalized) {
      case "SKIP_EXISTING", "FILL_MISSING", "OVERWRITE_SAFE" -> normalized;
      default -> throw new IllegalArgumentException("不支持的增量策略：" + strategy);
    };
  }

  private String normalizeValue(String value, String fallback) {
    if (value == null || value.isBlank()) {
      return fallback;
    }
    return value.trim();
  }

  private Integer safeSortNo(Integer sortNo) {
    return sortNo == null ? 0 : sortNo;
  }

  private String keyOf(Object first, Object second) {
    return String.valueOf(first) + "::" + String.valueOf(second);
  }

  private void addCreate(GenerationPlan plan, String entityType, String identifier, String parentIdentifier, String reason) {
    ResidenceBatchPreviewItem item = createItem(entityType, "CREATE", identifier, parentIdentifier, reason, false);
    plan.response.getItems().add(item);
    increaseCreateCount(plan.response, entityType);
  }

  private void addSkip(GenerationPlan plan, String entityType, String identifier, String parentIdentifier, String reason) {
    ResidenceBatchPreviewItem item = createItem(entityType, "SKIP", identifier, parentIdentifier, reason, false);
    plan.response.getItems().add(item);
    plan.response.setSkipCount(plan.response.getSkipCount() + 1);
    plan.response.getSkipped().add(entityType + " " + identifier + "：" + reason);
  }

  private void addConflict(GenerationPlan plan, String entityType, String identifier, String parentIdentifier, String reason) {
    ResidenceBatchPreviewItem item = createItem(entityType, "CONFLICT", identifier, parentIdentifier, reason, false);
    plan.response.getItems().add(item);
    plan.response.setConflictCount(plan.response.getConflictCount() + 1);
    plan.response.getConflicts().add(entityType + " " + identifier + "：" + reason);
  }

  private void addSafeOverwrite(GenerationPlan plan, String entityType, String identifier, String parentIdentifier, String reason) {
    ResidenceBatchPreviewItem item = createItem(entityType, "OVERWRITE_SAFE", identifier, parentIdentifier, reason, true);
    plan.response.getItems().add(item);
    plan.response.setOverwriteSafeCount(plan.response.getOverwriteSafeCount() + 1);
    plan.response.getSafeOverwriteTargets().add(entityType + " " + identifier + "：" + reason);
  }

  private ResidenceBatchPreviewItem createItem(String entityType, String action, String identifier, String parentIdentifier, String reason, boolean safeOverwrite) {
    ResidenceBatchPreviewItem item = new ResidenceBatchPreviewItem();
    item.setEntityType(entityType);
    item.setAction(action);
    item.setIdentifier(identifier);
    item.setParentIdentifier(parentIdentifier);
    item.setDisplayName(identifier);
    item.setReason(reason);
    item.setSafeOverwrite(safeOverwrite);
    return item;
  }

  private void increaseCreateCount(ResidenceBatchPreviewResponse response, String entityType) {
    switch (entityType) {
      case "BUILDING" -> response.setCreateBuildingCount(response.getCreateBuildingCount() + 1);
      case "FLOOR" -> response.setCreateFloorCount(response.getCreateFloorCount() + 1);
      case "ROOM" -> response.setCreateRoomCount(response.getCreateRoomCount() + 1);
      case "BED" -> response.setCreateBedCount(response.getCreateBedCount() + 1);
      default -> {
      }
    }
  }

  private void sortPreviewItems(List<ResidenceBatchPreviewItem> items) {
    items.sort(Comparator
        .comparing(ResidenceBatchPreviewItem::getEntityType, Comparator.nullsLast(String::compareTo))
        .thenComparing(ResidenceBatchPreviewItem::getIdentifier, Comparator.nullsLast(String::compareTo)));
  }

  private void purgeExpiredPreview() {
    Instant now = Instant.now();
    PREVIEW_CACHE.entrySet().removeIf(entry -> Duration.between(entry.getValue().createdAt, now).compareTo(PREVIEW_TTL) > 0);
  }

  private static class CachedPreview {
    private final Long tenantId;
    private final Long operatorId;
    private final Instant createdAt;
    private final GenerationPlan plan;

    private CachedPreview(Long tenantId, Long operatorId, Instant createdAt, GenerationPlan plan) {
      this.tenantId = tenantId;
      this.operatorId = operatorId;
      this.createdAt = createdAt;
      this.plan = plan;
    }
  }

  private static class GenerationPlan {
    private final ResidenceBatchPreviewResponse response = new ResidenceBatchPreviewResponse();
    private final List<BuildingPlan> buildings = new ArrayList<>();
    private final List<FloorPlan> floors = new ArrayList<>();
    private final List<RoomPlan> rooms = new ArrayList<>();
    private final List<BedPlan> beds = new ArrayList<>();
  }

  private static class BuildingPlan {
    private final String action;
    private final String planKey;
    private final Long existingBuildingId;
    private final String name;
    private final String code;
    private final Integer sortNo;

    private BuildingPlan(String action, String planKey, Long existingBuildingId, String name, String code, Integer sortNo) {
      this.action = action;
      this.planKey = planKey;
      this.existingBuildingId = existingBuildingId;
      this.name = name;
      this.code = code;
      this.sortNo = sortNo;
    }
  }

  private static class FloorPlan {
    private final String action;
    private final String planKey;
    private final Long existingFloorId;
    private final String buildingPlanKey;
    private final Long existingBuildingId;
    private final String floorNo;
    private final String name;
    private final Integer sortNo;

    private FloorPlan(String action, String planKey, Long existingFloorId, String buildingPlanKey, Long existingBuildingId, String floorNo, String name, Integer sortNo) {
      this.action = action;
      this.planKey = planKey;
      this.existingFloorId = existingFloorId;
      this.buildingPlanKey = buildingPlanKey;
      this.existingBuildingId = existingBuildingId;
      this.floorNo = floorNo;
      this.name = name;
      this.sortNo = sortNo;
    }
  }

  private static class RoomPlan {
    private final String action;
    private final String planKey;
    private final Long existingRoomId;
    private final String buildingPlanKey;
    private final String floorPlanKey;
    private final Long existingBuildingId;
    private final Long existingFloorId;
    private final String buildingName;
    private final String floorNo;
    private final String roomNo;
    private final String roomType;
    private final Integer capacity;
    private final String remark;

    private RoomPlan(String action, String planKey, Long existingRoomId, String buildingPlanKey, String floorPlanKey, Long existingBuildingId, Long existingFloorId, String buildingName, String floorNo, String roomNo, String roomType, Integer capacity, String remark) {
      this.action = action;
      this.planKey = planKey;
      this.existingRoomId = existingRoomId;
      this.buildingPlanKey = buildingPlanKey;
      this.floorPlanKey = floorPlanKey;
      this.existingBuildingId = existingBuildingId;
      this.existingFloorId = existingFloorId;
      this.buildingName = buildingName;
      this.floorNo = floorNo;
      this.roomNo = roomNo;
      this.roomType = roomType;
      this.capacity = capacity;
      this.remark = remark;
    }
  }

  private static class BedPlan {
    private final String action;
    private final String planKey;
    private final Long existingBedId;
    private final String roomPlanKey;
    private final Long existingRoomId;
    private final String bedNo;
    private final String bedType;

    private BedPlan(String action, String planKey, Long existingBedId, String roomPlanKey, Long existingRoomId, String bedNo, String bedType) {
      this.action = action;
      this.planKey = planKey;
      this.existingBedId = existingBedId;
      this.roomPlanKey = roomPlanKey;
      this.existingRoomId = existingRoomId;
      this.bedNo = bedNo;
      this.bedType = bedType;
    }
  }

  private static class Snapshot {
    private Long tenantId;
    private List<Building> buildings = new ArrayList<>();
    private List<Floor> floors = new ArrayList<>();
    private List<Room> rooms = new ArrayList<>();
    private List<Bed> beds = new ArrayList<>();
    private List<ElderBedRelation> activeRelations = new ArrayList<>();
    private Map<Long, Building> buildingById = new HashMap<>();
    private Map<String, Building> buildingByName = new HashMap<>();
    private Map<String, Building> buildingByCode = new HashMap<>();
    private Map<Long, Floor> floorById = new HashMap<>();
    private Map<String, Floor> floorByBuildingAndNo = new HashMap<>();
    private Map<Long, Room> roomById = new HashMap<>();
    private Map<String, Room> roomByNo = new HashMap<>();
    private Map<String, Bed> bedByRoomAndNo = new HashMap<>();
    private Map<Long, Boolean> roomHasActiveOccupancy = new HashMap<>();
  }
}
