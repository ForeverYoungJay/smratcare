package com.zhiyangyun.care.elder.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.elder.model.RoomRequest;
import com.zhiyangyun.care.elder.model.RoomResponse;
import com.zhiyangyun.care.elder.service.RoomService;
import org.springframework.stereotype.Service;

@Service
public class RoomServiceImpl implements RoomService {
  private final RoomMapper roomMapper;

  public RoomServiceImpl(RoomMapper roomMapper) {
    this.roomMapper = roomMapper;
  }

  @Override
  public RoomResponse create(RoomRequest request) {
    Room room = new Room();
    room.setOrgId(request.getOrgId());
    room.setBuilding(request.getBuilding());
    room.setFloorNo(request.getFloorNo());
    room.setRoomNo(request.getRoomNo());
    room.setRoomType(request.getRoomType());
    room.setCapacity(request.getCapacity());
    room.setStatus(request.getStatus());
    roomMapper.insert(room);
    return toResponse(room);
  }

  @Override
  public RoomResponse update(Long id, RoomRequest request) {
    Room room = roomMapper.selectById(id);
    if (room == null) {
      return null;
    }
    room.setOrgId(request.getOrgId());
    room.setBuilding(request.getBuilding());
    room.setFloorNo(request.getFloorNo());
    room.setRoomNo(request.getRoomNo());
    room.setRoomType(request.getRoomType());
    room.setCapacity(request.getCapacity());
    room.setStatus(request.getStatus());
    roomMapper.updateById(room);
    return toResponse(room);
  }

  @Override
  public RoomResponse get(Long id) {
    Room room = roomMapper.selectById(id);
    return room == null ? null : toResponse(room);
  }

  @Override
  public IPage<RoomResponse> page(Long orgId, long pageNo, long pageSize, String keyword) {
    var wrapper = Wrappers.lambdaQuery(Room.class)
        .eq(Room::getIsDeleted, 0)
        .eq(orgId != null, Room::getOrgId, orgId);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(Room::getRoomNo, keyword)
          .or().like(Room::getBuilding, keyword)
          .or().like(Room::getFloorNo, keyword));
    }
    IPage<Room> page = roomMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    return page.convert(this::toResponse);
  }

  @Override
  public java.util.List<RoomResponse> list(Long orgId) {
    var wrapper = Wrappers.lambdaQuery(Room.class)
        .eq(Room::getIsDeleted, 0)
        .eq(orgId != null, Room::getOrgId, orgId)
        .orderByAsc(Room::getRoomNo);
    return roomMapper.selectList(wrapper).stream().map(this::toResponse).toList();
  }

  @Override
  public void delete(Long id) {
    Room room = roomMapper.selectById(id);
    if (room != null) {
      room.setIsDeleted(1);
      roomMapper.updateById(room);
    }
  }

  private RoomResponse toResponse(Room room) {
    RoomResponse response = new RoomResponse();
    response.setId(room.getId());
    response.setOrgId(room.getOrgId());
    response.setBuilding(room.getBuilding());
    response.setFloorNo(room.getFloorNo());
    response.setRoomNo(room.getRoomNo());
    response.setRoomType(room.getRoomType());
    response.setCapacity(room.getCapacity());
    response.setStatus(room.getStatus());
    return response;
  }
}
