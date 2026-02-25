package com.zhiyangyun.care.nursing.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.nursing.model.NursingRecordItem;
import java.time.LocalDateTime;

public interface NursingRecordService {
  IPage<NursingRecordItem> page(Long tenantId, long pageNo, long pageSize, LocalDateTime timeFrom,
      LocalDateTime timeTo, Long elderId, Long staffId, String keyword);
}
