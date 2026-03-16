package com.zhiyangyun.care.asset.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.asset.entity.Building;
import com.zhiyangyun.care.asset.entity.Floor;
import com.zhiyangyun.care.asset.mapper.BuildingMapper;
import com.zhiyangyun.care.asset.mapper.FloorMapper;
import com.zhiyangyun.care.asset.model.ResidenceBootstrapRequest;
import com.zhiyangyun.care.asset.model.ResidenceBootstrapResponse;
import com.zhiyangyun.care.asset.service.ResidenceBootstrapService;
import com.zhiyangyun.care.baseconfig.entity.BaseDataItem;
import com.zhiyangyun.care.baseconfig.mapper.BaseDataItemMapper;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.elder.util.QrCodeUtil;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResidenceBootstrapServiceImpl implements ResidenceBootstrapService {
  private final BuildingMapper buildingMapper;
  private final FloorMapper floorMapper;
  private final RoomMapper roomMapper;
  private final BedMapper bedMapper;
  private final BaseDataItemMapper baseDataItemMapper;

  public ResidenceBootstrapServiceImpl(
      BuildingMapper buildingMapper,
      FloorMapper floorMapper,
      RoomMapper roomMapper,
      BedMapper bedMapper,
      BaseDataItemMapper baseDataItemMapper) {
    this.buildingMapper = buildingMapper;
    this.floorMapper = floorMapper;
    this.roomMapper = roomMapper;
    this.bedMapper = bedMapper;
    this.baseDataItemMapper = baseDataItemMapper;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ResidenceBootstrapResponse bootstrap(Long tenantId, Long createdBy, ResidenceBootstrapRequest request) {
    if (tenantId == null) {
      throw new IllegalArgumentException("机构信息缺失，无法生成");
    }
    purgeDeletedAssetKeys(tenantId);

    int buildingCount = request.getBuildingCount() == null ? 1 : request.getBuildingCount();
    int floorsPerBuilding = request.getFloorsPerBuilding() == null ? 3 : request.getFloorsPerBuilding();
    int roomsPerFloor = request.getRoomsPerFloor() == null ? 10 : request.getRoomsPerFloor();
    int seed = request.getStartNo() == null ? 1 : request.getStartNo();

    String buildingPrefix = normalizePrefix(request.getBuildingPrefix(), "楼栋");
    String floorPrefix = normalizePrefix(request.getFloorPrefix(), "F");
    String roomPrefix = normalizePrefix(request.getRoomPrefix(), "R");
    String bedPrefix = normalizePrefix(request.getBedPrefix(), "B");

    String preferredRoomType = normalizeValue(request.getRoomType(), "");
    String bedType = normalizeValue(
        request.getBedType(),
        resolveDefaultConfigCode(tenantId, "ADMISSION_BED_TYPE", "BED_STANDARD"));
    int bedsPerRoom = resolveBedsPerRoom(preferredRoomType, request.getBedsPerRoom());
    String roomType = resolveRoomType(tenantId, preferredRoomType, bedsPerRoom);

    String templateCode = normalizeValue(request.getTemplateCode(), "");
    if ("AB_F1_6_R101_130_B01_03".equals(templateCode)) {
      return bootstrapTemplateAb(tenantId, createdBy, roomType, bedType, bedsPerRoom);
    }

    int createdBuilding = 0;
    int createdFloor = 0;
    int createdRoom = 0;
    int createdBed = 0;
    int buildingNo = seed;

    for (int index = 0; index < buildingCount; index++) {
      String buildingCode = buildBuildingCode(buildingNo);
      while (existsBuildingName(tenantId, buildingPrefix + buildingNo)
          || existsBuildingCode(tenantId, buildingCode)) {
        buildingNo++;
        buildingCode = buildBuildingCode(buildingNo);
      }
      Building building = new Building();
      building.setTenantId(tenantId);
      building.setOrgId(tenantId);
      building.setName(buildingPrefix + buildingNo);
      building.setCode(buildingCode);
      building.setStatus(1);
      building.setSortNo(buildingNo);
      building.setCreatedBy(createdBy);
      buildingMapper.insert(building);
      createdBuilding++;

      for (int floorNo = 1; floorNo <= floorsPerBuilding; floorNo++) {
        Floor floor = new Floor();
        floor.setTenantId(tenantId);
        floor.setOrgId(tenantId);
        floor.setBuildingId(building.getId());
        floor.setFloorNo(floorPrefix + floorNo);
        floor.setName("第" + floorNo + "层");
        floor.setStatus(1);
        floor.setSortNo(floorNo);
        floor.setCreatedBy(createdBy);
        floorMapper.insert(floor);
        createdFloor++;

        for (int roomNo = 1; roomNo <= roomsPerFloor; roomNo++) {
          Room room = new Room();
          room.setTenantId(tenantId);
          room.setOrgId(tenantId);
          room.setBuildingId(building.getId());
          room.setFloorId(floor.getId());
          room.setBuilding(building.getName());
          room.setFloorNo(floor.getFloorNo());
          room.setRoomNo(roomPrefix + buildingNo + String.format("%02d%02d", floorNo, roomNo));
          room.setRoomType(roomType);
          room.setCapacity(bedsPerRoom);
          room.setStatus(1);
          room.setRoomQrCode(QrCodeUtil.generate());
          room.setCreatedBy(createdBy);
          roomMapper.insert(room);
          createdRoom++;

          for (int bedNo = 1; bedNo <= bedsPerRoom; bedNo++) {
            Bed bed = new Bed();
            bed.setTenantId(tenantId);
            bed.setOrgId(tenantId);
            bed.setRoomId(room.getId());
            bed.setBedNo(bedPrefix + String.format("%02d", bedNo));
            bed.setBedType(bedType);
            bed.setBedQrCode(QrCodeUtil.generate());
            bed.setStatus(1);
            bed.setCreatedBy(createdBy);
            bedMapper.insert(bed);
            createdBed++;
          }
        }
      }
      buildingNo++;
    }

    ResidenceBootstrapResponse response = new ResidenceBootstrapResponse();
    response.setBuildingCount(createdBuilding);
    response.setFloorCount(createdFloor);
    response.setRoomCount(createdRoom);
    response.setBedCount(createdBed);
    return response;
  }

  private void purgeDeletedAssetKeys(Long tenantId) {
    bedMapper.delete(Wrappers.lambdaQuery(Bed.class)
        .eq(Bed::getTenantId, tenantId)
        .eq(Bed::getIsDeleted, 1));
    roomMapper.delete(Wrappers.lambdaQuery(Room.class)
        .eq(Room::getTenantId, tenantId)
        .eq(Room::getIsDeleted, 1));
    floorMapper.delete(Wrappers.lambdaQuery(Floor.class)
        .eq(Floor::getTenantId, tenantId)
        .eq(Floor::getIsDeleted, 1));
    buildingMapper.delete(Wrappers.lambdaQuery(Building.class)
        .eq(Building::getTenantId, tenantId)
        .eq(Building::getIsDeleted, 1));
  }

  private ResidenceBootstrapResponse bootstrapTemplateAb(Long tenantId, Long createdBy, String roomType, String bedType, int bedsPerRoom) {
    int createdBuilding = 0;
    int createdFloor = 0;
    int createdRoom = 0;
    int createdBed = 0;
    String[] buildingNames = new String[] {"A栋", "B栋"};

    for (String buildingName : buildingNames) {
      String buildingCode = buildingName.substring(0, 1).toUpperCase();
      if (existsBuildingName(tenantId, buildingName)) {
        throw new IllegalArgumentException("楼栋已存在：" + buildingName + "，请先删除或更换模板");
      }
      if (existsBuildingCode(tenantId, buildingCode)) {
        throw new IllegalArgumentException("楼栋编码已存在：" + buildingCode + "，请先删除或更换模板");
      }
      Building building = new Building();
      building.setTenantId(tenantId);
      building.setOrgId(tenantId);
      building.setName(buildingName);
      building.setCode(buildingCode);
      building.setStatus(1);
      building.setSortNo(createdBuilding + 1);
      building.setCreatedBy(createdBy);
      buildingMapper.insert(building);
      createdBuilding++;

      String roomHead = buildingName.substring(0, 1).toUpperCase();
      for (int floorNo = 1; floorNo <= 6; floorNo++) {
        Floor floor = new Floor();
        floor.setTenantId(tenantId);
        floor.setOrgId(tenantId);
        floor.setBuildingId(building.getId());
        floor.setFloorNo(floorNo + "F");
        floor.setName(floorNo + "F");
        floor.setStatus(1);
        floor.setSortNo(floorNo);
        floor.setCreatedBy(createdBy);
        floorMapper.insert(floor);
        createdFloor++;

        for (int roomSeq = 1; roomSeq <= 30; roomSeq++) {
          String roomNo = roomHead + floorNo + String.format("%02d", roomSeq);
          if (existsRoomNo(tenantId, roomNo)) {
            throw new IllegalArgumentException("房间号已存在：" + roomNo + "，请先清理后重试");
          }
          Room room = new Room();
          room.setTenantId(tenantId);
          room.setOrgId(tenantId);
          room.setBuildingId(building.getId());
          room.setFloorId(floor.getId());
          room.setBuilding(building.getName());
          room.setFloorNo(floor.getFloorNo());
          room.setRoomNo(roomNo);
          room.setRoomType(roomType);
          room.setCapacity(bedsPerRoom);
          room.setStatus(1);
          room.setRoomQrCode(QrCodeUtil.generate());
          room.setCreatedBy(createdBy);
          roomMapper.insert(room);
          createdRoom++;

          for (int bedNo = 1; bedNo <= bedsPerRoom; bedNo++) {
            Bed bed = new Bed();
            bed.setTenantId(tenantId);
            bed.setOrgId(tenantId);
            bed.setRoomId(room.getId());
            bed.setBedNo(String.format("%02d", bedNo));
            bed.setBedType(bedType);
            bed.setBedQrCode(QrCodeUtil.generate());
            bed.setStatus(1);
            bed.setCreatedBy(createdBy);
            bedMapper.insert(bed);
            createdBed++;
          }
        }
      }
    }

    ResidenceBootstrapResponse response = new ResidenceBootstrapResponse();
    response.setBuildingCount(createdBuilding);
    response.setFloorCount(createdFloor);
    response.setRoomCount(createdRoom);
    response.setBedCount(createdBed);
    return response;
  }

  private boolean existsBuildingName(Long tenantId, String name) {
    return buildingMapper.selectCount(Wrappers.lambdaQuery(Building.class)
        .eq(Building::getIsDeleted, 0)
        .eq(Building::getTenantId, tenantId)
        .eq(Building::getName, name)) > 0;
  }

  private boolean existsBuildingCode(Long tenantId, String code) {
    return buildingMapper.selectCount(Wrappers.lambdaQuery(Building.class)
        .eq(Building::getIsDeleted, 0)
        .eq(Building::getTenantId, tenantId)
        .eq(Building::getCode, code)) > 0;
  }

  private boolean existsRoomNo(Long tenantId, String roomNo) {
    return roomMapper.selectCount(Wrappers.lambdaQuery(Room.class)
        .eq(Room::getIsDeleted, 0)
        .eq(Room::getTenantId, tenantId)
        .eq(Room::getRoomNo, roomNo)) > 0;
  }

  private String resolveDefaultConfigCode(Long tenantId, String configGroup, String fallback) {
    List<BaseDataItem> items = baseDataItemMapper.selectList(Wrappers.lambdaQuery(BaseDataItem.class)
        .eq(BaseDataItem::getIsDeleted, 0)
        .eq(BaseDataItem::getTenantId, tenantId)
        .eq(BaseDataItem::getConfigGroup, configGroup)
        .eq(BaseDataItem::getStatus, 1)
        .orderByAsc(BaseDataItem::getSortNo));
    if (items.isEmpty()) return fallback;
    String code = items.get(0).getItemCode();
    return code == null || code.isBlank() ? fallback : code;
  }

  private String normalizePrefix(String value, String fallback) {
    if (value == null || value.isBlank()) return fallback;
    return value.trim();
  }

  private String buildBuildingCode(int buildingNo) {
    return "B" + buildingNo;
  }

  private String normalizeValue(String value, String fallback) {
    if (value == null || value.isBlank()) return fallback;
    return value.trim();
  }

  private int resolveBedsPerRoom(String roomType, Integer fallbackBedsPerRoom) {
    Integer inferred = inferCapacityByRoomType(roomType);
    if (inferred != null) {
      return inferred;
    }
    if (fallbackBedsPerRoom == null || fallbackBedsPerRoom <= 0) {
      return 1;
    }
    return fallbackBedsPerRoom;
  }

  private String resolveRoomType(Long tenantId, String roomType, int bedsPerRoom) {
    if (roomType != null && !roomType.isBlank()) {
      return roomType;
    }
    String inferred = inferRoomTypeByCapacity(tenantId, bedsPerRoom);
    if (inferred != null && !inferred.isBlank()) {
      return inferred;
    }
    return resolveDefaultConfigCode(tenantId, "ADMISSION_ROOM_TYPE", "ROOM_DOUBLE");
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

  private String inferRoomTypeByCapacity(Long tenantId, int bedsPerRoom) {
    String preferredName = bedsPerRoom == 1 ? "单人间" : bedsPerRoom == 2 ? "双人间" : bedsPerRoom == 3 ? "三人间" : null;
    String fallbackCode = bedsPerRoom == 1 ? "ROOM_SINGLE" : bedsPerRoom == 2 ? "ROOM_DOUBLE" : bedsPerRoom == 3 ? "ROOM_TRIPLE" : null;
    if (preferredName == null) {
      return null;
    }
    List<BaseDataItem> items = baseDataItemMapper.selectList(Wrappers.lambdaQuery(BaseDataItem.class)
        .eq(BaseDataItem::getIsDeleted, 0)
        .eq(BaseDataItem::getTenantId, tenantId)
        .eq(BaseDataItem::getConfigGroup, "ADMISSION_ROOM_TYPE")
        .eq(BaseDataItem::getStatus, 1)
        .orderByAsc(BaseDataItem::getSortNo));
    BaseDataItem byName = items.stream()
        .filter(item -> preferredName.equals(item.getItemName()))
        .findFirst()
        .orElse(null);
    if (byName != null && byName.getItemCode() != null && !byName.getItemCode().isBlank()) {
      return byName.getItemCode();
    }
    BaseDataItem byCode = items.stream()
        .filter(item -> fallbackCode.equals(item.getItemCode()))
        .findFirst()
        .orElse(null);
    if (byCode != null && byCode.getItemCode() != null && !byCode.getItemCode().isBlank()) {
      return byCode.getItemCode();
    }
    return fallbackCode;
  }
}
