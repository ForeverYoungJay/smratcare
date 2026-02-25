package com.zhiyangyun.care.nursing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhiyangyun.care.nursing.entity.CaregiverGroupMember;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface CaregiverGroupMemberMapper extends BaseMapper<CaregiverGroupMember> {
  @Select({
      "<script>",
      "select * from caregiver_group_member where is_deleted = 0 and group_id in",
      "<foreach collection='groupIds' item='item' open='(' separator=',' close=')'>",
      "#{item}",
      "</foreach>",
      "</script>"
  })
  List<CaregiverGroupMember> selectByGroupIds(@Param("groupIds") List<Long> groupIds);

  @Update("update caregiver_group_member set is_deleted = 1 where group_id = #{groupId} and is_deleted = 0")
  int softDeleteByGroupId(@Param("groupId") Long groupId);
}
