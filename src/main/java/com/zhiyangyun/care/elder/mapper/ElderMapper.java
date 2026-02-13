package com.zhiyangyun.care.elder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ElderMapper extends BaseMapper<ElderProfile> {
  @Select("""
      SELECT MAX(CAST(SUBSTRING(elder_code, 2) AS UNSIGNED))
      FROM elder
      WHERE org_id = #{orgId}
        AND elder_code REGEXP '^E[0-9]+$'
      """)
  Integer selectMaxElderCodeNumber(@Param("orgId") Long orgId);
}
