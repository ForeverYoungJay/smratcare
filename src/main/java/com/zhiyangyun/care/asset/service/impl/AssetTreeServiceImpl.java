package com.zhiyangyun.care.asset.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.asset.entity.Building;
import com.zhiyangyun.care.asset.entity.Floor;
import com.zhiyangyun.care.asset.mapper.BuildingMapper;
import com.zhiyangyun.care.asset.mapper.FloorMapper;
import com.zhiyangyun.care.asset.model.AssetTreeNode;
import com.zhiyangyun.care.asset.service.AssetTreeService;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class AssetTreeServiceImpl implements AssetTreeService {
  private final BuildingMapper buildingMapper;
  private final FloorMapper floorMapper;
  private final RoomMapper roomMapper;
  private final BedMapper bedMapper;

  public AssetTreeServiceImpl(BuildingMapper buildingMapper, FloorMapper floorMapper,
                              RoomMapper roomMapper, BedMapper bedMapper) {
    this.buildingMapper = buildingMapper;
    this.floorMapper = floorMapper;
    this.roomMapper = roomMapper;
    this.bedMapper = bedMapper;
  }

  @Override
  public List<AssetTreeNode> getTree(Long tenantId) {
    if (tenantId == null) {
      return List.of();
    }
    List<Building> buildings = buildingMapper.selectList(Wrappers.lambdaQuery(Building.class)
        .eq(Building::getIsDeleted, 0)
        .eq(Building::getTenantId, tenantId)
        .orderByAsc(Building::getSortNo));

    List<Floor> floors = floorMapper.selectList(Wrappers.lambdaQuery(Floor.class)
        .eq(Floor::getIsDeleted, 0)
        .eq(Floor::getTenantId, tenantId)
        .orderByAsc(Floor::getSortNo));

    List<Room> rooms = roomMapper.selectList(Wrappers.lambdaQuery(Room.class)
        .eq(Room::getIsDeleted, 0)
        .eq(Room::getTenantId, tenantId)
        .orderByAsc(Room::getRoomNo));

    List<Bed> beds = bedMapper.selectList(Wrappers.lambdaQuery(Bed.class)
        .eq(Bed::getIsDeleted, 0)
        .eq(Bed::getTenantId, tenantId)
        .orderByAsc(Bed::getBedNo));

    Map<Long, List<Floor>> floorByBuilding = floors.stream()
        .filter(f -> f.getBuildingId() != null)
        .collect(Collectors.groupingBy(Floor::getBuildingId));
    Map<Long, List<Room>> roomByFloor = rooms.stream()
        .filter(r -> r.getFloorId() != null)
        .collect(Collectors.groupingBy(Room::getFloorId));
    Map<Long, List<Bed>> bedByRoom = beds.stream()
        .filter(b -> b.getRoomId() != null)
        .collect(Collectors.groupingBy(Bed::getRoomId));

    List<AssetTreeNode> result = new ArrayList<>();
    for (Building building : buildings) {
      AssetTreeNode buildingNode = new AssetTreeNode();
      buildingNode.setType("BUILDING");
      buildingNode.setId(building.getId());
      buildingNode.setName(building.getName());
      buildingNode.setStatus(building.getStatus());

      List<Floor> floorList = floorByBuilding.getOrDefault(building.getId(), List.of());
      for (Floor floor : floorList) {
        AssetTreeNode floorNode = new AssetTreeNode();
        floorNode.setType("FLOOR");
        floorNode.setId(floor.getId());
        floorNode.setName(floor.getFloorNo());
        floorNode.setStatus(floor.getStatus());

        List<Room> roomList = roomByFloor.getOrDefault(floor.getId(), List.of());
        for (Room room : roomList) {
          AssetTreeNode roomNode = new AssetTreeNode();
          roomNode.setType("ROOM");
          roomNode.setId(room.getId());
          roomNode.setName(room.getRoomNo());
          roomNode.setRoomNo(room.getRoomNo());
          roomNode.setQrCode(room.getRoomQrCode());
          roomNode.setStatus(room.getStatus());

          List<Bed> bedList = bedByRoom.getOrDefault(room.getId(), List.of());
          for (Bed bed : bedList) {
            AssetTreeNode bedNode = new AssetTreeNode();
            bedNode.setType("BED");
            bedNode.setId(bed.getId());
            bedNode.setName(bed.getBedNo());
            bedNode.setBedNo(bed.getBedNo());
            bedNode.setQrCode(bed.getBedQrCode());
            bedNode.setStatus(bed.getStatus());
            roomNode.getChildren().add(bedNode);
          }
          floorNode.getChildren().add(roomNode);
        }
        buildingNode.getChildren().add(floorNode);
      }
      result.add(buildingNode);
    }

    List<Long> roomIds = rooms.stream().map(Room::getId).filter(Objects::nonNull).toList();
    if (buildings.isEmpty() && !roomIds.isEmpty()) {
      Map<Long, Room> roomMap = rooms.stream().collect(Collectors.toMap(Room::getId, Function.identity()));
      for (Long roomId : roomIds) {
        Room room = roomMap.get(roomId);
        AssetTreeNode roomNode = new AssetTreeNode();
        roomNode.setType("ROOM");
        roomNode.setId(room.getId());
        roomNode.setName(room.getRoomNo());
        roomNode.setRoomNo(room.getRoomNo());
        roomNode.setQrCode(room.getRoomQrCode());
        roomNode.setStatus(room.getStatus());
        result.add(roomNode);
      }
    }
    return result;
  }
}
