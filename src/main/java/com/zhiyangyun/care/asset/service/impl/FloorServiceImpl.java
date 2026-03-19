package com.zhiyangyun.care.asset.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.asset.entity.Floor;
import com.zhiyangyun.care.asset.mapper.FloorMapper;
import com.zhiyangyun.care.asset.model.FloorRequest;
import com.zhiyangyun.care.asset.model.FloorResponse;
import com.zhiyangyun.care.asset.service.FloorService;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FloorServiceImpl implements FloorService {
  private final FloorMapper floorMapper;
  private final RoomMapper roomMapper;
  private final BedMapper bedMapper;

  public FloorServiceImpl(FloorMapper floorMapper, RoomMapper roomMapper, BedMapper bedMapper) {
    this.floorMapper = floorMapper;
    this.roomMapper = roomMapper;
    this.bedMapper = bedMapper;
  }

  @Override
  public FloorResponse create(FloorRequest request) {
    ensureFloorUnique(null, request.getTenantId(), request.getBuildingId(), request.getFloorNo());
    Floor floor = new Floor();
    floor.setTenantId(request.getTenantId());
    floor.setOrgId(request.getOrgId());
    floor.setBuildingId(request.getBuildingId());
    floor.setFloorNo(request.getFloorNo());
    floor.setName(request.getName());
    floor.setStatus(request.getStatus());
    floor.setSortNo(request.getSortNo());
    floor.setCreatedBy(request.getCreatedBy());
    floorMapper.insert(floor);
    return toResponse(floor);
  }

  @Override
  public FloorResponse update(Long id, FloorRequest request) {
    Floor floor = floorMapper.selectById(id);
    if (floor == null) {
      return null;
    }
    if (!request.getTenantId().equals(floor.getTenantId())) {
      throw new IllegalArgumentException("无权限访问该楼层");
    }
    ensureFloorUnique(id, request.getTenantId(), request.getBuildingId(), request.getFloorNo());
    floor.setBuildingId(request.getBuildingId());
    floor.setFloorNo(request.getFloorNo());
    floor.setName(request.getName());
    floor.setStatus(request.getStatus());
    floor.setSortNo(request.getSortNo());
    floorMapper.updateById(floor);
    syncRoomFloorNo(floor);
    return toResponse(floor);
  }

  @Override
  public FloorResponse get(Long id) {
    Floor floor = floorMapper.selectById(id);
    return floor == null ? null : toResponse(floor);
  }

  @Override
  public IPage<FloorResponse> page(Long tenantId, long pageNo, long pageSize, Long buildingId, String keyword, Integer status) {
    var wrapper = Wrappers.lambdaQuery(Floor.class)
        .eq(Floor::getIsDeleted, 0)
        .eq(tenantId != null, Floor::getTenantId, tenantId)
        .eq(buildingId != null, Floor::getBuildingId, buildingId);
    if (status != null) {
      wrapper.eq(Floor::getStatus, status);
    }
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(Floor::getFloorNo, keyword).or().like(Floor::getName, keyword));
    }
    IPage<Floor> page = floorMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    return page.convert(this::toResponse);
  }

  @Override
  public java.util.List<FloorResponse> list(Long tenantId, Long buildingId) {
    var wrapper = Wrappers.lambdaQuery(Floor.class)
        .eq(Floor::getIsDeleted, 0)
        .eq(tenantId != null, Floor::getTenantId, tenantId)
        .eq(buildingId != null, Floor::getBuildingId, buildingId)
        .orderByAsc(Floor::getSortNo)
        .orderByAsc(Floor::getFloorNo);
    return floorMapper.selectList(wrapper).stream().map(this::toResponse).toList();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void delete(Long id, Long tenantId) {
    Floor floor = floorMapper.selectById(id);
    if (floor == null || Integer.valueOf(1).equals(floor.getIsDeleted())) {
      throw new IllegalArgumentException("楼层不存在或已删除");
    }
    if (tenantId == null || !tenantId.equals(floor.getTenantId())) {
      throw new IllegalArgumentException("无权限删除该楼层");
    }
    List<Room> rooms = roomMapper.selectList(Wrappers.lambdaQuery(Room.class)
        .eq(Room::getIsDeleted, 0)
        .eq(Room::getTenantId, tenantId)
        .eq(Room::getFloorId, id));
    List<Long> roomIds = rooms.stream().map(Room::getId).toList();
    List<Bed> beds = roomIds.isEmpty()
        ? List.of()
        : bedMapper.selectList(Wrappers.lambdaQuery(Bed.class)
            .eq(Bed::getIsDeleted, 0)
            .eq(Bed::getTenantId, tenantId)
            .in(Bed::getRoomId, roomIds));
    boolean hasOccupiedBed = beds.stream().anyMatch(bed ->
        bed.getElderId() != null || Integer.valueOf(2).equals(bed.getStatus()));
    if (hasOccupiedBed) {
      throw new IllegalArgumentException("当前楼层下存在已入住床位，请先办理退床或换床");
    }
    for (Bed bed : beds) {
      bed.setIsDeleted(1);
      bedMapper.updateById(bed);
    }
    for (Room room : rooms) {
      room.setIsDeleted(1);
      roomMapper.updateById(room);
    }
    floor.setIsDeleted(1);
    floorMapper.updateById(floor);
  }

  private void syncRoomFloorNo(Floor floor) {
    if (floor == null || floor.getId() == null) {
      return;
    }
    roomMapper.selectList(Wrappers.lambdaQuery(Room.class)
            .eq(Room::getIsDeleted, 0)
            .eq(Room::getTenantId, floor.getTenantId())
            .eq(Room::getFloorId, floor.getId()))
        .forEach(room -> {
          room.setFloorNo(floor.getFloorNo());
          roomMapper.updateById(room);
        });
  }

  private void ensureFloorUnique(Long id, Long tenantId, Long buildingId, String floorNo) {
    if (tenantId == null || buildingId == null || floorNo == null || floorNo.isBlank()) {
      return;
    }
    var wrapper = Wrappers.lambdaQuery(Floor.class)
        .eq(Floor::getIsDeleted, 0)
        .eq(Floor::getTenantId, tenantId)
        .eq(Floor::getBuildingId, buildingId)
        .eq(Floor::getFloorNo, floorNo);
    if (id != null) {
      wrapper.ne(Floor::getId, id);
    }
    if (floorMapper.selectCount(wrapper) > 0) {
      throw new IllegalArgumentException("楼层编号已存在");
    }
  }

  private FloorResponse toResponse(Floor floor) {
    FloorResponse response = new FloorResponse();
    response.setId(floor.getId());
    response.setTenantId(floor.getTenantId());
    response.setOrgId(floor.getOrgId());
    response.setBuildingId(floor.getBuildingId());
    response.setFloorNo(floor.getFloorNo());
    response.setName(floor.getName());
    response.setStatus(floor.getStatus());
    response.setSortNo(floor.getSortNo());
    return response;
  }
}
