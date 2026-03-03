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
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.elder.model.BedRequest;
import com.zhiyangyun.care.elder.model.BedResponse;
import com.zhiyangyun.care.elder.service.BedService;
import com.zhiyangyun.care.elder.util.QrCodeUtil;
import com.zhiyangyun.care.health.entity.HealthDataRecord;
import com.zhiyangyun.care.health.mapper.HealthDataRecordMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class BedServiceImpl implements BedService {
  private final BedMapper bedMapper;
  private final RoomMapper roomMapper;
  private final ElderMapper elderMapper;
  private final BuildingMapper buildingMapper;
  private final FloorMapper floorMapper;
  private final AssessmentRecordMapper assessmentRecordMapper;
  private final HealthDataRecordMapper healthDataRecordMapper;

  public BedServiceImpl(
      BedMapper bedMapper,
      RoomMapper roomMapper,
      ElderMapper elderMapper,
      BuildingMapper buildingMapper,
      FloorMapper floorMapper,
      AssessmentRecordMapper assessmentRecordMapper,
      HealthDataRecordMapper healthDataRecordMapper) {
    this.bedMapper = bedMapper;
    this.roomMapper = roomMapper;
    this.elderMapper = elderMapper;
    this.buildingMapper = buildingMapper;
    this.floorMapper = floorMapper;
    this.assessmentRecordMapper = assessmentRecordMapper;
    this.healthDataRecordMapper = healthDataRecordMapper;
  }

  @Override
  public BedResponse create(BedRequest request) {
    ensureBedNoUnique(null, request.getTenantId(), request.getRoomId(), request.getBedNo());
    Bed bed = new Bed();
    bed.setTenantId(request.getTenantId());
    bed.setOrgId(request.getOrgId());
    bed.setRoomId(request.getRoomId());
    bed.setBedNo(request.getBedNo());
    bed.setBedType(request.getBedType());
    bed.setBedQrCode(QrCodeUtil.generate());
    bed.setStatus(request.getStatus());
    bed.setCreatedBy(request.getCreatedBy());
    ensureRoomTenant(request.getTenantId(), request.getRoomId());
    bedMapper.insert(bed);
    return toResponse(bed);
  }

  @Override
  public BedResponse update(Long id, BedRequest request) {
    Bed bed = bedMapper.selectById(id);
    if (bed == null) {
      return null;
    }
    if (bed.getElderId() != null && !Objects.equals(bed.getRoomId(), request.getRoomId())) {
      throw new IllegalArgumentException("当前床位已绑定长者，不可直接变更所属房间");
    }
    if (bed.getElderId() != null && request.getStatus() != null && request.getStatus() != 2) {
      throw new IllegalArgumentException("当前床位已入住，状态只能保持“入住”");
    }
    ensureBedNoUnique(id, request.getTenantId(), request.getRoomId(), request.getBedNo());
    bed.setTenantId(request.getTenantId());
    bed.setOrgId(request.getOrgId());
    bed.setRoomId(request.getRoomId());
    bed.setBedNo(request.getBedNo());
    bed.setBedType(request.getBedType());
    bed.setStatus(request.getStatus());
    ensureRoomTenant(request.getTenantId(), request.getRoomId());
    bedMapper.updateById(bed);
    return toResponse(bed);
  }

  @Override
  public BedResponse get(Long id, Long tenantId) {
    Bed bed = bedMapper.selectById(id);
    if (bed == null || (tenantId != null && !tenantId.equals(bed.getTenantId()))) {
      return null;
    }
    return toResponse(bed);
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
      if (elderIds.isEmpty()) {
        return new Page<>(pageNo, pageSize);
      }
      wrapper.in(Bed::getElderId, elderIds);
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
    List<Long> elderIds = records.stream()
        .map(Bed::getElderId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, ElderProfile> elderMap = elderIds.isEmpty()
        ? Map.of()
        : elderMapper.selectBatchIds(elderIds)
            .stream()
            .collect(Collectors.toMap(ElderProfile::getId, Function.identity()));
    List<BedResponse> enriched = page.getRecords().stream().map(bed -> {
      BedResponse response = toResponse(bed);
      Room room = roomMap.get(bed.getRoomId());
      if (room != null) {
        Building building = room.getBuildingId() == null ? null : buildingMap.get(room.getBuildingId());
        Floor floor = room.getFloorId() == null ? null : floorMap.get(room.getFloorId());
        response.setRoomNo(room.getRoomNo());
        response.setBuilding(building != null ? building.getName() : room.getBuilding());
        response.setFloorNo(floor != null ? floor.getFloorNo() : room.getFloorNo());
        response.setRoomType(room.getRoomType());
        response.setAreaCode(building == null ? null : building.getAreaCode());
        response.setAreaName(building == null ? null : building.getAreaName());
        response.setRoomQrCode(room.getRoomQrCode());
      }
      ElderProfile elder = bed.getElderId() == null ? null : elderMap.get(bed.getElderId());
      if (elder != null) {
        response.setElderName(elder.getFullName());
        response.setCareLevel(elder.getCareLevel());
      }
      return response;
    }).toList();
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
    return bedMapper.selectList(wrapper).stream().map(this::toResponse).toList();
  }

  @Override
  public List<BedResponse> map(Long orgId) {
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
    List<Long> elderIds = beds.stream()
        .map(Bed::getElderId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, ElderProfile> elderMap = elderIds.isEmpty()
        ? Map.of()
        : elderMapper.selectBatchIds(elderIds)
            .stream()
            .collect(Collectors.toMap(ElderProfile::getId, Function.identity()));
    List<BedResponse> responses = beds.stream().map(bed -> {
      BedResponse response = toResponse(bed);
      Room room = roomMap.get(bed.getRoomId());
      if (room != null) {
        Building building = room.getBuildingId() == null ? null : buildingMap.get(room.getBuildingId());
        Floor floor = room.getFloorId() == null ? null : floorMap.get(room.getFloorId());
        response.setRoomNo(room.getRoomNo());
        response.setBuilding(building != null ? building.getName() : room.getBuilding());
        response.setFloorNo(floor != null ? floor.getFloorNo() : room.getFloorNo());
        response.setRoomType(room.getRoomType());
        response.setAreaCode(building == null ? null : building.getAreaCode());
        response.setAreaName(building == null ? null : building.getAreaName());
        response.setRoomQrCode(room.getRoomQrCode());
      }
      ElderProfile elder = bed.getElderId() == null ? null : elderMap.get(bed.getElderId());
      if (elder != null) {
        response.setElderName(elder.getFullName());
        response.setCareLevel(elder.getCareLevel());
      }
      return response;
    }).toList();
    enrichRiskInfo(orgId, responses);
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
    return response;
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
