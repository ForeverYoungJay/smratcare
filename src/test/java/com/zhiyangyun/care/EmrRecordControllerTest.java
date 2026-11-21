package com.zhiyangyun.care;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.zhiyangyun.care.emr.controller.EmrRecordController;
import com.zhiyangyun.care.emr.entity.EmrRecord;
import com.zhiyangyun.care.emr.mapper.EmrRecordMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class EmrRecordControllerTest {

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void get_should_scope_record_to_current_org_and_active_rows() {
    var auth = new UsernamePasswordAuthenticationToken("100", null, List.of());
    auth.setDetails(Map.of("orgId", 7L));
    SecurityContextHolder.getContext().setAuthentication(auth);

    EmrRecordMapper mapper = mock(EmrRecordMapper.class);
    when(mapper.selectOne(any(Wrapper.class))).thenReturn(null);
    new EmrRecordController(mapper).get(99L);

    @SuppressWarnings("rawtypes")
    ArgumentCaptor<Wrapper> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
    verify(mapper).selectOne(wrapperCaptor.capture());
    Wrapper<?> wrapper = wrapperCaptor.getValue();
    assertThat(wrapper.getExpression().getNormal().getSqlSegment())
        .contains("id", "is_deleted", "org_id");
    assertThat(((AbstractWrapper<?, ?, ?>) wrapper).getParamNameValuePairs().values()).contains(99L, 0, 7L);
  }
}
