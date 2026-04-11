package com.zhiyangyun.care.elder.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.assessment.entity.AssessmentRecord;
import com.zhiyangyun.care.assessment.mapper.AssessmentRecordMapper;
import com.zhiyangyun.care.asset.entity.Building;
import com.zhiyangyun.care.asset.entity.Floor;
import com.zhiyangyun.care.asset.mapper.BuildingMapper;
import com.zhiyangyun.care.asset.mapper.FloorMapper;
import com.zhiyangyun.care.crm.entity.CrmContract;
import com.zhiyangyun.care.crm.entity.CrmLead;
import com.zhiyangyun.care.crm.mapper.CrmContractMapper;
import com.zhiyangyun.care.crm.mapper.CrmLeadMapper;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderBedRelation;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderBedRelationMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.elder.model.BedRequest;
import com.zhiyangyun.care.elder.model.BedResponse;
import com.zhiyangyun.care.elder.model.BedStatus;
import com.zhiyangyun.care.elder.service.BedService;
import com.zhiyangyun.care.elder.util.QrCodeUtil;
import com.zhiyangyun.care.health.entity.HealthDataRecord;
import com.zhiyangyun.care.health.mapper.HealthDataRecordMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class BedServiceImpl implements BedService {
  private static final String OCCUPANCY_SOURCE_RELATION = "RELATION";
  private static final String OCCUPANCY_SOURCE_SELF = "SELF";
  private static final String OCCUPANCY_REF_TYPE_RELATION = "ELDER_BED_RELATION";
  private static final String OCCUPANCY_REF_TYPE_ELDER = "ELDER";
  private final BedMapper bedMapper;
  private final RoomMapper roomMapper;
  private final ElderBedRelationMapper relationMapper;
  private final ElderMapper elderMapper;
  private final BuildingMapper buildingMapper;
  private final FloorMapper floorMapper;
  private final AssessmentRecordMapper assessmentRecordMapper;
  private final HealthDataRecordMapper healthDataRecordMapper;
  private final CrmLeadMapper crmLeadMapper;
  private final CrmContractMapper crmContractMapper;

  public BedServiceImpl(
      BedMapper bedMapper,
      RoomMapper roomMapper,
      ElderBedRelationMapper relationMapper,
      ElderMapper elderMapper,
      BuildingMapper buildingMapper,
      FloorMapper floorMapper,
      AssessmentRecordMapper assessmentRecordMapper,
      HealthDataRecordMapper healthDataRecordMapper,
      CrmLeadMapper crmLeadMapper,
      CrmContractMapper crmContractMapper) {
    this.bedMapper = bedMapper;
    this.roomMapper = roomMapper;
    this.relationMapper = relationMapper;
    this.elderMapper = elderMapper;
    this.buildingMapper = buildingMapper;
    this.floorMapper = floorMapper;
    this.assessmentRecordMapper = assessmentRecordMapper;
    this.healthDataRecordMapper = healthDataRecordMapper;
    this.crmLeadMapper = crmLeadMapper;
    this.crmContractMapper = crmContractMapper;
  }

  @Override
  public BedResponse create(BedRequest request) {
    if (request.getBedNo() == null || request.getBedNo().isBlank()) {
      request.setBedNo(resolveNextBedNo(request.getTenantId(), request.getRoomId()));
    }
    ensureBedNoUnique(null, request.getTenantId(), request.getRoomId(), request.getBedNo());
    ensureRoomTenant(request.getTenantId(), request.getRoomId());
    ensureRoomBedQuota(request.getTenantId(), request.getRoomId(), null);
    Bed bed = new Bed();
    bed.setTenantId(request.getTenantId());
    bed.setOrgId(request.getOrgId());
    bed.setRoomId(request.getRoomId());
    bed.setBedNo(request.getBedNo());
    bed.setBedType(request.getBedType());
    bed.setBedQrCode(QrCodeUtil.generate());
    bed.setStatus(request.getStatus());
    syncManualOccupancyState(bed);
    bed.setCreatedBy(request.getCreatedBy());
    bedMapper.insert(bed);
    return toResponse(bed);
  }

  @Override
  public BedResponse update(Long id, BedRequest request) {
    Bed bed = bedMapper.selectById(id);
    if (bed == null) {
      return null;
    }
    if (request.getBedNo() == null || request.getBedNo().isBlank()) {
      request.setBedNo(bed.getBedNo());
    }
    if (bed.getElderId() != null && !Objects.equals(bed.getRoomId(), request.getRoomId())) {
      throw new IllegalArgumentException("当前床位已绑定长者，不可直接变更所属房间");
    }
    if (bed.getElderId() != null && request.getStatus() != null && request.getStatus() != 2) {
      throw new IllegalArgumentException("当前床位已入住，状态只能保持“入住”");
    }
    if (bed.getElderId() == null
        && hasActiveReservation(bed.getTenantId(), bed.getId())
        && request.getStatus() != null
        && !Objects.equals(request.getStatus(), bed.getStatus())
        && !Objects.equals(request.getStatus(), BedStatus.AVAILABLE)) {
      throw new IllegalArgumentException("当前床位已被预定锁定，请先释放预定后再调整床位状态");
    }
    ensureBedNoUnique(id, request.getTenantId(), request.getRoomId(), request.getBedNo());
    ensureRoomTenant(request.getTenantId(), request.getRoomId());
    ensureRoomBedQuota(request.getTenantId(), request.getRoomId(), id);
    bed.setTenantId(request.getTenantId());
    bed.setOrgId(request.getOrgId());
    bed.setRoomId(request.getRoomId());
    bed.setBedNo(request.getBedNo());
    bed.setBedType(request.getBedType());
    bed.setStatus(request.getStatus());
    syncManualOccupancyState(bed);
    bedMapper.updateById(bed);
    return toResponse(bed);
  }

  @Override
  public BedResponse get(Long id, Long tenantId) {
    Bed bed = bedMapper.selectById(id);
    if (bed == null || (tenantId != null && !tenantId.equals(bed.getTenantId()))) {
      return null;
    }
    return enrichBeds(tenantId, List.of(bed), false).stream().findFirst().orElse(null);
  }

  @Override
  public IPage<BedResponse> page(Long orgId, long pageNo, long pageSize,
      String keyword, Integer status, String bedNo, String roomNo, String elderName, String roomType, String bedType) {
    Long tenantId = orgId;
    List<Long> activeRoomIds = resolveActiveRoomIds(tenantId);
    if (activeRoomIds.isEmpty()) {
      return new Page<>(pageNo, pageSize);
    }
    var wrapper = Wrappers.lambdaQuery(Bed.class)
        .eq(Bed::getIsDeleted, 0)
        .eq(tenantId != null, Bed::getTenantId, tenantId)
        .in(Bed::getRoomId, activeRoomIds);
    if (status != null) {
      wrapper.eq(Bed::getStatus, status);
    }
    if (bedNo != null && !bedNo.isBlank()) {
      wrapper.like(Bed::getBedNo, bedNo);
    }
    if (roomNo != null && !roomNo.isBlank()) {
      List<Long> roomIds = roomMapper.selectList(Wrappers.lambdaQuery(Room.class)
              .eq(Room::getIsDeleted, 0)
              .eq(tenantId != null, Room::getTenantId, tenantId)
              .like(Room::getRoomNo, roomNo))
          .stream().map(Room::getId).toList();
      if (roomIds.isEmpty()) {
        return new Page<>(pageNo, pageSize);
      }
      wrapper.in(Bed::getRoomId, roomIds);
    }
    if (roomType != null && !roomType.isBlank()) {
      List<Long> roomIds = roomMapper.selectList(Wrappers.lambdaQuery(Room.class)
              .eq(Room::getIsDeleted, 0)
              .eq(tenantId != null, Room::getTenantId, tenantId)
              .eq(Room::getRoomType, roomType))
          .stream().map(Room::getId).toList();
      if (roomIds.isEmpty()) {
        return new Page<>(pageNo, pageSize);
      }
      wrapper.in(Bed::getRoomId, roomIds);
    }
    if (bedType != null && !bedType.isBlank()) {
      wrapper.eq(Bed::getBedType, bedType);
    }
    if (elderName != null && !elderName.isBlank()) {
      List<Long> elderIds = elderMapper.selectList(Wrappers.lambdaQuery(ElderProfile.class)
              .eq(ElderProfile::getIsDeleted, 0)
              .eq(orgId != null, ElderProfile::getOrgId, orgId)
              .like(ElderProfile::getFullName, elderName))
          .stream().map(ElderProfile::getId).toList();
      List<Long> relationBedIds = elderIds.isEmpty()
          ? List.of()
          : relationMapper.selectList(Wrappers.lambdaQuery(ElderBedRelation.class)
                  .eq(ElderBedRelation::getIsDeleted, 0)
                  .eq(orgId != null, ElderBedRelation::getOrgId, orgId)
                  .eq(ElderBedRelation::getActiveFlag, 1)
                  .in(ElderBedRelation::getElderId, elderIds))
              .stream()
              .map(ElderBedRelation::getBedId)
              .filter(Objects::nonNull)
              .distinct()
              .toList();
      if (elderIds.isEmpty() && relationBedIds.isEmpty()) {
        return new Page<>(pageNo, pageSize);
      }
      wrapper.and(q -> {
        boolean appended = false;
        if (!elderIds.isEmpty()) {
          q.in(Bed::getElderId, elderIds);
          appended = true;
        }
        if (!relationBedIds.isEmpty()) {
          if (appended) {
            q.or();
          }
          q.in(Bed::getId, relationBedIds);
        }
      });
    }
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(Bed::getBedNo, keyword)
          .or().like(Bed::getBedQrCode, keyword));
    }
    IPage<Bed> page = bedMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    List<Bed> records = page.getRecords();
    List<Long> roomIds = records.stream()
        .map(Bed::getRoomId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, Room> roomMap = roomIds.isEmpty()
        ? Map.of()
        : roomMapper.selectList(Wrappers.lambdaQuery(Room.class)
                .eq(Room::getIsDeleted, 0)
                .in(Room::getId, roomIds))
            .stream()
            .collect(Collectors.toMap(Room::getId, Function.identity()));
    Map<Long, Building> buildingMap = resolveBuildingMap(roomMap);
    Map<Long, Floor> floorMap = resolveFloorMap(roomMap);
    Map<Long, ElderBedRelation> activeRelationMap = resolveActiveRelationMap(tenantId, orgId, records.stream()
        .map(Bed::getId)
        .filter(Objects::nonNull)
        .distinct()
        .toList());
    List<Long> elderIds = java.util.stream.Stream.concat(
            records.stream().map(Bed::getElderId),
            activeRelationMap.values().stream().map(ElderBedRelation::getElderId))
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, ElderProfile> elderMap = elderIds.isEmpty()
        ? Map.of()
        : elderMapper.selectBatchIds(elderIds)
            .stream()
            .collect(Collectors.toMap(ElderProfile::getId, Function.identity()));
    List<BedResponse> enriched = decorateBeds(orgId, records, roomMap, buildingMap, floorMap, elderMap, activeRelationMap);
    enrichRiskInfo(orgId, enriched);
    IPage<BedResponse> responsePage = new Page<>(pageNo, pageSize, page.getTotal());
    responsePage.setRecords(enriched);
    return responsePage;
  }

  @Override
  public java.util.List<BedResponse> list(Long orgId) {
    Long tenantId = orgId;
    List<Long> activeRoomIds = resolveActiveRoomIds(tenantId);
    if (activeRoomIds.isEmpty()) {
      return List.of();
    }
    var wrapper = Wrappers.lambdaQuery(Bed.class)
        .eq(Bed::getIsDeleted, 0)
        .eq(tenantId != null, Bed::getTenantId, tenantId)
        .in(Bed::getRoomId, activeRoomIds)
        .orderByAsc(Bed::getRoomId)
        .orderByAsc(Bed::getBedNo);
    return enrichBeds(orgId, bedMapper.selectList(wrapper), false);
  }

  @Override
  public List<BedResponse> map(Long orgId, boolean includeRisk) {
    Long tenantId = orgId;
    List<Long> activeRoomIds = resolveActiveRoomIds(tenantId);
    if (activeRoomIds.isEmpty()) {
      return List.of();
    }
    var wrapper = Wrappers.lambdaQuery(Bed.class)
        .eq(Bed::getIsDeleted, 0)
        .eq(tenantId != null, Bed::getTenantId, tenantId)
        .in(Bed::getRoomId, activeRoomIds)
        .orderByAsc(Bed::getRoomId)
        .orderByAsc(Bed::getBedNo);
    List<Bed> beds = bedMapper.selectList(wrapper);
    List<Long> roomIds = beds.stream()
        .map(Bed::getRoomId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, Room> roomMap = roomIds.isEmpty()
        ? Map.of()
        : roomMapper.selectList(Wrappers.lambdaQuery(Room.class)
                .eq(Room::getIsDeleted, 0)
                .in(Room::getId, roomIds))
            .stream()
            .collect(Collectors.toMap(Room::getId, Function.identity()));
    Map<Long, Building> buildingMap = resolveBuildingMap(roomMap);
    Map<Long, Floor> floorMap = resolveFloorMap(roomMap);
    Map<Long, ElderBedRelation> activeRelationMap = resolveActiveRelationMap(tenantId, orgId, beds.stream()
        .map(Bed::getId)
        .filter(Objects::nonNull)
        .distinct()
        .toList());
    List<Long> elderIds = java.util.stream.Stream.concat(
            beds.stream().map(Bed::getElderId),
            activeRelationMap.values().stream().map(ElderBedRelation::getElderId))
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, ElderProfile> elderMap = elderIds.isEmpty()
        ? Map.of()
        : elderMapper.selectBatchIds(elderIds)
            .stream()
            .collect(Collectors.toMap(ElderProfile::getId, Function.identity()));
    List<BedResponse> responses = decorateBeds(orgId, beds, roomMap, buildingMap, floorMap, elderMap, activeRelationMap);
    if (includeRisk) {
      enrichRiskInfo(orgId, responses);
    }
    return responses;
  }

  @Override
  public void delete(Long id, Long tenantId) {
    Bed bed = bedMapper.selectById(id);
    if (bed == null || Integer.valueOf(1).equals(bed.getIsDeleted())) {
      throw new IllegalArgumentException("床位不存在或已删除");
    }
    if (tenantId == null || !tenantId.equals(bed.getTenantId())) {
      throw new IllegalArgumentException("无权限删除该床位");
    }
    if (bed.getElderId() != null) {
      throw new IllegalArgumentException("当前床位已绑定长者，请先办理退床或换床");
    }
    if (hasActiveReservation(tenantId, id)) {
      throw new IllegalArgumentException("当前床位已被预定锁定，请先释放预定后再删除");
    }
    bed.setIsDeleted(1);
    bedMapper.updateById(bed);
  }

  private BedResponse toResponse(Bed bed) {
    BedResponse response = new BedResponse();
    response.setId(bed.getId());
    response.setTenantId(bed.getTenantId());
    response.setOrgId(bed.getOrgId());
    response.setRoomId(bed.getRoomId());
    response.setBedNo(bed.getBedNo());
    response.setBedType(bed.getBedType());
    response.setBedQrCode(bed.getBedQrCode());
    response.setStatus(bed.getStatus());
    response.setElderId(bed.getElderId());
    response.setOccupancySource(bed.getOccupancySource());
    response.setOccupancyRefType(bed.getOccupancyRefType());
    response.setOccupancyRefId(bed.getOccupancyRefId());
    response.setLockExpiresAt(bed.getLockExpiresAt());
    response.setOccupancyNote(bed.getOccupancyNote());
    response.setLastReleaseReason(bed.getLastReleaseReason());
    response.setLastReleasedAt(bed.getLastReleasedAt());
    return response;
  }

  private List<BedResponse> enrichBeds(Long orgId, List<Bed> beds, boolean includeRisk) {
    if (beds == null || beds.isEmpty()) {
      return List.of();
    }
    List<Long> roomIds = beds.stream()
        .map(Bed::getRoomId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, Room> roomMap = roomIds.isEmpty()
        ? Map.of()
        : roomMapper.selectList(Wrappers.lambdaQuery(Room.class)
                .eq(Room::getIsDeleted, 0)
                .in(Room::getId, roomIds))
            .stream()
            .collect(Collectors.toMap(Room::getId, Function.identity()));
    Map<Long, Building> buildingMap = resolveBuildingMap(roomMap);
    Map<Long, Floor> floorMap = resolveFloorMap(roomMap);
    Map<Long, ElderBedRelation> activeRelationMap = resolveActiveRelationMap(orgId, orgId, beds.stream()
        .map(Bed::getId)
        .filter(Objects::nonNull)
        .distinct()
        .toList());
    List<Long> elderIds = java.util.stream.Stream.concat(
            beds.stream().map(Bed::getElderId),
            activeRelationMap.values().stream().map(ElderBedRelation::getElderId))
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, ElderProfile> elderMap = elderIds.isEmpty()
        ? Map.of()
        : elderMapper.selectBatchIds(elderIds)
            .stream()
            .collect(Collectors.toMap(ElderProfile::getId, Function.identity()));
    List<BedResponse> responses = decorateBeds(orgId, beds, roomMap, buildingMap, floorMap, elderMap, activeRelationMap);
    if (includeRisk) {
      enrichRiskInfo(orgId, responses);
    }
    return responses;
  }

  private List<BedResponse> decorateBeds(
      Long orgId,
      List<Bed> beds,
      Map<Long, Room> roomMap,
      Map<Long, Building> buildingMap,
      Map<Long, Floor> floorMap,
      Map<Long, ElderProfile> elderMap,
      Map<Long, ElderBedRelation> activeRelationMap) {
    List<BedResponse> responses = beds.stream().map(bed -> {
      BedResponse response = toResponse(bed);
      Room room = roomMap.get(bed.getRoomId());
      if (room != null) {
        Building building = room.getBuildingId() == null ? null : buildingMap.get(room.getBuildingId());
        Floor floor = room.getFloorId() == null ? null : floorMap.get(room.getFloorId());
        response.setRoomNo(room.getRoomNo());
        response.setBuilding(building != null ? building.getName() : room.getBuilding());
        response.setBuildingRemark(building == null ? null : building.getRemark());
        response.setFloorNo(floor != null ? floor.getFloorNo() : room.getFloorNo());
        response.setRoomType(room.getRoomType());
        response.setRoomRemark(room.getRemark());
        response.setAreaCode(building == null ? null : building.getAreaCode());
        response.setAreaName(building == null ? null : building.getAreaName());
        response.setRoomQrCode(room.getRoomQrCode());
      }
      ElderBedRelation activeRelation = activeRelationMap.get(bed.getId());
      Long occupiedElderId = activeRelation == null ? bed.getElderId() : activeRelation.getElderId();
      if (activeRelation != null) {
        response.setStatus(BedStatus.OCCUPIED);
        response.setElderId(occupiedElderId);
      }
      ElderProfile elder = occupiedElderId == null ? null : elderMap.get(occupiedElderId);
      if (elder != null) {
        response.setElderName(elder.getFullName());
        response.setElderGender(elder.getGender());
        response.setCareLevel(elder.getCareLevel());
      }
      return response;
    }).toList();
    enrichOccupancy(orgId, beds, responses, elderMap, activeRelationMap);
    return responses;
  }

  private void enrichOccupancy(
      Long tenantId,
      List<Bed> beds,
      List<BedResponse> responses,
      Map<Long, ElderProfile> elderMap,
      Map<Long, ElderBedRelation> activeRelationMap) {
    if (beds == null || beds.isEmpty() || responses == null || responses.isEmpty()) {
      return;
    }
    Map<Long, Bed> bedMap = beds.stream().collect(Collectors.toMap(Bed::getId, Function.identity()));
    Map<Long, BedResponse> responseMap = responses.stream().collect(Collectors.toMap(BedResponse::getId, Function.identity()));
    for (Bed bed : beds) {
      BedResponse response = responseMap.get(bed.getId());
      if (response == null) {
        continue;
      }
      ElderBedRelation activeRelation = activeRelationMap.get(bed.getId());
      Long occupiedElderId = activeRelation == null ? bed.getElderId() : activeRelation.getElderId();
      if (activeRelation != null) {
        response.setStatus(BedStatus.OCCUPIED);
        response.setElderId(occupiedElderId);
        response.setOccupancySource(OCCUPANCY_SOURCE_RELATION);
        response.setOccupancyRefType(OCCUPANCY_REF_TYPE_RELATION);
        response.setOccupancyRefId(activeRelation.getId());
        if (isBlank(response.getOccupancyNote())) {
          ElderProfile elder = elderMap.get(occupiedElderId);
          response.setOccupancyNote(elder == null ? "长者在住" : "长者在住：" + elder.getFullName());
        }
        response.setLockExpiresAt(null);
        continue;
      }
      if (bed.getElderId() != null) {
        response.setElderId(bed.getElderId());
        response.setOccupancySource(OCCUPANCY_SOURCE_SELF);
        response.setOccupancyRefType(OCCUPANCY_REF_TYPE_ELDER);
        response.setOccupancyRefId(bed.getElderId());
        if (isBlank(response.getOccupancyNote())) {
          ElderProfile elder = elderMap.get(bed.getElderId());
          response.setOccupancyNote(elder == null ? "长者在住" : "长者在住：" + elder.getFullName());
        }
        response.setLockExpiresAt(null);
        continue;
      }
      if (Objects.equals(bed.getStatus(), BedStatus.MAINTENANCE)) {
        response.setOccupancySource("MAINTENANCE");
        response.setOccupancyRefType("SYSTEM");
        response.setOccupancyRefId(null);
        if (isBlank(response.getOccupancyNote())) {
          response.setOccupancyNote("床位维修中");
        }
      }
    }

    List<Long> candidateBedIds = beds.stream()
        .filter(item -> item.getId() != null)
        .filter(item -> !activeRelationMap.containsKey(item.getId()))
        .filter(item -> item.getElderId() == null)
        .filter(item -> !Objects.equals(item.getStatus(), BedStatus.MAINTENANCE))
        .map(Bed::getId)
        .toList();
    if (candidateBedIds.isEmpty()) {
      return;
    }

    Map<Long, CrmContract> reservedContractMap = crmContractMapper.selectList(Wrappers.lambdaQuery(CrmContract.class)
            .eq(CrmContract::getIsDeleted, 0)
            .eq(tenantId != null, CrmContract::getTenantId, tenantId)
            .in(CrmContract::getReservationBedId, candidateBedIds)
            .and(w -> w
                .eq(CrmContract::getFlowStage, "PENDING_BED_SELECT")
                .or().eq(CrmContract::getFlowStage, "PENDING_SIGN")
                .or().eq(CrmContract::getStatus, "APPROVED")
                .or().eq(CrmContract::getStatus, "PENDING_APPROVAL")))
        .stream()
        .sorted(Comparator
            .comparing(CrmContract::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(CrmContract::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())))
        .collect(Collectors.toMap(CrmContract::getReservationBedId, Function.identity(), (first, ignored) -> first));

    for (Map.Entry<Long, CrmContract> entry : reservedContractMap.entrySet()) {
      BedResponse response = responseMap.get(entry.getKey());
      Bed bed = bedMap.get(entry.getKey());
      if (response == null
          || bed == null
          || activeRelationMap.containsKey(bed.getId())
          || bed.getElderId() != null
          || Objects.equals(bed.getStatus(), BedStatus.MAINTENANCE)
          || (!isBlank(response.getOccupancySource()) && !"RESERVATION".equals(response.getOccupancySource()))) {
        continue;
      }
      CrmContract contract = entry.getValue();
      response.setOccupancySource("RESERVATION");
      response.setOccupancyRefType("CONTRACT");
      response.setOccupancyRefId(contract.getId());
      response.setOccupancyRefNo(contract.getContractNo());
      if (isBlank(response.getOccupancyNote())) {
        response.setOccupancyNote(buildReservationNote(contract.getElderName(), contract.getName(), contract.getMarketerName()));
      }
    }

    Map<Long, CrmLead> reservedLeadMap = crmLeadMapper.selectList(Wrappers.lambdaQuery(CrmLead.class)
            .eq(CrmLead::getIsDeleted, 0)
            .eq(tenantId != null, CrmLead::getTenantId, tenantId)
            .isNotNull(CrmLead::getReservationBedId)
            .in(CrmLead::getReservationBedId, candidateBedIds))
        .stream()
        .filter(this::isActiveLeadReservation)
        .sorted(Comparator
            .comparing(CrmLead::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(CrmLead::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())))
        .collect(Collectors.toMap(CrmLead::getReservationBedId, Function.identity(), (first, ignored) -> first));

    for (Map.Entry<Long, CrmLead> entry : reservedLeadMap.entrySet()) {
      BedResponse response = responseMap.get(entry.getKey());
      Bed bed = bedMap.get(entry.getKey());
      if (response == null
          || bed == null
          || activeRelationMap.containsKey(bed.getId())
          || bed.getElderId() != null
          || Objects.equals(bed.getStatus(), BedStatus.MAINTENANCE)
          || !isBlank(response.getOccupancySource())) {
        continue;
      }
      CrmLead lead = entry.getValue();
      response.setOccupancySource("RESERVATION");
      response.setOccupancyRefType("LEAD");
      response.setOccupancyRefId(lead.getId());
      response.setOccupancyRefNo(lead.getContractNo());
      response.setLockExpiresAt(resolveLeadLockExpiresAt(lead));
      if (isBlank(response.getOccupancyNote())) {
        response.setOccupancyNote(buildReservationNote(lead.getElderName(), lead.getName(), lead.getMarketerName()));
      }
    }
  }

  private boolean isActiveLeadReservation(CrmLead lead) {
    if (lead == null || lead.getReservationBedId() == null) {
      return false;
    }
    if (Objects.equals(lead.getContractSignedFlag(), 1) || Objects.equals(lead.getStatus(), 3)) {
      return false;
    }
    if ("SIGNED".equalsIgnoreCase(lead.getFlowStage())) {
      return false;
    }
    return lead.getReservationBedId() != null
        || !isBlank(lead.getReservationRoomNo())
        || lead.getReservationAmount() != null
        || !isBlank(lead.getReservationStatus());
  }

  private boolean hasActiveReservation(Long tenantId, Long bedId) {
    if (bedId == null) {
      return false;
    }
    Long contractCount = crmContractMapper.selectCount(Wrappers.lambdaQuery(CrmContract.class)
        .eq(CrmContract::getIsDeleted, 0)
        .eq(tenantId != null, CrmContract::getTenantId, tenantId)
        .eq(CrmContract::getReservationBedId, bedId)
        .and(w -> w
            .eq(CrmContract::getFlowStage, "PENDING_BED_SELECT")
            .or().eq(CrmContract::getFlowStage, "PENDING_SIGN")
            .or().eq(CrmContract::getStatus, "APPROVED")
            .or().eq(CrmContract::getStatus, "PENDING_APPROVAL")));
    if (contractCount != null && contractCount > 0) {
      return true;
    }
    return crmLeadMapper.selectList(Wrappers.lambdaQuery(CrmLead.class)
            .eq(CrmLead::getIsDeleted, 0)
            .eq(tenantId != null, CrmLead::getTenantId, tenantId)
            .eq(CrmLead::getReservationBedId, bedId))
        .stream()
        .anyMatch(this::isActiveLeadReservation);
  }

  private LocalDateTime resolveLeadLockExpiresAt(CrmLead lead) {
    LocalDate nextFollowDate = lead == null ? null : lead.getNextFollowDate();
    return nextFollowDate == null ? null : nextFollowDate.atTime(LocalTime.of(23, 59, 59));
  }

  private String buildReservationNote(String elderName, String contactName, String marketerName) {
    String reservedFor = firstNonBlank(elderName, contactName, "待确认客户");
    String marketer = firstNonBlank(marketerName, null);
    return marketer == null ? "预定锁床：" + reservedFor : "预定锁床：" + reservedFor + " / 顾问：" + marketer;
  }

  private String firstNonBlank(String first, String second) {
    if (!isBlank(first)) {
      return first.trim();
    }
    if (!isBlank(second)) {
      return second.trim();
    }
    return null;
  }

  private String firstNonBlank(String first, String second, String fallback) {
    String resolved = firstNonBlank(first, second);
    return resolved == null ? fallback : resolved;
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

  private void syncManualOccupancyState(Bed bed) {
    if (bed == null || bed.getElderId() != null) {
      return;
    }
    if (Objects.equals(bed.getStatus(), BedStatus.MAINTENANCE)) {
      bed.setOccupancySource("MAINTENANCE");
      bed.setOccupancyRefType("SYSTEM");
      bed.setOccupancyRefId(null);
      bed.setLockExpiresAt(null);
      if (isBlank(bed.getOccupancyNote())) {
        bed.setOccupancyNote("床位维修中");
      }
      return;
    }
    if (Objects.equals(bed.getStatus(), BedStatus.AVAILABLE) || Objects.equals(bed.getStatus(), BedStatus.DISABLED)) {
      bed.setOccupancySource(null);
      bed.setOccupancyRefType(null);
      bed.setOccupancyRefId(null);
      bed.setLockExpiresAt(null);
      bed.setOccupancyNote(null);
    }
  }

  private Map<Long, ElderBedRelation> resolveActiveRelationMap(Long tenantId, Long orgId, List<Long> bedIds) {
    if (bedIds == null || bedIds.isEmpty()) {
      return Map.of();
    }
    return relationMapper.selectList(Wrappers.lambdaQuery(ElderBedRelation.class)
            .eq(ElderBedRelation::getIsDeleted, 0)
            .eq(tenantId != null, ElderBedRelation::getTenantId, tenantId)
            .eq(orgId != null, ElderBedRelation::getOrgId, orgId)
            .eq(ElderBedRelation::getActiveFlag, 1)
            .in(ElderBedRelation::getBedId, bedIds)
            .orderByDesc(ElderBedRelation::getUpdateTime)
            .orderByDesc(ElderBedRelation::getCreateTime)
            .orderByDesc(ElderBedRelation::getId))
        .stream()
        .filter(item -> item.getBedId() != null)
        .collect(Collectors.toMap(ElderBedRelation::getBedId, Function.identity(), (first, ignored) -> first));
  }

  private Map<Long, Building> resolveBuildingMap(Map<Long, Room> roomMap) {
    if (roomMap == null || roomMap.isEmpty()) {
      return Map.of();
    }
    List<Long> buildingIds = roomMap.values().stream()
        .map(Room::getBuildingId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    if (buildingIds.isEmpty()) {
      return Map.of();
    }
    return buildingMapper.selectList(Wrappers.lambdaQuery(Building.class)
            .eq(Building::getIsDeleted, 0)
            .in(Building::getId, buildingIds))
        .stream()
        .collect(Collectors.toMap(Building::getId, Function.identity()));
  }

  private Map<Long, Floor> resolveFloorMap(Map<Long, Room> roomMap) {
    if (roomMap == null || roomMap.isEmpty()) {
      return Map.of();
    }
    List<Long> floorIds = roomMap.values().stream()
        .map(Room::getFloorId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    if (floorIds.isEmpty()) {
      return Map.of();
    }
    return floorMapper.selectList(Wrappers.lambdaQuery(Floor.class)
            .eq(Floor::getIsDeleted, 0)
            .in(Floor::getId, floorIds))
        .stream()
        .collect(Collectors.toMap(Floor::getId, Function.identity()));
  }

  private void enrichRiskInfo(Long orgId, List<BedResponse> responses) {
    if (responses == null || responses.isEmpty()) {
      return;
    }
    List<Long> elderIds = responses.stream()
        .map(BedResponse::getElderId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    if (elderIds.isEmpty()) {
      return;
    }

    Map<Long, AssessmentRecord> latestAssessmentMap = assessmentRecordMapper.selectList(Wrappers.lambdaQuery(AssessmentRecord.class)
            .eq(AssessmentRecord::getIsDeleted, 0)
            .eq(orgId != null, AssessmentRecord::getOrgId, orgId)
            .in(AssessmentRecord::getElderId, elderIds)
            .eq(AssessmentRecord::getAssessmentType, "ADMISSION")
            .orderByDesc(AssessmentRecord::getAssessmentDate)
            .orderByDesc(AssessmentRecord::getUpdateTime))
        .stream()
        .collect(Collectors.toMap(
            AssessmentRecord::getElderId,
            Function.identity(),
            (first, ignored) -> first));

    Map<Long, Integer> abnormalCountMap = healthDataRecordMapper.selectList(Wrappers.lambdaQuery(HealthDataRecord.class)
            .eq(HealthDataRecord::getIsDeleted, 0)
            .eq(orgId != null, HealthDataRecord::getOrgId, orgId)
            .in(HealthDataRecord::getElderId, elderIds)
            .eq(HealthDataRecord::getAbnormalFlag, 1)
            .ge(HealthDataRecord::getMeasuredAt, LocalDateTime.now().minusHours(24)))
        .stream()
        .collect(Collectors.toMap(
            HealthDataRecord::getElderId,
            item -> 1,
            Integer::sum));

    for (BedResponse item : responses) {
      Long elderId = item.getElderId();
      if (elderId == null) {
        item.setRiskLevel("");
        item.setRiskLabel("");
        item.setRiskSource("");
        item.setAbnormalVital24hCount(0);
        continue;
      }
      AssessmentRecord latest = latestAssessmentMap.get(elderId);
      Integer abnormalCount = abnormalCountMap.getOrDefault(elderId, 0);
      item.setAbnormalVital24hCount(abnormalCount);
      item.setLatestAssessmentLevel(latest == null ? null : latest.getLevelCode());
      item.setLatestAssessmentDate(latest == null ? null : latest.getAssessmentDate());
      String riskLevel = resolveRiskLevel(latest == null ? null : latest.getLevelCode(), abnormalCount);
      item.setRiskLevel(riskLevel);
      item.setRiskLabel(resolveRiskLabel(riskLevel));
      item.setRiskSource(resolveRiskSource(latest == null ? null : latest.getLevelCode(), abnormalCount));
    }
  }

  private String resolveRiskLevel(String assessmentLevel, Integer abnormalCount) {
    int abnormalities = abnormalCount == null ? 0 : abnormalCount;
    String level = assessmentLevel == null ? "" : assessmentLevel.trim();
    if (abnormalities >= 1
        || level.contains("高")
        || level.contains("重")
        || level.startsWith("4")
        || level.startsWith("3")
        || "A".equalsIgnoreCase(level)) {
      return "HIGH";
    }
    if (level.contains("中")
        || level.startsWith("2")
        || "B".equalsIgnoreCase(level)
        || "1级".equals(level)) {
      return "MEDIUM";
    }
    if (!level.isBlank()) {
      return "LOW";
    }
    return "";
  }

  private String resolveRiskLabel(String level) {
    if ("HIGH".equals(level)) return "高风险";
    if ("MEDIUM".equals(level)) return "中风险";
    if ("LOW".equals(level)) return "低风险";
    return "";
  }

  private String resolveRiskSource(String assessmentLevel, Integer abnormalCount) {
    boolean hasAssessment = assessmentLevel != null && !assessmentLevel.isBlank();
    boolean hasAbnormal = abnormalCount != null && abnormalCount > 0;
    if (hasAssessment && hasAbnormal) return "评估+生命体征";
    if (hasAssessment) return "入住评估";
    if (hasAbnormal) return "生命体征";
    return "";
  }

  private void ensureRoomTenant(Long tenantId, Long roomId) {
    if (tenantId == null || roomId == null) {
      return;
    }
    Room room = roomMapper.selectById(roomId);
    if (room == null
        || !tenantId.equals(room.getTenantId())
        || Integer.valueOf(1).equals(room.getIsDeleted())) {
      throw new IllegalArgumentException("房间不存在或无权限");
    }
  }

  private void ensureRoomBedQuota(Long tenantId, Long roomId, Long bedIdToExclude) {
    if (tenantId == null || roomId == null) {
      return;
    }
    Room room = roomMapper.selectById(roomId);
    if (room == null || Integer.valueOf(1).equals(room.getIsDeleted()) || !tenantId.equals(room.getTenantId())) {
      throw new IllegalArgumentException("房间不存在或无权限");
    }
    int roomCapacity = resolveRoomCapacity(room);
    var query = Wrappers.lambdaQuery(Bed.class)
        .eq(Bed::getIsDeleted, 0)
        .eq(Bed::getTenantId, tenantId)
        .eq(Bed::getRoomId, roomId);
    if (bedIdToExclude != null) {
      query.ne(Bed::getId, bedIdToExclude);
    }
    long existingBedCount = bedMapper.selectCount(query);
    if (existingBedCount >= roomCapacity) {
      throw new IllegalArgumentException("床位数不能超过房间容量(" + roomCapacity + ")");
    }
  }

  private int resolveRoomCapacity(Room room) {
    Integer inferred = inferCapacityByRoomType(room.getRoomType());
    if (inferred != null) {
      return inferred;
    }
    return room.getCapacity() == null || room.getCapacity() <= 0 ? 1 : room.getCapacity();
  }

  private Integer inferCapacityByRoomType(String roomType) {
    if (roomType == null || roomType.isBlank()) {
      return null;
    }
    String normalized = roomType.trim().toUpperCase();
    if ("1".equals(normalized)
        || normalized.contains("SINGLE")
        || normalized.contains("ROOM_SINGLE")
        || roomType.contains("单人")) {
      return 1;
    }
    if ("2".equals(normalized)
        || normalized.contains("DOUBLE")
        || normalized.contains("ROOM_DOUBLE")
        || roomType.contains("双人")) {
      return 2;
    }
    if ("3".equals(normalized)
        || normalized.contains("TRIPLE")
        || normalized.contains("ROOM_TRIPLE")
        || roomType.contains("三人")) {
      return 3;
    }
    return null;
  }

  private String resolveNextBedNo(Long tenantId, Long roomId) {
    Room room = roomMapper.selectById(roomId);
    String roomNo = room == null ? "ROOM" : room.getRoomNo();
    List<Bed> beds = bedMapper.selectList(Wrappers.lambdaQuery(Bed.class)
        .eq(Bed::getIsDeleted, 0)
        .eq(Bed::getTenantId, tenantId)
        .eq(Bed::getRoomId, roomId));
    int nextAlphaIndex = beds.stream()
        .map(Bed::getBedNo)
        .filter(Objects::nonNull)
        .map(value -> value.contains("-") ? value.substring(value.lastIndexOf('-') + 1) : value)
        .map(String::trim)
        .filter(value -> value.matches("[A-Z]+"))
        .mapToInt(this::alphaToIndex)
        .max()
        .orElse(0) + 1;
    return roomNo + "-" + indexToAlpha(nextAlphaIndex);
  }

  private int alphaToIndex(String value) {
    int result = 0;
    for (int i = 0; i < value.length(); i++) {
      result = result * 26 + (value.charAt(i) - 'A' + 1);
    }
    return result;
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

  private void ensureBedNoUnique(Long id, Long tenantId, Long roomId, String bedNo) {
    if (tenantId == null || roomId == null || bedNo == null || bedNo.isBlank()) {
      return;
    }
    var wrapper = Wrappers.lambdaQuery(Bed.class)
        .eq(Bed::getIsDeleted, 0)
        .eq(Bed::getTenantId, tenantId)
        .eq(Bed::getRoomId, roomId)
        .eq(Bed::getBedNo, bedNo);
    if (id != null) {
      wrapper.ne(Bed::getId, id);
    }
    if (bedMapper.selectCount(wrapper) > 0) {
      throw new IllegalArgumentException("同一房间内床位号已存在");
    }
  }

  private List<Long> resolveActiveRoomIds(Long tenantId) {
    return roomMapper.selectList(Wrappers.lambdaQuery(Room.class)
            .eq(Room::getIsDeleted, 0)
            .eq(tenantId != null, Room::getTenantId, tenantId))
        .stream()
        .map(Room::getId)
        .filter(Objects::nonNull)
        .toList();
  }
}
