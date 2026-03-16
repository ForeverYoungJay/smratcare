package com.zhiyangyun.care.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zhiyangyun.care.asset.entity.Building;
import com.zhiyangyun.care.asset.entity.Floor;
import com.zhiyangyun.care.asset.mapper.BuildingMapper;
import com.zhiyangyun.care.asset.mapper.FloorMapper;
import com.zhiyangyun.care.asset.model.ResidenceBootstrapRequest;
import com.zhiyangyun.care.asset.model.ResidenceBootstrapResponse;
import com.zhiyangyun.care.asset.service.impl.ResidenceBootstrapServiceImpl;
import com.zhiyangyun.care.baseconfig.mapper.BaseDataItemMapper;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResidenceBootstrapServiceImplTest {
  @Mock
  private BuildingMapper buildingMapper;

  @Mock
  private FloorMapper floorMapper;

  @Mock
  private RoomMapper roomMapper;

  @Mock
  private BedMapper bedMapper;

  @Mock
  private BaseDataItemMapper baseDataItemMapper;

  private ResidenceBootstrapServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new ResidenceBootstrapServiceImpl(
        buildingMapper, floorMapper, roomMapper, bedMapper, baseDataItemMapper);
  }

  @Test
  void bootstrap_skips_existing_building_code() {
    ResidenceBootstrapRequest request = new ResidenceBootstrapRequest();
    request.setBuildingCount(1);
    request.setFloorsPerBuilding(1);
    request.setRoomsPerFloor(1);
    request.setBedsPerRoom(1);
    request.setStartNo(3);
    request.setRoomType("ROOM_SINGLE");
    request.setBedType("BED_STANDARD");

    when(baseDataItemMapper.selectList(any())).thenReturn(List.of());
    when(buildingMapper.selectCount(any())).thenReturn(0L, 1L, 0L, 0L);
    when(roomMapper.selectCount(any())).thenReturn(0L);

    AtomicLong ids = new AtomicLong(100L);
    doAnswer(invocation -> {
      Building building = invocation.getArgument(0);
      building.setId(ids.getAndIncrement());
      return 1;
    }).when(buildingMapper).insert(any(Building.class));
    doAnswer(invocation -> {
      Floor floor = invocation.getArgument(0);
      floor.setId(ids.getAndIncrement());
      return 1;
    }).when(floorMapper).insert(any(Floor.class));
    doAnswer(invocation -> {
      Room room = invocation.getArgument(0);
      room.setId(ids.getAndIncrement());
      return 1;
    }).when(roomMapper).insert(any(Room.class));
    doAnswer(invocation -> {
      Bed bed = invocation.getArgument(0);
      bed.setId(ids.getAndIncrement());
      return 1;
    }).when(bedMapper).insert(any(Bed.class));

    ResidenceBootstrapResponse response = service.bootstrap(1L, 99L, request);

    ArgumentCaptor<Building> buildingCaptor = ArgumentCaptor.forClass(Building.class);
    verify(buildingMapper).insert(buildingCaptor.capture());
    assertEquals("楼栋4", buildingCaptor.getValue().getName());
    assertEquals("B4", buildingCaptor.getValue().getCode());
    assertEquals(1, response.getBuildingCount());
    assertEquals(1, response.getFloorCount());
    assertEquals(1, response.getRoomCount());
    assertEquals(1, response.getBedCount());
  }
}
