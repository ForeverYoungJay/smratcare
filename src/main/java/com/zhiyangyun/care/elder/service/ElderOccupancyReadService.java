package com.zhiyangyun.care.elder.service;

import com.zhiyangyun.care.elder.entity.Bed;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ElderOccupancyReadService {
  Map<Long, Bed> buildOccupiedBedMapByElderId(Long orgId, List<Bed> beds);

  Map<Long, Bed> loadOccupiedBedMapByElderIds(Long orgId, List<Long> elderIds);

  long countOccupiedBedsByElderIds(Long orgId, List<Bed> beds, Set<Long> elderIds);
}
