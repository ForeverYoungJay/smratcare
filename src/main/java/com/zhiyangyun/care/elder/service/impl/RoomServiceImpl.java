package com.zhiyangyun.care.elder.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.asset.entity.Building;
import com.zhiyangyun.care.asset.entity.Floor;
import com.zhiyangyun.care.asset.mapper.BuildingMapper;
import com.zhiyangyun.care.asset.mapper.FloorMapper;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.elder.model.RoomRequest;
import com.zhiyangyun.care.elder.model.RoomResponse;
import com.zhiyangyun.care.elder.service.RoomService;
import com.zhiyangyun.care.elder.util.QrCodeUtil;
import org.springframework.stereotype.Service;

@Service
public class RoomServiceImpl implements RoomService {
  private final RoomMapper roomMapper;
  private final BuildingMapper buildingMapper;
  private final FloorMapper floorMapper;

  public RoomServiceImpl(RoomMapper roomMapper, BuildingMapper buildingMapper, FloorMapper floorMapper) {
    this.roomMapper = roomMapper;
    this.buildingMapper = buildingMapper;
    this.floorMapper = floorMapper;
  }

  @Override
  public RoomResponse create(RoomRequest request) {
    ensureRoomNoUnique(null, request.getTenantId(), request.getRoomNo());
    Room room = new Room();
    room.setTenantId(request.getTenantId());
    room.setOrgId(request.getOrgId());
    room.setBuildingId(request.getBuildingId());
    room.setFloorId(request.getFloorId());
    room.setBuilding(request.getBuilding());
    room.setFloorNo(request.getFloorNo());
    room.setRoomNo(request.getRoomNo());
    room.setRoomType(request.getRoomType());
    room.setCapacity(request.getCapacity());
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
    room.setTenantId(request.getTenantId());
    room.setOrgId(request.getOrgId());
    room.setBuildingId(request.getBuildingId());
    room.setFloorId(request.getFloorId());
    room.setBuilding(request.getBuilding());
    room.setFloorNo(request.getFloorNo());
    room.setRoomNo(request.getRoomNo());
    room.setRoomType(request.getRoomType());
    room.setCapacity(request.getCapacity());
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
      String keyword, String roomNo, String building, String floorNo, Long buildingId, Long floorId, Integer status) {
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
    if (room != null && (tenantId == null || tenantId.equals(room.getTenantId()))) {
      room.setIsDeleted(1);
      roomMapper.updateById(room);
    }
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
      if (building == null || (tenantId != null && !tenantId.equals(building.getTenantId()))) {
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
      if (floor == null || (tenantId != null && !tenantId.equals(floor.getTenantId()))) {
        throw new IllegalArgumentException("楼层不存在或无权限");
      }
      room.setFloorNo(floor.getFloorNo());
    } else {
      room.setFloorNo(fallbackFloorNo);
    }
  }
}
