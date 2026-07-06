package com.zhiyangyun.care.medins.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.medins.entity.MedinsEvoucher;
import com.zhiyangyun.care.medins.model.MedinsEvoucherBindRequest;

/** 长者医保电子凭证服务。 */
public interface MedinsEvoucherService {

  /** 绑定登记（同一长者重复登记视为更新），返回登记 ID。 */
  Long bind(MedinsEvoucherBindRequest request);

  /** 解绑。 */
  void unbind(Long id);

  /** 调用渠道适配器校验凭证有效性，回写校验状态。 */
  MedinsEvoucher verify(Long id);

  IPage<MedinsEvoucher> page(long pageNo, long pageSize, Long elderId, String bindStatus,
      String verifyStatus);
}
