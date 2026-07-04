package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zhiyangyun.care.asset.mapper.BuildingMapper;
import com.zhiyangyun.care.asset.mapper.FloorMapper;
import com.zhiyangyun.care.assessment.mapper.AssessmentRecordMapper;
import com.zhiyangyun.care.crm.entity.CrmContract;
import com.zhiyangyun.care.crm.mapper.CrmContractMapper;
import com.zhiyangyun.care.crm.mapper.CrmLeadMapper;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderBedRelation;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderBedRelationMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.elder.model.BedRequest;
import com.zhiyangyun.care.elder.model.BedResponse;
import com.zhiyangyun.care.elder.model.BedStatus;
import com.zhiyangyun.care.elder.service.impl.BedServiceImpl;
import com.zhiyangyun.care.health.mapper.HealthDataRecordMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BedServiceImplTest {

  @Mock
  private BedMapper bedMapper;
  @Mock
  private RoomMapper roomMapper;
  @Mock
  private ElderBedRelationMapper relationMapper;
  @Mock
  private ElderMapper elderMapper;
  @Mock
  private BuildingMapper buildingMapper;
  @Mock
  private FloorMapper floorMapper;
  @Mock
  private AssessmentRecordMapper assessmentRecordMapper;
  @Mock
  private HealthDataRecordMapper healthDataRecordMapper;
  @Mock
  private CrmLeadMapper crmLeadMapper;
  @Mock
  private CrmContractMapper crmContractMapper;

  private BedServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new BedServiceImpl(
        bedMapper,
        roomMapper,
        relationMapper,
        elderMapper,
        buildingMapper,
        floorMapper,
        assessmentRecordMapper,
        healthDataRecordMapper,
        crmLeadMapper,
        crmContractMapper);
  }

  @Test
  void delete_should_reject_relation_backed_occupancy_when_bed_projection_is_empty() {
    Bed bed = new Bed();
    bed.setId(11L);
    bed.setTenantId(1L);
    bed.setOrgId(1L);
    bed.setRoomId(21L);
    bed.setStatus(BedStatus.AVAILABLE);
    bed.setElderId(null);
    bed.setIsDeleted(0);

    ElderBedRelation relation = new ElderBedRelation();
    relation.setId(31L);
    relation.setBedId(11L);
    relation.setElderId(41L);
    relation.setActiveFlag(1);
    relation.setIsDeleted(0);

    when(bedMapper.selectById(11L)).thenReturn(bed);
    when(relationMapper.selectList(any())).thenReturn(List.of(relation));

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.delete(11L, 1L));
    assertEquals("当前床位已绑定长者，请先办理退床或换床", ex.getMessage());
  }

  @Test
  void update_should_return_relation_backed_occupancy_when_bed_projection_is_empty() {
    Bed bed = new Bed();
    bed.setId(12L);
    bed.setTenantId(1L);
    bed.setOrgId(1L);
    bed.setRoomId(22L);
    bed.setBedNo("01");
    bed.setBedType("护理床");
    bed.setStatus(BedStatus.AVAILABLE);
    bed.setElderId(null);
    bed.setIsDeleted(0);

    ElderBedRelation relation = new ElderBedRelation();
    relation.setId(32L);
    relation.setBedId(12L);
    relation.setElderId(42L);
    relation.setActiveFlag(1);
    relation.setIsDeleted(0);

    ElderProfile elder = new ElderProfile();
    elder.setId(42L);
    elder.setFullName("关系占床长者");
    elder.setLifecycleStatus("IN_HOSPITAL");

    BedRequest request = new BedRequest();
    request.setTenantId(1L);
    request.setOrgId(1L);
    request.setRoomId(22L);
    request.setBedNo("01");
    request.setBedType("护理床");
    request.setStatus(BedStatus.OCCUPIED);

    when(bedMapper.selectById(12L)).thenReturn(bed);
    when(relationMapper.selectList(any())).thenReturn(List.of(relation), List.of(relation));
    when(roomMapper.selectList(any())).thenReturn(List.of());
    when(elderMapper.selectBatchIds(any())).thenReturn(List.of(elder));
    when(crmContractMapper.selectList(any())).thenReturn(List.of());
    when(crmLeadMapper.selectList(any())).thenReturn(List.of());

    BedResponse response = service.update(12L, request);

    assertEquals(BedStatus.OCCUPIED, response.getStatus());
    assertEquals(42L, response.getElderId());
    assertEquals("RELATION", response.getOccupancySource());
    assertEquals("ELDER_BED_RELATION", response.getOccupancyRefType());
    assertEquals(32L, response.getOccupancyRefId());
    assertEquals("关系占床长者", response.getElderName());
    verify(bedMapper).updateById(any(Bed.class));
  }
}
