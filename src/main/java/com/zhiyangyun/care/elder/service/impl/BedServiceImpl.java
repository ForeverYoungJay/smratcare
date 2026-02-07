package com.zhiyangyun.care.elder.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.model.BedRequest;
import com.zhiyangyun.care.elder.model.BedResponse;
import com.zhiyangyun.care.elder.service.BedService;
import com.zhiyangyun.care.elder.util.QrCodeUtil;
import org.springframework.stereotype.Service;

@Service
public class BedServiceImpl implements BedService {
  private final BedMapper bedMapper;

  public BedServiceImpl(BedMapper bedMapper) {
    this.bedMapper = bedMapper;
  }

  @Override
  public BedResponse create(BedRequest request) {
    Bed bed = new Bed();
    bed.setOrgId(request.getOrgId());
    bed.setRoomId(request.getRoomId());
    bed.setBedNo(request.getBedNo());
    bed.setBedQrCode(QrCodeUtil.generate());
    bed.setStatus(request.getStatus());
    bedMapper.insert(bed);
    return toResponse(bed);
  }

  @Override
  public BedResponse update(Long id, BedRequest request) {
    Bed bed = bedMapper.selectById(id);
    if (bed == null) {
      return null;
    }
    bed.setOrgId(request.getOrgId());
    bed.setRoomId(request.getRoomId());
    bed.setBedNo(request.getBedNo());
    bed.setStatus(request.getStatus());
    bedMapper.updateById(bed);
    return toResponse(bed);
  }

  @Override
  public BedResponse get(Long id) {
    Bed bed = bedMapper.selectById(id);
    return bed == null ? null : toResponse(bed);
  }

  @Override
  public IPage<BedResponse> page(Long orgId, long pageNo, long pageSize, String keyword, Integer status) {
    var wrapper = Wrappers.lambdaQuery(Bed.class)
        .eq(Bed::getIsDeleted, 0)
        .eq(orgId != null, Bed::getOrgId, orgId);
    if (status != null) {
      wrapper.eq(Bed::getStatus, status);
    }
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(Bed::getBedNo, keyword)
          .or().like(Bed::getBedQrCode, keyword));
    }
    IPage<Bed> page = bedMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    return page.convert(this::toResponse);
  }

  @Override
  public java.util.List<BedResponse> list(Long orgId) {
    var wrapper = Wrappers.lambdaQuery(Bed.class)
        .eq(Bed::getIsDeleted, 0)
        .eq(orgId != null, Bed::getOrgId, orgId)
        .orderByAsc(Bed::getRoomId)
        .orderByAsc(Bed::getBedNo);
    return bedMapper.selectList(wrapper).stream().map(this::toResponse).toList();
  }

  @Override
  public void delete(Long id) {
    Bed bed = bedMapper.selectById(id);
    if (bed != null) {
      bed.setIsDeleted(1);
      bedMapper.updateById(bed);
    }
  }

  private BedResponse toResponse(Bed bed) {
    BedResponse response = new BedResponse();
    response.setId(bed.getId());
    response.setOrgId(bed.getOrgId());
    response.setRoomId(bed.getRoomId());
    response.setBedNo(bed.getBedNo());
    response.setBedQrCode(bed.getBedQrCode());
    response.setStatus(bed.getStatus());
    response.setElderId(bed.getElderId());
    return response;
  }
}
