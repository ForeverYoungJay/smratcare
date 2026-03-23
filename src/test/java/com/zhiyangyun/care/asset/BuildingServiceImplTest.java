package com.zhiyangyun.care.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zhiyangyun.care.asset.entity.Building;
import com.zhiyangyun.care.asset.mapper.BuildingMapper;
import com.zhiyangyun.care.asset.mapper.FloorMapper;
import com.zhiyangyun.care.asset.model.BuildingRequest;
import com.zhiyangyun.care.asset.model.BuildingResponse;
import com.zhiyangyun.care.asset.service.impl.BuildingServiceImpl;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BuildingServiceImplTest {

  @Mock
  private BuildingMapper buildingMapper;

  @Mock
  private FloorMapper floorMapper;

  @Mock
  private RoomMapper roomMapper;

  @Mock
  private BedMapper bedMapper;

  @Test
  void shouldReleaseSoftDeletedBuildingNameBeforeCreate() {
    Building deleted = new Building();
    deleted.setId(11L);
    deleted.setTenantId(100L);
    deleted.setName("A座");
    deleted.setCode("B01");
    deleted.setIsDeleted(1);

    when(buildingMapper.selectList(any())).thenReturn(List.of(deleted));
    when(buildingMapper.selectCount(any())).thenReturn(0L);
    when(buildingMapper.insert(any())).thenAnswer(invocation -> {
      Building building = invocation.getArgument(0);
      building.setId(22L);
      return 1;
    });

    BuildingServiceImpl service = new BuildingServiceImpl(buildingMapper, floorMapper, roomMapper, bedMapper);

    BuildingRequest request = new BuildingRequest();
    request.setTenantId(100L);
    request.setOrgId(100L);
    request.setCreatedBy(99L);
    request.setName("A座");
    request.setCode("B01");
    request.setStatus(1);
    request.setSortNo(1);

    BuildingResponse response = service.create(request);

    assertEquals(22L, response.getId());

    ArgumentCaptor<Building> deletedCaptor = ArgumentCaptor.forClass(Building.class);
    verify(buildingMapper).updateById(deletedCaptor.capture());
    Building releasedDeleted = deletedCaptor.getValue();
    assertEquals("A座_DEL_11", releasedDeleted.getName());
    assertEquals("B01_DEL_11", releasedDeleted.getCode());

    ArgumentCaptor<Building> insertCaptor = ArgumentCaptor.forClass(Building.class);
    verify(buildingMapper).insert(insertCaptor.capture());
    Building created = insertCaptor.getValue();
    assertEquals("A座", created.getName());
    assertEquals("B01", created.getCode());
  }
}
