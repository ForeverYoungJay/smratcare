package com.zhiyangyun.care.compliance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhiyangyun.care.compliance.entity.ExportLog;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ExportLogMapper extends BaseMapper<ExportLog> {

  /** 留存清理：按机构物理删除超期日志。 */
  @Delete("DELETE FROM compliance_export_log WHERE org_id = #{orgId} AND create_time < #{before}")
  int deleteByOrgBefore(@Param("orgId") Long orgId, @Param("before") LocalDateTime before);

  /** 留存清理：按默认留存物理删除超期日志（排除已单独配置策略的机构）。 */
  @Delete("<script>DELETE FROM compliance_export_log WHERE create_time &lt; #{before}"
      + "<if test='excludeOrgIds != null and excludeOrgIds.size() > 0'>"
      + " AND (org_id IS NULL OR org_id NOT IN "
      + "<foreach collection='excludeOrgIds' item='orgId' open='(' separator=',' close=')'>#{orgId}</foreach>)"
      + "</if>"
      + "</script>")
  int deleteBeforeExcludingOrgs(@Param("before") LocalDateTime before, @Param("excludeOrgIds") List<Long> excludeOrgIds);
}
