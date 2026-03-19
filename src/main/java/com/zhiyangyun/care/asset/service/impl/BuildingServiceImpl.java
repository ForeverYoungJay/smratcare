package com.zhiyangyun.care.asset.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.asset.entity.Building;
import com.zhiyangyun.care.asset.entity.Floor;
import com.zhiyangyun.care.asset.mapper.BuildingMapper;
import com.zhiyangyun.care.asset.mapper.FloorMapper;
import com.zhiyangyun.care.asset.model.BuildingRequest;
import com.zhiyangyun.care.asset.model.BuildingResponse;
import com.zhiyangyun.care.asset.service.BuildingService;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BuildingServiceImpl implements BuildingService {
  private final BuildingMapper buildingMapper;
  private final FloorMapper floorMapper;
  private final RoomMapper roomMapper;
  private final BedMapper bedMapper;

  public BuildingServiceImpl(BuildingMapper buildingMapper, FloorMapper floorMapper, RoomMapper roomMapper, BedMapper bedMapper) {
    this.buildingMapper = buildingMapper;
    this.floorMapper = floorMapper;
    this.roomMapper = roomMapper;
    this.bedMapper = bedMapper;
  }

  @Override
  public BuildingResponse create(BuildingRequest request) {
    ensureNameUnique(null, request.getTenantId(), request.getName());
    Building building = new Building();
    building.setTenantId(request.getTenantId());
    building.setOrgId(request.getOrgId());
    building.setName(request.getName());
    building.setCode(request.getCode());
    building.setAreaCode(request.getAreaCode());
    building.setAreaName(request.getAreaName());
    building.setStatus(request.getStatus());
    building.setSortNo(request.getSortNo());
    building.setRemark(request.getRemark());
    building.setCreatedBy(request.getCreatedBy());
    buildingMapper.insert(building);
    return toResponse(building);
  }

  @Override
  public BuildingResponse update(Long id, BuildingRequest request) {
    Building building = buildingMapper.selectById(id);
    if (building == null) {
      return null;
    }
    if (!request.getTenantId().equals(building.getTenantId())) {
      throw new IllegalArgumentException("无权限访问该楼栋");
    }
    ensureNameUnique(id, request.getTenantId(), request.getName());
    building.setName(request.getName());
    building.setCode(request.getCode());
    building.setAreaCode(request.getAreaCode());
    building.setAreaName(request.getAreaName());
    building.setStatus(request.getStatus());
    building.setSortNo(request.getSortNo());
    building.setRemark(request.getRemark());
    buildingMapper.updateById(building);
    syncRoomBuildingName(building);
    return toResponse(building);
  }

  @Override
  public BuildingResponse get(Long id) {
    Building building = buildingMapper.selectById(id);
    return building == null ? null : toResponse(building);
  }

  @Override
  public IPage<BuildingResponse> page(Long tenantId, long pageNo, long pageSize, String keyword, Integer status) {
    var wrapper = Wrappers.lambdaQuery(Building.class)
        .eq(Building::getIsDeleted, 0)
        .eq(tenantId != null, Building::getTenantId, tenantId);
    if (status != null) {
      wrapper.eq(Building::getStatus, status);
    }
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(Building::getName, keyword).or().like(Building::getCode, keyword));
    }
    IPage<Building> page = buildingMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    return page.convert(this::toResponse);
  }

  @Override
  public java.util.List<BuildingResponse> list(Long tenantId) {
    var wrapper = Wrappers.lambdaQuery(Building.class)
        .eq(Building::getIsDeleted, 0)
        .eq(tenantId != null, Building::getTenantId, tenantId)
        .orderByAsc(Building::getSortNo)
        .orderByAsc(Building::getName);
    return buildingMapper.selectList(wrapper).stream().map(this::toResponse).toList();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void delete(Long id, Long tenantId) {
    Building building = buildingMapper.selectById(id);
    if (building == null || Integer.valueOf(1).equals(building.getIsDeleted())) {
      throw new IllegalArgumentException("楼栋不存在或已删除");
    }
    if (tenantId == null || !tenantId.equals(building.getTenantId())) {
      throw new IllegalArgumentException("无权限删除该楼栋");
    }
    List<Floor> floors = floorMapper.selectList(Wrappers.lambdaQuery(Floor.class)
        .eq(Floor::getIsDeleted, 0)
        .eq(Floor::getTenantId, tenantId)
        .eq(Floor::getBuildingId, id));
    List<Room> rooms = roomMapper.selectList(Wrappers.lambdaQuery(Room.class)
        .eq(Room::getIsDeleted, 0)
        .eq(Room::getTenantId, tenantId)
        .eq(Room::getBuildingId, id));
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
      throw new IllegalArgumentException("当前楼栋下存在已入住床位，请先办理退床或换床");
    }
    for (Bed bed : beds) {
      bed.setIsDeleted(1);
      bedMapper.updateById(bed);
    }
    for (Room room : rooms) {
      room.setIsDeleted(1);
      roomMapper.updateById(room);
    }
    for (Floor floor : floors) {
      floor.setIsDeleted(1);
      floorMapper.updateById(floor);
    }
    building.setIsDeleted(1);
    buildingMapper.updateById(building);
  }

  private void syncRoomBuildingName(Building building) {
    if (building == null || building.getId() == null) {
      return;
    }
    roomMapper.selectList(Wrappers.lambdaQuery(Room.class)
            .eq(Room::getIsDeleted, 0)
            .eq(Room::getTenantId, building.getTenantId())
            .eq(Room::getBuildingId, building.getId()))
        .forEach(room -> {
          room.setBuilding(building.getName());
          roomMapper.updateById(room);
        });
  }

  private void ensureNameUnique(Long id, Long tenantId, String name) {
    if (tenantId == null || name == null || name.isBlank()) {
      return;
    }
    var wrapper = Wrappers.lambdaQuery(Building.class)
        .eq(Building::getIsDeleted, 0)
        .eq(Building::getTenantId, tenantId)
        .eq(Building::getName, name);
    if (id != null) {
      wrapper.ne(Building::getId, id);
    }
    if (buildingMapper.selectCount(wrapper) > 0) {
      throw new IllegalArgumentException("楼栋名称已存在");
    }
  }

  private BuildingResponse toResponse(Building building) {
    BuildingResponse response = new BuildingResponse();
    response.setId(building.getId());
    response.setTenantId(building.getTenantId());
    response.setOrgId(building.getOrgId());
    response.setName(building.getName());
    response.setCode(building.getCode());
    response.setAreaCode(building.getAreaCode());
    response.setAreaName(building.getAreaName());
    response.setStatus(building.getStatus());
    response.setSortNo(building.getSortNo());
    response.setRemark(building.getRemark());
    return response;
  }
}
