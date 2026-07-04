package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderBedRelation;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.mapper.ElderBedRelationMapper;
import com.zhiyangyun.care.elder.model.AssignBedRequest;
import com.zhiyangyun.care.elder.model.BedStatus;
import com.zhiyangyun.care.elder.service.ElderOccupancyService;
import com.zhiyangyun.care.elder.service.ElderService;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ElderServiceTest {
  @Autowired
  private ElderService elderService;

  @Autowired
  private ElderOccupancyService elderOccupancyService;

  @Autowired
  private BedMapper bedMapper;

  @Autowired
  private ElderMapper elderMapper;

  @Autowired
  private RoomMapper roomMapper;

  @Autowired
  private ElderBedRelationMapper relationMapper;

  @Test
  void bed_exclusive() {
    AssignBedRequest request = new AssignBedRequest();
    request.setBedId(100L);
    request.setStartDate(LocalDate.now());

    IllegalStateException ex = assertThrows(IllegalStateException.class,
        () -> elderService.assignBed(201L, request));
    assertEquals("Bed already assigned", ex.getMessage());
  }

  @Test
  void bind_and_unbind() {
    Bed newBed = new Bed();
    newBed.setOrgId(1L);
    newBed.setRoomId(10L);
    newBed.setBedNo("02");
    newBed.setBedQrCode("QR-NEW");
    newBed.setStatus(1);
    bedMapper.insert(newBed);

    AssignBedRequest request = new AssignBedRequest();
    request.setBedId(newBed.getId());
    request.setStartDate(LocalDate.now());

    var bound = elderService.assignBed(200L, request);
    assertEquals(newBed.getId(), bound.getBedId());

    var unbound = elderService.unbindBed(200L, LocalDate.now(), "discharge", null, null);
    assertNull(unbound.getBedId());
  }

  @Test
  void reassign_same_bed_is_idempotent() {
    Bed newBed = new Bed();
    newBed.setOrgId(1L);
    newBed.setRoomId(10L);
    newBed.setBedNo("03");
    newBed.setBedQrCode("QR-IDEMPOTENT");
    newBed.setStatus(1);
    bedMapper.insert(newBed);

    AssignBedRequest request = new AssignBedRequest();
    request.setBedId(newBed.getId());
    request.setStartDate(LocalDate.now());

    var first = elderService.assignBed(200L, request);
    var second = elderService.assignBed(200L, request);

    assertEquals(newBed.getId(), first.getBedId());
    assertEquals(newBed.getId(), second.getBedId());
    long activeCount = relationMapper.selectCount(
        Wrappers.lambdaQuery(ElderBedRelation.class)
            .eq(ElderBedRelation::getElderId, 200L)
            .eq(ElderBedRelation::getBedId, newBed.getId())
            .eq(ElderBedRelation::getActiveFlag, 1)
            .eq(ElderBedRelation::getIsDeleted, 0));
    assertEquals(1L, activeCount);

    elderService.unbindBed(200L, LocalDate.now(), "cleanup", null, null);
  }

  @Test
  void assign_bed_should_count_relation_backed_occupancy_for_room_capacity() {
    Room room = new Room();
    room.setTenantId(1L);
    room.setOrgId(1L);
    room.setRoomNo("REL-CAP-" + System.nanoTime());
    room.setCapacity(1);
    room.setStatus(1);
    roomMapper.insert(room);

    Bed occupiedBed = new Bed();
    occupiedBed.setTenantId(1L);
    occupiedBed.setOrgId(1L);
    occupiedBed.setRoomId(room.getId());
    occupiedBed.setBedNo("01");
    occupiedBed.setBedQrCode("QR-REL-CAP-1");
    occupiedBed.setStatus(1);
    occupiedBed.setElderId(null);
    bedMapper.insert(occupiedBed);

    Bed targetBed = new Bed();
    targetBed.setTenantId(1L);
    targetBed.setOrgId(1L);
    targetBed.setRoomId(room.getId());
    targetBed.setBedNo("02");
    targetBed.setBedQrCode("QR-REL-CAP-2");
    targetBed.setStatus(1);
    targetBed.setElderId(null);
    bedMapper.insert(targetBed);

    ElderBedRelation relation = new ElderBedRelation();
    relation.setTenantId(1L);
    relation.setOrgId(1L);
    relation.setElderId(200L);
    relation.setBedId(occupiedBed.getId());
    relation.setStartDate(LocalDate.now());
    relation.setActiveFlag(1);
    relation.setIsDeleted(0);
    relationMapper.insert(relation);

    ElderProfile targetElder = new ElderProfile();
    targetElder.setTenantId(1L);
    targetElder.setOrgId(1L);
    targetElder.setFullName("容量校验长者");
    targetElder.setElderCode("ELD-CAP-" + System.nanoTime());
    targetElder.setElderQrCode("QR-CAP-" + System.nanoTime());
    targetElder.setStatus(1);
    elderMapper.insert(targetElder);

    AssignBedRequest request = new AssignBedRequest();
    request.setBedId(targetBed.getId());
    request.setStartDate(LocalDate.now());

    IllegalStateException ex = assertThrows(IllegalStateException.class,
        () -> elderService.assignBed(targetElder.getId(), request));
    assertEquals("Room capacity exceeded", ex.getMessage());
  }

  @Test
  void release_should_prefer_active_relation_over_stale_elder_bed_projection() {
    Room room = new Room();
    room.setTenantId(1L);
    room.setOrgId(1L);
    room.setRoomNo("REL-REL-" + System.nanoTime());
    room.setCapacity(2);
    room.setStatus(1);
    roomMapper.insert(room);

    Bed staleProjectedBed = new Bed();
    staleProjectedBed.setTenantId(1L);
    staleProjectedBed.setOrgId(1L);
    staleProjectedBed.setRoomId(room.getId());
    staleProjectedBed.setBedNo("01");
    staleProjectedBed.setBedQrCode("QR-REL-STALE-" + System.nanoTime());
    staleProjectedBed.setStatus(BedStatus.OCCUPIED);
    staleProjectedBed.setElderId(200L);
    bedMapper.insert(staleProjectedBed);

    Bed relationBed = new Bed();
    relationBed.setTenantId(1L);
    relationBed.setOrgId(1L);
    relationBed.setRoomId(room.getId());
    relationBed.setBedNo("02");
    relationBed.setBedQrCode("QR-REL-ACTIVE-" + System.nanoTime());
    relationBed.setStatus(BedStatus.OCCUPIED);
    relationBed.setElderId(null);
    bedMapper.insert(relationBed);

    ElderProfile elder = elderMapper.selectById(200L);
    elder.setBedId(staleProjectedBed.getId());
    elderMapper.updateById(elder);

    ElderBedRelation relation = new ElderBedRelation();
    relation.setTenantId(1L);
    relation.setOrgId(1L);
    relation.setElderId(200L);
    relation.setBedId(relationBed.getId());
    relation.setStartDate(LocalDate.now());
    relation.setActiveFlag(1);
    relation.setIsDeleted(0);
    relationMapper.insert(relation);

    var releaseResult = elderOccupancyService.releaseBedAndCloseRelation(1L, 1L, 200L, LocalDate.now(), "relation-first");

    assertEquals(relationBed.getId(), releaseResult.getPreviousBedId());
    assertEquals(relationBed.getId(), releaseResult.getReleasedBedId());
    assertEquals(0L, relationMapper.selectCount(
        Wrappers.lambdaQuery(ElderBedRelation.class)
            .eq(ElderBedRelation::getElderId, 200L)
            .eq(ElderBedRelation::getBedId, relationBed.getId())
            .eq(ElderBedRelation::getActiveFlag, 1)
            .eq(ElderBedRelation::getIsDeleted, 0)));
    assertNull(elderMapper.selectById(200L).getBedId());
    assertEquals(BedStatus.AVAILABLE, bedMapper.selectById(relationBed.getId()).getStatus());
    assertNull(bedMapper.selectById(relationBed.getId()).getElderId());
    Bed staleAfterRelease = bedMapper.selectById(staleProjectedBed.getId());
    assertEquals(BedStatus.OCCUPIED, staleAfterRelease.getStatus());
    assertEquals(200L, staleAfterRelease.getElderId());

    staleAfterRelease.setStatus(BedStatus.AVAILABLE);
    staleAfterRelease.setElderId(null);
    bedMapper.updateById(staleAfterRelease);
  }

  @Test
  void get_should_prefer_active_relation_bed_over_stale_elder_bed_projection() {
    Room room = new Room();
    room.setTenantId(1L);
    room.setOrgId(1L);
    room.setRoomNo("REL-GET-" + System.nanoTime());
    room.setCapacity(2);
    room.setStatus(1);
    roomMapper.insert(room);

    Bed staleProjectedBed = new Bed();
    staleProjectedBed.setTenantId(1L);
    staleProjectedBed.setOrgId(1L);
    staleProjectedBed.setRoomId(room.getId());
    staleProjectedBed.setBedNo("01");
    staleProjectedBed.setBedQrCode("QR-REL-GET-STALE-" + System.nanoTime());
    staleProjectedBed.setStatus(BedStatus.OCCUPIED);
    staleProjectedBed.setElderId(200L);
    bedMapper.insert(staleProjectedBed);

    Bed relationBed = new Bed();
    relationBed.setTenantId(1L);
    relationBed.setOrgId(1L);
    relationBed.setRoomId(room.getId());
    relationBed.setBedNo("02");
    relationBed.setBedQrCode("QR-REL-GET-ACTIVE-" + System.nanoTime());
    relationBed.setStatus(BedStatus.OCCUPIED);
    relationBed.setElderId(null);
    bedMapper.insert(relationBed);

    ElderProfile elder = elderMapper.selectById(200L);
    elder.setBedId(staleProjectedBed.getId());
    elderMapper.updateById(elder);

    ElderBedRelation relation = new ElderBedRelation();
    relation.setTenantId(1L);
    relation.setOrgId(1L);
    relation.setElderId(200L);
    relation.setBedId(relationBed.getId());
    relation.setStartDate(LocalDate.now());
    relation.setActiveFlag(1);
    relation.setIsDeleted(0);
    relationMapper.insert(relation);

    var response = elderService.get(200L, 1L);

    assertEquals(relationBed.getId(), response.getBedId());
    assertEquals(relationBed.getBedNo(), response.getBedNo());
    assertEquals(room.getRoomNo(), response.getRoomNo());
    assertNotNull(response.getCurrentBed());
    assertEquals(relationBed.getId(), response.getCurrentBed().getId());
    assertEquals(relationBed.getBedNo(), response.getCurrentBed().getBedNo());

    relationMapper.update(null, Wrappers.lambdaUpdate(ElderBedRelation.class)
        .eq(ElderBedRelation::getId, relation.getId())
        .set(ElderBedRelation::getActiveFlag, 0));
    staleProjectedBed.setStatus(BedStatus.AVAILABLE);
    staleProjectedBed.setElderId(null);
    bedMapper.updateById(staleProjectedBed);
    relationBed.setStatus(BedStatus.AVAILABLE);
    relationBed.setElderId(null);
    bedMapper.updateById(relationBed);
    elder.setBedId(null);
    elderMapper.updateById(elder);
  }

  @Test
  void page_query() {
    var page = elderService.page(null, 1, 10, "Elder", false, null, null, null, null, null, null, null, null);
    assertNotNull(page);
    assertEquals(1, page.getCurrent());
  }
}
