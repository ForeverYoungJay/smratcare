package com.zhiyangyun.care.report.mapper;

import com.zhiyangyun.care.report.model.FlowReportRow;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface StatisticsFlowReportMapper {
  @Select({
      "<script>",
      "SELECT COUNT(*) FROM elder_admission a",
      "LEFT JOIN elder e ON e.id = a.elder_id AND e.is_deleted = 0",
      "WHERE a.is_deleted = 0",
      "<if test='orgId != null'> AND a.org_id = #{orgId}</if>",
      "<if test='elderId != null'> AND a.elder_id = #{elderId}</if>",
      "AND a.admission_date <![CDATA[>=]]> #{start}",
      "AND a.admission_date <![CDATA[<=]]> #{end}",
      "<if test='keyword != null and keyword != \"\"'>",
      "AND e.full_name LIKE CONCAT('%', #{keyword}, '%')",
      "</if>",
      "</script>"
  })
  long countAdmissions(
      @Param("orgId") Long orgId,
      @Param("start") LocalDate start,
      @Param("end") LocalDate end,
      @Param("elderId") Long elderId,
      @Param("keyword") String keyword);

  @Select({
      "<script>",
      "SELECT COUNT(*) FROM elder_discharge d",
      "LEFT JOIN elder e ON e.id = d.elder_id AND e.is_deleted = 0",
      "WHERE d.is_deleted = 0",
      "<if test='orgId != null'> AND d.org_id = #{orgId}</if>",
      "<if test='elderId != null'> AND d.elder_id = #{elderId}</if>",
      "AND d.discharge_date <![CDATA[>=]]> #{start}",
      "AND d.discharge_date <![CDATA[<=]]> #{end}",
      "<if test='keyword != null and keyword != \"\"'>",
      "AND e.full_name LIKE CONCAT('%', #{keyword}, '%')",
      "</if>",
      "</script>"
  })
  long countDischarges(
      @Param("orgId") Long orgId,
      @Param("start") LocalDate start,
      @Param("end") LocalDate end,
      @Param("elderId") Long elderId,
      @Param("keyword") String keyword);

  @Select({
      "<script>",
      "SELECT event_type AS eventType, event_date AS eventDate, elder_id AS elderId, elder_name AS elderName, remark",
      "FROM (",
      "  SELECT 'ADMISSION' AS event_type,",
      "         a.admission_date AS event_date,",
      "         a.elder_id AS elder_id,",
      "         e.full_name AS elder_name,",
      "         a.remark AS remark",
      "  FROM elder_admission a",
      "  LEFT JOIN elder e ON e.id = a.elder_id AND e.is_deleted = 0",
      "  WHERE a.is_deleted = 0",
      "  <if test='eventType == null or eventType == \"ADMISSION\"'>",
      "    <if test='orgId != null'> AND a.org_id = #{orgId}</if>",
      "    <if test='elderId != null'> AND a.elder_id = #{elderId}</if>",
      "    AND a.admission_date <![CDATA[>=]]> #{start}",
      "    AND a.admission_date <![CDATA[<=]]> #{end}",
      "    <if test='keyword != null and keyword != \"\"'>",
      "    AND e.full_name LIKE CONCAT('%', #{keyword}, '%')",
      "    </if>",
      "  </if>",
      "  <if test='eventType != null and eventType == \"DISCHARGE\"'>",
      "    AND 1 = 0",
      "  </if>",
      "  UNION ALL",
      "  SELECT 'DISCHARGE' AS event_type,",
      "         d.discharge_date AS event_date,",
      "         d.elder_id AS elder_id,",
      "         e.full_name AS elder_name,",
      "         COALESCE(d.reason, d.remark) AS remark",
      "  FROM elder_discharge d",
      "  LEFT JOIN elder e ON e.id = d.elder_id AND e.is_deleted = 0",
      "  WHERE d.is_deleted = 0",
      "  <if test='eventType == null or eventType == \"DISCHARGE\"'>",
      "    <if test='orgId != null'> AND d.org_id = #{orgId}</if>",
      "    <if test='elderId != null'> AND d.elder_id = #{elderId}</if>",
      "    AND d.discharge_date <![CDATA[>=]]> #{start}",
      "    AND d.discharge_date <![CDATA[<=]]> #{end}",
      "    <if test='keyword != null and keyword != \"\"'>",
      "    AND e.full_name LIKE CONCAT('%', #{keyword}, '%')",
      "    </if>",
      "  </if>",
      "  <if test='eventType != null and eventType == \"ADMISSION\"'>",
      "    AND 1 = 0",
      "  </if>",
      ") flow_rows",
      "ORDER BY event_date DESC, elder_id DESC",
      "<if test='limit != null and offset != null'>",
      "LIMIT #{limit} OFFSET #{offset}",
      "</if>",
      "</script>"
  })
  List<FlowReportRow> selectFlowRows(
      @Param("orgId") Long orgId,
      @Param("start") LocalDate start,
      @Param("end") LocalDate end,
      @Param("eventType") String eventType,
      @Param("elderId") Long elderId,
      @Param("keyword") String keyword,
      @Param("offset") Long offset,
      @Param("limit") Long limit);
}
