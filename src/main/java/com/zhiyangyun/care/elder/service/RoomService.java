package com.zhiyangyun.care.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.model.RoomRequest;
import com.zhiyangyun.care.elder.model.RoomResponse;

public interface RoomService {
  RoomResponse create(RoomRequest request);

  RoomResponse update(Long id, RoomRequest request);

  RoomResponse get(Long id);

  IPage<RoomResponse> page(Long orgId, long pageNo, long pageSize, String keyword);

  java.util.List<RoomResponse> list(Long orgId);

  void delete(Long id);
}
