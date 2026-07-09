package com.zhiyangyun.care.operations.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderBedRelation;
import com.zhiyangyun.care.elder.entity.ElderFamily;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.FamilyUser;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderBedRelationMapper;
import com.zhiyangyun.care.elder.mapper.ElderFamilyMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.FamilyUserMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.health.entity.HealthArchive;
import com.zhiyangyun.care.health.entity.HealthDataRecord;
import com.zhiyangyun.care.health.mapper.HealthArchiveMapper;
import com.zhiyangyun.care.health.mapper.HealthDataRecordMapper;
import com.zhiyangyun.care.hr.entity.StaffRewardPunishment;
import com.zhiyangyun.care.hr.mapper.StaffRewardPunishmentMapper;
import com.zhiyangyun.care.medorder.entity.MedicalOrder;
import com.zhiyangyun.care.medorder.mapper.MedicalOrderMapper;
import com.zhiyangyun.care.operations.entity.StaffMobileTaskReceipt;
import com.zhiyangyun.care.operations.mapper.StaffMobileTaskReceiptMapper;
import com.zhiyangyun.care.schedule.entity.AttendanceRecord;
import com.zhiyangyun.care.schedule.mapper.AttendanceRecordMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 员工端移动扩展能力：
 * 1) 床边档案速查——扫腕带/床码或按姓名搜索，返回护理现场最需要的老人关键信息卡；
 * 2) 我的工作量统计——本月任务回执、体征录入、出勤与奖惩摘要，供员工自查。
 * 只做薄查询，复用既有表结构，无新增 DDL。
 */
@RestController
@RequestMapping("/api/operations/staff-mobile")
@RequiredArgsConstructor
public class StaffMobileExtrasController {

  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private final ElderMapper elderProfileMapper;
  private final ElderBedRelationMapper elderBedRelationMapper;
  private final BedMapper bedMapper;
  private final RoomMapper roomMapper;
  private final ElderFamilyMapper elderFamilyMapper;
  private final FamilyUserMapper familyUserMapper;
  private final HealthArchiveMapper healthArchiveMapper;
  private final HealthDataRecordMapper healthDataRecordMapper;
  private final MedicalOrderMapper medicalOrderMapper;
  private final StaffMobileTaskReceiptMapper staffMobileTaskReceiptMapper;
  private final AttendanceRecordMapper attendanceRecordMapper;
  private final StaffRewardPunishmentMapper staffRewardPunishmentMapper;

  // ---------- 床边档案速查 ----------

  @GetMapping("/elder-card")
  public Result<Map<String, Object>> elderCard(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Long elderId) {
    Long orgId = AuthContext.getOrgId();
    Map<String, Object> payload = new LinkedHashMap<>();

    ElderProfile target = null;
    List<ElderProfile> matches = new ArrayList<>();
    if (elderId != null) {
      ElderProfile profile = elderProfileMapper.selectById(elderId);
      if (profile != null && Objects.equals(profile.getOrgId(), orgId)
          && Integer.valueOf(0).equals(profile.getIsDeleted())) {
        target = profile;
      }
    } else {
      String text = keyword == null ? "" : keyword.trim();
      if (text.isEmpty()) {
        payload.put("matches", List.of());
        return Result.ok(payload);
      }
      matches = searchElders(orgId, text);
      if (matches.size() == 1) {
        target = matches.get(0);
      }
    }

    List<Map<String, Object>> briefs = new ArrayList<>();
    for (ElderProfile profile : matches) {
      Map<String, Object> brief = new LinkedHashMap<>();
      brief.put("elderId", String.valueOf(profile.getId()));
      brief.put("name", profile.getFullName());
      brief.put("careLevel", defaultText(profile.getCareLevel(), "未评估"));
      brief.put("roomText", resolveRoomText(orgId, profile));
      briefs.add(brief);
    }
    payload.put("matches", briefs);
    if (target != null) {
      payload.put("card", buildElderCard(orgId, target));
    }
    return Result.ok(payload);
  }

  private List<ElderProfile> searchElders(Long orgId, String text) {
    // 扫码值优先精确匹配老人码/腕带码，其次匹配床位二维码，最后按姓名模糊
    List<ElderProfile> exact = elderProfileMapper.selectList(
        Wrappers.lambdaQuery(ElderProfile.class)
            .eq(ElderProfile::getIsDeleted, 0)
            .eq(ElderProfile::getOrgId, orgId)
            .and(w -> w.eq(ElderProfile::getElderCode, text).or().eq(ElderProfile::getElderQrCode, text))
            .last("LIMIT 3"));
    if (!exact.isEmpty()) {
      return exact;
    }
    Bed bed = bedMapper.selectOne(
        Wrappers.lambdaQuery(Bed.class)
            .eq(Bed::getIsDeleted, 0)
            .eq(Bed::getOrgId, orgId)
            .eq(Bed::getBedQrCode, text)
            .last("LIMIT 1"));
    if (bed != null) {
      ElderBedRelation relation = elderBedRelationMapper.selectOne(
          Wrappers.lambdaQuery(ElderBedRelation.class)
              .eq(ElderBedRelation::getIsDeleted, 0)
              .eq(ElderBedRelation::getOrgId, orgId)
              .eq(ElderBedRelation::getBedId, bed.getId())
              .eq(ElderBedRelation::getActiveFlag, 1)
              .last("LIMIT 1"));
      if (relation != null) {
        ElderProfile profile = elderProfileMapper.selectById(relation.getElderId());
        if (profile != null && Integer.valueOf(0).equals(profile.getIsDeleted())) {
          return List.of(profile);
        }
      }
      return List.of();
    }
    return elderProfileMapper.selectList(
        Wrappers.lambdaQuery(ElderProfile.class)
            .eq(ElderProfile::getIsDeleted, 0)
            .eq(ElderProfile::getOrgId, orgId)
            .like(ElderProfile::getFullName, text)
            .orderByDesc(ElderProfile::getUpdateTime)
            .last("LIMIT 8"));
  }

  private Map<String, Object> buildElderCard(Long orgId, ElderProfile profile) {
    Map<String, Object> card = new LinkedHashMap<>();
    card.put("elderId", String.valueOf(profile.getId()));
    card.put("name", profile.getFullName());
    card.put("gender", Integer.valueOf(1).equals(profile.getGender()) ? "男" : "女");
    card.put("age", profile.getBirthDate() == null
        ? "--" : String.valueOf(Period.between(profile.getBirthDate(), LocalDate.now()).getYears()));
    card.put("careLevel", defaultText(profile.getCareLevel(), "未评估"));
    card.put("riskFlags", defaultText(profile.getRiskPrecommit(), "无特殊风险标记"));
    card.put("roomText", resolveRoomText(orgId, profile));
    card.put("admissionDate", profile.getAdmissionDate() == null ? "--" : profile.getAdmissionDate().format(DATE_FMT));

    HealthArchive archive = healthArchiveMapper.selectOne(
        Wrappers.lambdaQuery(HealthArchive.class)
            .eq(HealthArchive::getIsDeleted, 0)
            .eq(HealthArchive::getOrgId, orgId)
            .eq(HealthArchive::getElderId, profile.getId())
            .orderByDesc(HealthArchive::getUpdateTime)
            .last("LIMIT 1"));
    card.put("bloodType", archive == null ? "--" : defaultText(archive.getBloodType(), "--"));
    card.put("allergyHistory", archive == null ? "暂无档案记录" : defaultText(archive.getAllergyHistory(), "无已知过敏"));
    card.put("chronicDisease", archive == null ? "暂无档案记录" : defaultText(archive.getChronicDisease(), "无慢性病记录"));

    String contactName = archive == null ? "" : defaultText(archive.getEmergencyContact(), "");
    String contactPhone = archive == null ? "" : defaultText(archive.getEmergencyPhone(), "");
    if (contactPhone.isEmpty()) {
      ElderFamily relation = elderFamilyMapper.selectOne(
          Wrappers.lambdaQuery(ElderFamily.class)
              .eq(ElderFamily::getIsDeleted, 0)
              .eq(ElderFamily::getOrgId, orgId)
              .eq(ElderFamily::getElderId, profile.getId())
              .orderByDesc(ElderFamily::getIsPrimary)
              .last("LIMIT 1"));
      if (relation != null) {
        FamilyUser familyUser = familyUserMapper.selectById(relation.getFamilyUserId());
        if (familyUser != null) {
          contactName = defaultText(familyUser.getRealName(), "") + "（" + defaultText(relation.getRelation(), "家属") + "）";
          contactPhone = defaultText(familyUser.getPhone(), "");
        }
      }
    }
    card.put("emergencyContact", contactName.isEmpty() ? "未登记" : contactName);
    card.put("emergencyPhone", contactPhone);

    List<MedicalOrder> orders = medicalOrderMapper.selectList(
        Wrappers.lambdaQuery(MedicalOrder.class)
            .eq(MedicalOrder::getIsDeleted, 0)
            .eq(MedicalOrder::getOrgId, orgId)
            .eq(MedicalOrder::getElderId, profile.getId())
            .eq(MedicalOrder::getStatus, "ACTIVE")
            .orderByDesc(MedicalOrder::getStartTime)
            .last("LIMIT 10"));
    List<Map<String, Object>> orderViews = new ArrayList<>();
    for (MedicalOrder order : orders) {
      Map<String, Object> view = new LinkedHashMap<>();
      view.put("content", defaultText(order.getContent(), "医嘱内容未填写"));
      view.put("usageText", Stream.of(order.getDosage(), order.getFrequency(), order.getRoute())
          .filter(part -> part != null && !part.isBlank())
          .reduce((a, b) -> a + " · " + b).orElse("按医嘱执行"));
      view.put("doctorName", defaultText(order.getDoctorName(), "值班医生"));
      orderViews.add(view);
    }
    card.put("activeOrders", orderViews);
    return card;
  }

  private String resolveRoomText(Long orgId, ElderProfile profile) {
    Long bedId = profile.getBedId();
    if (bedId == null) {
      ElderBedRelation relation = elderBedRelationMapper.selectOne(
          Wrappers.lambdaQuery(ElderBedRelation.class)
              .eq(ElderBedRelation::getIsDeleted, 0)
              .eq(ElderBedRelation::getOrgId, orgId)
              .eq(ElderBedRelation::getElderId, profile.getId())
              .eq(ElderBedRelation::getActiveFlag, 1)
              .last("LIMIT 1"));
      bedId = relation == null ? null : relation.getBedId();
    }
    if (bedId == null) {
      return "床位未分配";
    }
    Bed bed = bedMapper.selectById(bedId);
    if (bed == null) {
      return "床位未分配";
    }
    Room room = bed.getRoomId() == null ? null : roomMapper.selectById(bed.getRoomId());
    return Stream.of(
            room == null ? null : room.getBuilding(),
            room == null ? null : room.getFloorNo(),
            room == null ? null : room.getRoomNo(),
            bed.getBedNo())
        .filter(part -> part != null && !part.isBlank())
        .reduce((a, b) -> a + " · " + b).orElse("床位未分配");
  }

  // ---------- 我的工作量统计 ----------

  @GetMapping("/my-stats")
  public Result<Map<String, Object>> myStats() {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    LocalDate today = LocalDate.now();
    LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();

    long taskCount = staffMobileTaskReceiptMapper.selectCount(
        Wrappers.lambdaQuery(StaffMobileTaskReceipt.class)
            .eq(StaffMobileTaskReceipt::getIsDeleted, 0)
            .eq(StaffMobileTaskReceipt::getOrgId, orgId)
            .eq(StaffMobileTaskReceipt::getStaffId, staffId)
            .ge(StaffMobileTaskReceipt::getReceiptTime, monthStart));

    long vitalsCount = healthDataRecordMapper.selectCount(
        Wrappers.lambdaQuery(HealthDataRecord.class)
            .eq(HealthDataRecord::getIsDeleted, 0)
            .eq(HealthDataRecord::getOrgId, orgId)
            .eq(HealthDataRecord::getCreatedBy, staffId)
            .ge(HealthDataRecord::getCreateTime, monthStart));

    List<AttendanceRecord> attendance = attendanceRecordMapper.selectList(
        Wrappers.lambdaQuery(AttendanceRecord.class)
            .eq(AttendanceRecord::getIsDeleted, 0)
            .eq(AttendanceRecord::getOrgId, orgId)
            .eq(AttendanceRecord::getStaffId, staffId)
            .ge(AttendanceRecord::getWorkDate, today.withDayOfMonth(1)));
    long attendanceDays = attendance.stream().filter(item -> item.getCheckInTime() != null).count();
    String avgCheckIn = attendance.stream()
        .map(AttendanceRecord::getCheckInTime)
        .filter(Objects::nonNull)
        .mapToInt(time -> time.getHour() * 60 + time.getMinute())
        .average()
        .stream()
        .mapToObj(avg -> String.format("%02d:%02d", (int) avg / 60, (int) avg % 60))
        .findFirst().orElse("--");

    List<StaffRewardPunishment> records = staffRewardPunishmentMapper.selectList(
        Wrappers.lambdaQuery(StaffRewardPunishment.class)
            .eq(StaffRewardPunishment::getIsDeleted, 0)
            .eq(StaffRewardPunishment::getOrgId, orgId)
            .eq(StaffRewardPunishment::getStaffId, staffId)
            .orderByDesc(StaffRewardPunishment::getOccurredDate)
            .last("LIMIT 5"));
    long rewardCount = records.stream().filter(item -> "REWARD".equalsIgnoreCase(item.getType())).count();
    List<Map<String, Object>> recordViews = new ArrayList<>();
    for (StaffRewardPunishment record : records) {
      Map<String, Object> view = new LinkedHashMap<>();
      view.put("typeLabel", "REWARD".equalsIgnoreCase(record.getType()) ? "表扬" : "整改");
      view.put("title", defaultText(record.getTitle(), defaultText(record.getReason(), "记录")));
      view.put("date", record.getOccurredDate() == null ? "--" : record.getOccurredDate().format(DATE_FMT));
      recordViews.add(view);
    }

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("month", today.format(DateTimeFormatter.ofPattern("yyyy-MM")));
    payload.put("taskCount", taskCount);
    payload.put("vitalsCount", vitalsCount);
    payload.put("attendanceDays", attendanceDays);
    payload.put("avgCheckIn", avgCheckIn);
    payload.put("rewardCount", rewardCount);
    payload.put("recentRecords", recordViews);
    return Result.ok(payload);
  }

  private String defaultText(String value, String fallback) {
    return value == null || value.isBlank() ? fallback : value;
  }
}
