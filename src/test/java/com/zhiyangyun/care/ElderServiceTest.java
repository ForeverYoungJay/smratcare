package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderBedRelation;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderBedRelationMapper;
import com.zhiyangyun.care.elder.model.AssignBedRequest;
import com.zhiyangyun.care.elder.service.ElderService;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
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
  private BedMapper bedMapper;

  @Autowired
  private ElderMapper elderMapper;

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
  void page_query() {
    var page = elderService.page(null, 1, 10, "Elder", false, null, null, null, null, null, null, null, null);
    assertNotNull(page);
    assertEquals(1, page.getCurrent());
  }
}
