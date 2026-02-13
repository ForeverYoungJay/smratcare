package com.zhiyangyun.care.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface StaffMapper extends BaseMapper<StaffAccount> {
  @Select("SELECT s.* FROM staff s "
      + "JOIN staff_role sr ON s.id = sr.staff_id "
      + "JOIN role r ON r.id = sr.role_id "
      + "WHERE sr.org_id = #{orgId} AND r.role_code = #{roleCode} "
      + "AND s.is_deleted = 0 AND sr.is_deleted = 0 AND r.is_deleted = 0")
  List<StaffAccount> selectByRoleCode(@Param("orgId") Long orgId, @Param("roleCode") String roleCode);
}
