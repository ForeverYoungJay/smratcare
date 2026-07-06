package com.zhiyangyun.care.medins.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.medins.adapter.MedinsChannelAdapter;
import com.zhiyangyun.care.medins.adapter.MedinsChannelResult;
import com.zhiyangyun.care.medins.adapter.MedinsMockAdapter;
import com.zhiyangyun.care.medins.entity.MedinsChannelConfig;
import com.zhiyangyun.care.medins.entity.MedinsEvoucher;
import com.zhiyangyun.care.medins.mapper.MedinsEvoucherMapper;
import com.zhiyangyun.care.medins.model.MedinsEvoucherBindRequest;
import com.zhiyangyun.care.medins.service.MedinsChannelService;
import com.zhiyangyun.care.medins.service.MedinsEvoucherService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MedinsEvoucherServiceImpl implements MedinsEvoucherService {

  private static final String BOUND = "BOUND";
  private static final String UNBOUND = "UNBOUND";
  private static final String VERIFY_PENDING = "PENDING";
  private static final String VERIFY_PASSED = "PASSED";
  private static final String VERIFY_FAILED = "FAILED";

  private final MedinsEvoucherMapper evoucherMapper;
  private final MedinsChannelService channelService;
  private final Map<String, MedinsChannelAdapter> adapters;

  public MedinsEvoucherServiceImpl(MedinsEvoucherMapper evoucherMapper,
      MedinsChannelService channelService, List<MedinsChannelAdapter> adapterList) {
    this.evoucherMapper = evoucherMapper;
    this.channelService = channelService;
    this.adapters = adapterList.stream()
        .collect(Collectors.toMap(MedinsChannelAdapter::channel, a -> a));
  }

  @Override
  @Transactional
  public Long bind(MedinsEvoucherBindRequest request) {
    Long orgId = AuthContext.getOrgId();
    MedinsEvoucher existing = evoucherMapper.selectOne(Wrappers.lambdaQuery(MedinsEvoucher.class)
        .eq(MedinsEvoucher::getIsDeleted, 0)
        .eq(orgId != null, MedinsEvoucher::getOrgId, orgId)
        .eq(MedinsEvoucher::getElderId, request.getElderId())
        .last("limit 1"));

    MedinsEvoucher evoucher = existing != null ? existing : new MedinsEvoucher();
    if (existing == null) {
      evoucher.setOrgId(orgId);
      evoucher.setElderId(request.getElderId());
      evoucher.setCreatedBy(AuthContext.getStaffId());
    }
    evoucher.setInsuredNo(request.getInsuredNo());
    evoucher.setIdCard(request.getIdCard());
    evoucher.setEcardToken(request.getEcardToken());
    evoucher.setChannel(StringUtils.hasText(request.getChannel())
        ? request.getChannel() : MedinsMockAdapter.CHANNEL);
    evoucher.setBindStatus(BOUND);
    evoucher.setVerifyStatus(VERIFY_PENDING);
    evoucher.setVerifyMessage(null);
    evoucher.setRemark(request.getRemark());
    if (existing == null) {
      evoucherMapper.insert(evoucher);
    } else {
      evoucherMapper.updateById(evoucher);
    }
    return evoucher.getId();
  }

  @Override
  @Transactional
  public void unbind(Long id) {
    MedinsEvoucher evoucher = loadEvoucher(id);
    evoucher.setBindStatus(UNBOUND);
    evoucherMapper.updateById(evoucher);
  }

  @Override
  @Transactional
  public MedinsEvoucher verify(Long id) {
    MedinsEvoucher evoucher = loadEvoucher(id);
    if (!BOUND.equals(evoucher.getBindStatus())) {
      throw new IllegalStateException("电子凭证未绑定，无法校验");
    }
    String channel = StringUtils.hasText(evoucher.getChannel())
        ? evoucher.getChannel() : MedinsMockAdapter.CHANNEL;
    MedinsChannelAdapter adapter = adapters.get(channel);
    if (adapter == null) {
      throw new IllegalArgumentException("无对应医保渠道适配器: " + channel);
    }
    MedinsChannelConfig config = channelService.resolveChannel(channel, evoucher.getOrgId());
    MedinsChannelResult result;
    try {
      result = adapter.verifyEvoucher(evoucher, config);
    } catch (Exception ex) {
      result = MedinsChannelResult.fail("凭证校验异常: " + ex.getMessage());
    }
    evoucher.setLastVerifyAt(LocalDateTime.now());
    if (result != null && result.isSuccess()) {
      evoucher.setVerifyStatus(VERIFY_PASSED);
      evoucher.setVerifyMessage(result.getReceiptCode());
    } else {
      evoucher.setVerifyStatus(VERIFY_FAILED);
      evoucher.setVerifyMessage(result == null ? "凭证校验失败" : result.getErrorDetail());
    }
    evoucherMapper.updateById(evoucher);
    return evoucher;
  }

  @Override
  public IPage<MedinsEvoucher> page(long pageNo, long pageSize, Long elderId, String bindStatus,
      String verifyStatus) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(MedinsEvoucher.class)
        .eq(MedinsEvoucher::getIsDeleted, 0)
        .eq(orgId != null, MedinsEvoucher::getOrgId, orgId)
        .eq(elderId != null, MedinsEvoucher::getElderId, elderId)
        .eq(StringUtils.hasText(bindStatus), MedinsEvoucher::getBindStatus, bindStatus)
        .eq(StringUtils.hasText(verifyStatus), MedinsEvoucher::getVerifyStatus, verifyStatus)
        .orderByDesc(MedinsEvoucher::getId);
    return evoucherMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
  }

  private MedinsEvoucher loadEvoucher(Long id) {
    MedinsEvoucher evoucher = evoucherMapper.selectById(id);
    if (evoucher == null || Integer.valueOf(1).equals(evoucher.getIsDeleted())) {
      throw new IllegalArgumentException("电子凭证登记不存在: " + id);
    }
    Long orgId = AuthContext.getOrgId();
    if (orgId != null && !orgId.equals(evoucher.getOrgId())) {
      throw new IllegalArgumentException("无权操作其他机构的电子凭证登记");
    }
    return evoucher;
  }
}
