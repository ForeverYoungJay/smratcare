package com.zhiyangyun.care.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhiyangyun.care.auth.entity.Role;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface RoleMapper extends BaseMapper<Role> {
  @Select("SELECT r.role_code FROM role r "
      + "JOIN staff_role sr ON r.id = sr.role_id "
      + "WHERE sr.staff_id = #{staffId} AND sr.org_id = #{orgId} "
      + "AND r.is_deleted = 0 AND sr.is_deleted = 0")
  List<String> selectRoleCodesByStaff(@Param("staffId") Long staffId, @Param("orgId") Long orgId);
}
