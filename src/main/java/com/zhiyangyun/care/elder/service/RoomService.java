package com.zhiyangyun.care.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.model.RoomRequest;
import com.zhiyangyun.care.elder.model.RoomResponse;

public interface RoomService {
  RoomResponse create(RoomRequest request);

  RoomResponse update(Long id, RoomRequest request);

  RoomResponse get(Long id, Long tenantId);

  IPage<RoomResponse> page(Long tenantId, long pageNo, long pageSize,
      String keyword, String roomNo, String building, String floorNo, Long buildingId, Long floorId, Integer status);

  java.util.List<RoomResponse> list(Long tenantId);

  void delete(Long id, Long tenantId);
}
