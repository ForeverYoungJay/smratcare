package com.zhiyangyun.care.elder.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.asset.entity.Building;
import com.zhiyangyun.care.asset.entity.Floor;
import com.zhiyangyun.care.asset.mapper.BuildingMapper;
import com.zhiyangyun.care.asset.mapper.FloorMapper;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.elder.model.RoomRequest;
import com.zhiyangyun.care.elder.model.RoomResponse;
import com.zhiyangyun.care.elder.service.RoomService;
import com.zhiyangyun.care.elder.util.QrCodeUtil;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class RoomServiceImpl implements RoomService {
  private final RoomMapper roomMapper;
  private final BedMapper bedMapper;
  private final BuildingMapper buildingMapper;
  private final FloorMapper floorMapper;

  public RoomServiceImpl(RoomMapper roomMapper, BedMapper bedMapper, BuildingMapper buildingMapper, FloorMapper floorMapper) {
    this.roomMapper = roomMapper;
    this.bedMapper = bedMapper;
    this.buildingMapper = buildingMapper;
    this.floorMapper = floorMapper;
  }

  @Override
  public RoomResponse create(RoomRequest request) {
    ensureRoomNoUnique(null, request.getTenantId(), request.getRoomNo());
    Integer normalizedCapacity = normalizeCapacityByRoomType(request.getRoomType(), request.getCapacity());
    Room room = new Room();
    room.setTenantId(request.getTenantId());
    room.setOrgId(request.getOrgId());
    room.setBuildingId(request.getBuildingId());
    room.setFloorId(request.getFloorId());
    room.setBuilding(request.getBuilding());
    room.setFloorNo(request.getFloorNo());
    room.setRoomNo(request.getRoomNo());
    room.setRoomType(request.getRoomType());
    room.setCapacity(normalizedCapacity);
    room.setStatus(request.getStatus());
    room.setRoomQrCode(request.getRoomQrCode());
    room.setCreatedBy(request.getCreatedBy());
    applyBuildingFloor(room, request.getTenantId(), request.getBuildingId(), request.getFloorId(),
        request.getBuilding(), request.getFloorNo());
    if (room.getRoomQrCode() == null || room.getRoomQrCode().isBlank()) {
      room.setRoomQrCode(QrCodeUtil.generate());
    }
    roomMapper.insert(room);
    return toResponse(room);
  }

  @Override
  public RoomResponse update(Long id, RoomRequest request) {
    Room room = roomMapper.selectById(id);
    if (room == null) {
      return null;
    }
    ensureRoomNoUnique(id, request.getTenantId(), request.getRoomNo());
    Integer normalizedCapacity = normalizeCapacityByRoomType(request.getRoomType(), request.getCapacity());
    ensureCapacityNotLessThanOccupied(id, request.getTenantId(), normalizedCapacity);
    room.setTenantId(request.getTenantId());
    room.setOrgId(request.getOrgId());
    room.setBuildingId(request.getBuildingId());
    room.setFloorId(request.getFloorId());
    room.setBuilding(request.getBuilding());
    room.setFloorNo(request.getFloorNo());
    room.setRoomNo(request.getRoomNo());
    room.setRoomType(request.getRoomType());
    room.setCapacity(normalizedCapacity);
    room.setStatus(request.getStatus());
    room.setRoomQrCode(request.getRoomQrCode());
    applyBuildingFloor(room, request.getTenantId(), request.getBuildingId(), request.getFloorId(),
        request.getBuilding(), request.getFloorNo());
    roomMapper.updateById(room);
    return toResponse(room);
  }

  @Override
  public RoomResponse get(Long id, Long tenantId) {
    Room room = roomMapper.selectById(id);
    if (room == null || (tenantId != null && !tenantId.equals(room.getTenantId()))) {
      return null;
    }
    return room == null ? null : toResponse(room);
  }

  @Override
  public IPage<RoomResponse> page(Long tenantId, long pageNo, long pageSize,
      String keyword, String roomNo, String building, String floorNo, Long buildingId, Long floorId, String roomType, Integer status) {
    var wrapper = Wrappers.lambdaQuery(Room.class)
        .eq(Room::getIsDeleted, 0)
        .eq(tenantId != null, Room::getTenantId, tenantId);
    if (status != null) {
      wrapper.eq(Room::getStatus, status);
    }
    if (roomNo != null && !roomNo.isBlank()) {
      wrapper.like(Room::getRoomNo, roomNo);
    }
    if (building != null && !building.isBlank()) {
      wrapper.like(Room::getBuilding, building);
    }
    if (floorNo != null && !floorNo.isBlank()) {
      wrapper.like(Room::getFloorNo, floorNo);
    }
    if (buildingId != null) {
      wrapper.eq(Room::getBuildingId, buildingId);
    }
    if (floorId != null) {
      wrapper.eq(Room::getFloorId, floorId);
    }
    if (roomType != null && !roomType.isBlank()) {
      wrapper.eq(Room::getRoomType, roomType);
    }
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(Room::getRoomNo, keyword)
          .or().like(Room::getBuilding, keyword)
          .or().like(Room::getFloorNo, keyword));
    }
    IPage<Room> page = roomMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    return page.convert(this::toResponse);
  }

  @Override
  public java.util.List<RoomResponse> list(Long tenantId) {
    var wrapper = Wrappers.lambdaQuery(Room.class)
        .eq(Room::getIsDeleted, 0)
        .eq(tenantId != null, Room::getTenantId, tenantId)
        .orderByAsc(Room::getRoomNo);
    return roomMapper.selectList(wrapper).stream().map(this::toResponse).toList();
  }

  @Override
  public void delete(Long id, Long tenantId) {
    Room room = roomMapper.selectById(id);
    if (room == null || Integer.valueOf(1).equals(room.getIsDeleted())) {
      throw new IllegalArgumentException("房间不存在或已删除");
    }
    if (tenantId == null || !tenantId.equals(room.getTenantId())) {
      throw new IllegalArgumentException("无权限删除该房间");
    }
    long bedCount = bedMapper.selectCount(Wrappers.lambdaQuery(Bed.class)
        .eq(Bed::getIsDeleted, 0)
        .eq(Bed::getTenantId, room.getTenantId())
        .eq(Bed::getRoomId, room.getId()));
    if (bedCount > 0) {
      throw new IllegalArgumentException("当前房间下仍存在床位，请先删除或迁移床位");
    }
    room.setIsDeleted(1);
    roomMapper.updateById(room);
  }

  private RoomResponse toResponse(Room room) {
    RoomResponse response = new RoomResponse();
    response.setId(room.getId());
    response.setTenantId(room.getTenantId());
    response.setOrgId(room.getOrgId());
    response.setBuildingId(room.getBuildingId());
    response.setFloorId(room.getFloorId());
    response.setBuilding(room.getBuilding());
    response.setFloorNo(room.getFloorNo());
    response.setRoomNo(room.getRoomNo());
    response.setRoomType(room.getRoomType());
    response.setCapacity(room.getCapacity());
    response.setStatus(room.getStatus());
    response.setRoomQrCode(room.getRoomQrCode());
    return response;
  }

  private void ensureRoomNoUnique(Long id, Long tenantId, String roomNo) {
    if (tenantId == null || roomNo == null || roomNo.isBlank()) {
      return;
    }
    var wrapper = Wrappers.lambdaQuery(Room.class)
        .eq(Room::getIsDeleted, 0)
        .eq(Room::getTenantId, tenantId)
        .eq(Room::getRoomNo, roomNo);
    if (id != null) {
      wrapper.ne(Room::getId, id);
    }
    if (roomMapper.selectCount(wrapper) > 0) {
      throw new IllegalArgumentException("房间号已存在");
    }
  }

  private void ensureCapacityNotLessThanOccupied(Long roomId, Long tenantId, Integer capacity) {
    if (roomId == null || tenantId == null || capacity == null) {
      return;
    }
    long occupiedCount = bedMapper.selectCount(Wrappers.lambdaQuery(Bed.class)
        .eq(Bed::getIsDeleted, 0)
        .eq(Bed::getTenantId, tenantId)
        .eq(Bed::getRoomId, roomId)
        .isNotNull(Bed::getElderId));
    if (capacity < occupiedCount) {
      throw new IllegalArgumentException("房间容量不能小于当前入住床位数");
    }
  }

  private Integer normalizeCapacityByRoomType(String roomType, Integer fallbackCapacity) {
    Integer inferred = inferCapacityByRoomType(roomType);
    if (inferred != null) {
      return inferred;
    }
    return fallbackCapacity == null || fallbackCapacity <= 0 ? 1 : fallbackCapacity;
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

  private void applyBuildingFloor(Room room, Long tenantId, Long buildingId, Long floorId,
                                  String fallbackBuilding, String fallbackFloorNo) {
    if (buildingId != null) {
      Building building = buildingMapper.selectById(buildingId);
      if (building != null && building.getTenantId() == null && tenantId != null) {
        if (building.getOrgId() == null || tenantId.equals(building.getOrgId())) {
          building.setTenantId(tenantId);
          buildingMapper.updateById(building);
        }
      }
      if (building == null
          || (tenantId != null && !tenantId.equals(building.getTenantId()))
          || Integer.valueOf(1).equals(building.getIsDeleted())) {
        throw new IllegalArgumentException("楼栋不存在或无权限");
      }
      room.setBuilding(building.getName());
    } else {
      room.setBuilding(fallbackBuilding);
    }
    if (floorId != null) {
      Floor floor = floorMapper.selectById(floorId);
      if (floor != null && floor.getTenantId() == null && tenantId != null) {
        if (floor.getOrgId() == null || tenantId.equals(floor.getOrgId())) {
          floor.setTenantId(tenantId);
          floorMapper.updateById(floor);
        }
      }
      if (floor == null
          || (tenantId != null && !tenantId.equals(floor.getTenantId()))
          || Integer.valueOf(1).equals(floor.getIsDeleted())) {
        throw new IllegalArgumentException("楼层不存在或无权限");
      }
      if (buildingId != null && floor.getBuildingId() != null && !Objects.equals(buildingId, floor.getBuildingId())) {
        throw new IllegalArgumentException("楼层不属于当前楼栋");
      }
      room.setFloorNo(floor.getFloorNo());
    } else {
      room.setFloorNo(fallbackFloorNo);
    }
  }
}
