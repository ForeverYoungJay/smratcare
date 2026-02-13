package com.zhiyangyun.care.elder.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.elder.model.BedRequest;
import com.zhiyangyun.care.elder.model.BedResponse;
import com.zhiyangyun.care.elder.service.BedService;
import com.zhiyangyun.care.elder.util.QrCodeUtil;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class BedServiceImpl implements BedService {
  private final BedMapper bedMapper;
  private final RoomMapper roomMapper;
  private final ElderMapper elderMapper;

  public BedServiceImpl(BedMapper bedMapper, RoomMapper roomMapper, ElderMapper elderMapper) {
    this.bedMapper = bedMapper;
    this.roomMapper = roomMapper;
    this.elderMapper = elderMapper;
  }

  @Override
  public BedResponse create(BedRequest request) {
    Bed bed = new Bed();
    bed.setTenantId(request.getTenantId());
    bed.setOrgId(request.getOrgId());
    bed.setRoomId(request.getRoomId());
    bed.setBedNo(request.getBedNo());
    bed.setBedQrCode(QrCodeUtil.generate());
    bed.setStatus(request.getStatus());
    bed.setCreatedBy(request.getCreatedBy());
    ensureRoomTenant(request.getTenantId(), request.getRoomId());
    bedMapper.insert(bed);
    return toResponse(bed);
  }

  @Override
  public BedResponse update(Long id, BedRequest request) {
    Bed bed = bedMapper.selectById(id);
    if (bed == null) {
      return null;
    }
    bed.setTenantId(request.getTenantId());
    bed.setOrgId(request.getOrgId());
    bed.setRoomId(request.getRoomId());
    bed.setBedNo(request.getBedNo());
    bed.setStatus(request.getStatus());
    ensureRoomTenant(request.getTenantId(), request.getRoomId());
    bedMapper.updateById(bed);
    return toResponse(bed);
  }

  @Override
  public BedResponse get(Long id, Long tenantId) {
    Bed bed = bedMapper.selectById(id);
    if (bed == null || (tenantId != null && !tenantId.equals(bed.getTenantId()))) {
      return null;
    }
    return toResponse(bed);
  }

  @Override
  public IPage<BedResponse> page(Long orgId, long pageNo, long pageSize,
      String keyword, Integer status, String bedNo, String roomNo, String elderName) {
    Long tenantId = orgId;
    var wrapper = Wrappers.lambdaQuery(Bed.class)
        .eq(Bed::getIsDeleted, 0)
        .eq(tenantId != null, Bed::getTenantId, tenantId);
    if (status != null) {
      wrapper.eq(Bed::getStatus, status);
    }
    if (bedNo != null && !bedNo.isBlank()) {
      wrapper.like(Bed::getBedNo, bedNo);
    }
    if (roomNo != null && !roomNo.isBlank()) {
      List<Long> roomIds = roomMapper.selectList(Wrappers.lambdaQuery(Room.class)
              .eq(Room::getIsDeleted, 0)
              .eq(tenantId != null, Room::getTenantId, tenantId)
              .like(Room::getRoomNo, roomNo))
          .stream().map(Room::getId).toList();
      if (roomIds.isEmpty()) {
        return new Page<>(pageNo, pageSize);
      }
      wrapper.in(Bed::getRoomId, roomIds);
    }
    if (elderName != null && !elderName.isBlank()) {
      List<Long> elderIds = elderMapper.selectList(Wrappers.lambdaQuery(ElderProfile.class)
              .eq(ElderProfile::getIsDeleted, 0)
              .eq(orgId != null, ElderProfile::getOrgId, orgId)
              .like(ElderProfile::getFullName, elderName))
          .stream().map(ElderProfile::getId).toList();
      if (elderIds.isEmpty()) {
        return new Page<>(pageNo, pageSize);
      }
      wrapper.in(Bed::getElderId, elderIds);
    }
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(Bed::getBedNo, keyword)
          .or().like(Bed::getBedQrCode, keyword));
    }
    IPage<Bed> page = bedMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    List<Bed> records = page.getRecords();
    List<Long> roomIds = records.stream()
        .map(Bed::getRoomId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, Room> roomMap = roomIds.isEmpty()
        ? Map.of()
        : roomMapper.selectBatchIds(roomIds)
            .stream()
            .collect(Collectors.toMap(Room::getId, Function.identity()));
    List<Long> elderIds = records.stream()
        .map(Bed::getElderId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, ElderProfile> elderMap = elderIds.isEmpty()
        ? Map.of()
        : elderMapper.selectBatchIds(elderIds)
            .stream()
            .collect(Collectors.toMap(ElderProfile::getId, Function.identity()));
    return page.convert(bed -> {
      BedResponse response = toResponse(bed);
      Room room = roomMap.get(bed.getRoomId());
      if (room != null) {
        response.setRoomNo(room.getRoomNo());
        response.setBuilding(room.getBuilding());
        response.setFloorNo(room.getFloorNo());
        response.setRoomQrCode(room.getRoomQrCode());
      }
      ElderProfile elder = elderMap.get(bed.getElderId());
      if (elder != null) {
        response.setElderName(elder.getFullName());
        response.setCareLevel(elder.getCareLevel());
      }
      return response;
    });
  }

  @Override
  public java.util.List<BedResponse> list(Long orgId) {
    Long tenantId = orgId;
    var wrapper = Wrappers.lambdaQuery(Bed.class)
        .eq(Bed::getIsDeleted, 0)
        .eq(tenantId != null, Bed::getTenantId, tenantId)
        .orderByAsc(Bed::getRoomId)
        .orderByAsc(Bed::getBedNo);
    return bedMapper.selectList(wrapper).stream().map(this::toResponse).toList();
  }

  @Override
  public List<BedResponse> map(Long orgId) {
    Long tenantId = orgId;
    var wrapper = Wrappers.lambdaQuery(Bed.class)
        .eq(Bed::getIsDeleted, 0)
        .eq(tenantId != null, Bed::getTenantId, tenantId)
        .orderByAsc(Bed::getRoomId)
        .orderByAsc(Bed::getBedNo);
    List<Bed> beds = bedMapper.selectList(wrapper);
    List<Long> roomIds = beds.stream()
        .map(Bed::getRoomId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, Room> roomMap = roomIds.isEmpty()
        ? Map.of()
        : roomMapper.selectBatchIds(roomIds)
            .stream()
            .collect(Collectors.toMap(Room::getId, Function.identity()));
    List<Long> elderIds = beds.stream()
        .map(Bed::getElderId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, ElderProfile> elderMap = elderIds.isEmpty()
        ? Map.of()
        : elderMapper.selectBatchIds(elderIds)
            .stream()
            .collect(Collectors.toMap(ElderProfile::getId, Function.identity()));
    return beds.stream().map(bed -> {
      BedResponse response = toResponse(bed);
      Room room = roomMap.get(bed.getRoomId());
      if (room != null) {
        response.setRoomNo(room.getRoomNo());
        response.setBuilding(room.getBuilding());
        response.setFloorNo(room.getFloorNo());
        response.setRoomQrCode(room.getRoomQrCode());
      }
      ElderProfile elder = elderMap.get(bed.getElderId());
      if (elder != null) {
        response.setElderName(elder.getFullName());
        response.setCareLevel(elder.getCareLevel());
      }
      return response;
    }).toList();
  }

  @Override
  public void delete(Long id, Long tenantId) {
    Bed bed = bedMapper.selectById(id);
    if (bed != null && (tenantId == null || tenantId.equals(bed.getTenantId()))) {
      bed.setIsDeleted(1);
      bedMapper.updateById(bed);
    }
  }

  private BedResponse toResponse(Bed bed) {
    BedResponse response = new BedResponse();
    response.setId(bed.getId());
    response.setTenantId(bed.getTenantId());
    response.setOrgId(bed.getOrgId());
    response.setRoomId(bed.getRoomId());
    response.setBedNo(bed.getBedNo());
    response.setBedQrCode(bed.getBedQrCode());
    response.setStatus(bed.getStatus());
    response.setElderId(bed.getElderId());
    return response;
  }

  private void ensureRoomTenant(Long tenantId, Long roomId) {
    if (tenantId == null || roomId == null) {
      return;
    }
    Room room = roomMapper.selectById(roomId);
    if (room == null || !tenantId.equals(room.getTenantId())) {
      throw new IllegalArgumentException("房间不存在或无权限");
    }
  }
}
