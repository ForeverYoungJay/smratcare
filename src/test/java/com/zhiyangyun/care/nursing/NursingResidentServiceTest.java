package com.zhiyangyun.care.nursing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.assessment.entity.AssessmentRecord;
import com.zhiyangyun.care.assessment.mapper.AssessmentRecordMapper;
import com.zhiyangyun.care.elder.model.ElderResponse;
import com.zhiyangyun.care.elder.service.ElderService;
import com.zhiyangyun.care.entity.CareTaskTemplate;
import com.zhiyangyun.care.nursing.model.NursingResidentItem;
import com.zhiyangyun.care.nursing.service.NursingResidentService;
import com.zhiyangyun.care.service.impl.CareTaskServiceImpl;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NursingResidentServiceTest {
  private static final LocalDate TODAY = LocalDate.of(2026, 11, 17);

  @BeforeAll
  static void initTableInfo() {
    // lambda select 需要实体表信息；纯 mock 测试没有 Spring 帮忙注册
    TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), AssessmentRecord.class);
  }

  private static AssessmentRecord record(long elderId, LocalDate date, LocalDate next, String level) {
    AssessmentRecord r = new AssessmentRecord();
    r.setElderId(elderId);
    r.setAssessmentDate(date);
    r.setNextAssessmentDate(next);
    r.setLevelCode(level);
    return r;
  }

  @Test
  void reassessFilterUsesOnlyTheLatestAssessmentPerElder() {
    ElderService elderService = mock(ElderService.class);
    AssessmentRecordMapper mapper = mock(AssessmentRecordMapper.class);
    // 已按评估日期倒序：1 号最新一次复评日还没到（更早那条过期的不算）；2 号最新一次今天到期
    when(mapper.selectList(any())).thenReturn(List.of(
        record(1, TODAY.minusDays(5), TODAY.plusDays(20), "B"),
        record(2, TODAY.minusDays(30), TODAY, "C"),
        record(1, TODAY.minusDays(90), TODAY.minusDays(10), "A")));
    ElderResponse elder = new ElderResponse();
    elder.setId(2L);
    Page<ElderResponse> page = new Page<>(1, 20, 1);
    page.setRecords(List.of(elder));
    when(elderService.pageWithin(eq(9L), eq(1L), eq(20L), any(), eq("IN_HOSPITAL"), any())).thenReturn(page);

    var result = new NursingResidentService(elderService, mapper).page(9L, 1, 20, null, true, TODAY);

    verify(elderService).pageWithin(9L, 1, 20, null, "IN_HOSPITAL", Set.of(2L));
    NursingResidentItem item = result.getRecords().get(0);
    assertEquals("C", item.getLatestLevelCode());
    assertEquals(TODAY, item.getNextAssessmentDate());
  }

  @Test
  void templateChecklistSkipsBlankLines() {
    CareTaskTemplate template = new CareTaskTemplate();
    template.setServiceItems("协助洗脸\n\n  梳头 \n口腔清洁\n");
    assertEquals(List.of("协助洗脸", "梳头", "口腔清洁"), CareTaskServiceImpl.splitServiceItems(template));
  }
}
