package com.zhiyangyun.care.fire.service;

import com.zhiyangyun.care.fire.entity.FireDutyHandover;
import com.zhiyangyun.care.fire.entity.FireLocation;
import com.zhiyangyun.care.fire.entity.FireReportSignature;
import com.zhiyangyun.care.fire.entity.FireSafetyRecord;
import com.zhiyangyun.care.fire.entity.FireVideoAccessRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 消防业务规则：交接双方确认、报告签署、录像调阅审批、楼栋风险和隐患统计。
 * 只做判断不读写库，控制器负责取数和保存，便于单独测试。
 */
public final class FireRules {
  /** 批准后调阅有效期 */
  public static final int VIDEO_ACCESS_VALID_DAYS = 7;

  private FireRules() {}

  // ---------- 控制室交接 ----------

  /** 交班人发起交接，接班人必须是另一个人 */
  public static void requireDifferentTakeover(Long handoverStaffId, Long takeoverStaffId) {
    if (takeoverStaffId == null) throw new IllegalArgumentException("请选择接班人");
    if (Objects.equals(handoverStaffId, takeoverStaffId)) throw new IllegalArgumentException("交班人和接班人不能是同一人");
  }

  /** 只有指定的接班人本人才能确认或退回，确认后不可再改 */
  public static void confirmHandover(FireDutyHandover handover, Long staffId, boolean accept, String note, LocalDateTime now) {
    if (!"PENDING".equals(handover.getStatus())) throw new IllegalArgumentException("该交接已处理，不能重复确认");
    if (!Objects.equals(handover.getTakeoverStaffId(), staffId)) throw new IllegalArgumentException("只有指定的接班人本人可以确认交接");
    if (!accept && (note == null || note.isBlank())) throw new IllegalArgumentException("退回交接时请写明原因");
    handover.setStatus(accept ? "CONFIRMED" : "REJECTED");
    handover.setTakeoverAt(now);
    handover.setTakeoverNote(note);
  }

  // ---------- 维保报告签署 ----------

  /** 先提交签署，再由另一位主管审核签署；同一报告只能审核一次 */
  public static void requireSignAllowed(List<FireReportSignature> existing, String signType, Long signerId, boolean manager) {
    boolean submitted = existing.stream().anyMatch(s -> "SUBMIT".equals(s.getSignType()));
    if ("SUBMIT".equals(signType)) {
      if (submitted) throw new IllegalArgumentException("该报告已提交签署");
      return;
    }
    if (!"REVIEW".equals(signType)) throw new IllegalArgumentException("签署类型只支持 SUBMIT/REVIEW");
    if (!manager) throw new IllegalArgumentException("审核签署需要消防主管或管理层");
    if (!submitted) throw new IllegalArgumentException("报告尚未提交签署，不能审核");
    if (existing.stream().anyMatch(s -> "REVIEW".equals(s.getSignType()))) throw new IllegalArgumentException("该报告已审核签署");
    boolean selfSubmitted = existing.stream().anyMatch(s -> "SUBMIT".equals(s.getSignType()) && Objects.equals(s.getSignerId(), signerId));
    if (selfSubmitted) throw new IllegalArgumentException("提交人不能审核自己提交的报告");
  }

  // ---------- 录像调阅 ----------

  public static void requireValidVideoRequest(LocalDateTime start, LocalDateTime end, String reason, LocalDateTime now) {
    if (start == null || end == null || !start.isBefore(end)) throw new IllegalArgumentException("请填写正确的调阅时间段");
    if (end.isAfter(now.plusMinutes(5))) throw new IllegalArgumentException("调阅时间段不能晚于当前时间");
    if (end.minusDays(1).isAfter(start)) throw new IllegalArgumentException("单次调阅不超过 24 小时，请缩小时间段");
    if (reason == null || reason.trim().length() < 4) throw new IllegalArgumentException("请写明调阅事由");
  }

  /** 审批人不能是申请人本人；批准后在有效期内可查看 */
  public static void decideVideoRequest(FireVideoAccessRequest request, Long approverId, boolean approve, String note, LocalDateTime now) {
    if (!"PENDING".equals(request.getStatus())) throw new IllegalArgumentException("该申请已处理");
    if (Objects.equals(request.getApplicantId(), approverId)) throw new IllegalArgumentException("不能审批自己提交的调阅申请");
    if (!approve && (note == null || note.isBlank())) throw new IllegalArgumentException("驳回时请写明原因");
    request.setStatus(approve ? "APPROVED" : "REJECTED");
    request.setApproverId(approverId);
    request.setDecidedAt(now);
    request.setDecisionNote(note);
    request.setExpiresAt(approve ? now.plusDays(VIDEO_ACCESS_VALID_DAYS) : null);
  }

  /** 只有申请人本人、在批准有效期内可以查看 */
  public static void requireCanView(FireVideoAccessRequest request, Long staffId, LocalDateTime now) {
    if (!"APPROVED".equals(request.getStatus())) throw new IllegalArgumentException("调阅申请尚未批准");
    if (!Objects.equals(request.getApplicantId(), staffId)) throw new IllegalArgumentException("只有申请人本人可以查看批准的录像");
    if (request.getExpiresAt() == null || now.isAfter(request.getExpiresAt())) throw new IllegalArgumentException("调阅授权已过期，请重新申请");
  }

  /** 录像必须是申请的摄像头，且与申请时间段有重叠 */
  public static boolean recordingCovered(FireVideoAccessRequest request, Long cameraId, LocalDateTime startedAt, LocalDateTime endedAt) {
    return Objects.equals(request.getCameraId(), cameraId) && startedAt != null && endedAt != null
        && startedAt.isBefore(request.getRangeEnd()) && endedAt.isAfter(request.getRangeStart());
  }

  // ---------- 隐患统计与楼栋风险 ----------

  public static boolean isOverdue(FireSafetyRecord record, LocalDate today) {
    return record.getNextCheckDate() != null && record.getNextCheckDate().isBefore(today);
  }

  /** 隐患汇总：传入未关闭且登记了问题的记录 */
  public static Map<String, Long> hazardStats(List<FireSafetyRecord> activeIssues, LocalDate today) {
    Map<String, Long> stats = new LinkedHashMap<>();
    stats.put("total", (long) activeIssues.size());
    stats.put("overdue", activeIssues.stream().filter(r -> isOverdue(r, today)).count());
    stats.put("running", activeIssues.stream().filter(r -> "RUNNING".equals(r.getStatus())).count());
    stats.put("faults", activeIssues.stream().filter(r -> "FAULT_MAINTENANCE".equals(r.getRecordType())).count());
    return stats;
  }

  public record BuildingRisk(String building, int points, int keyParts, long openHazards, long overdue, long faults, String level) {}

  /**
   * 按楼栋汇总：点位来自楼栋点位配置；隐患按记录的位置文字包含楼栋名归属（记录还没有点位外键，
   * 位置写得不规范时会统计不到）。有逾期为 DANGER，有未关闭隐患为 WARNING，否则 OK。
   */
  public static List<BuildingRisk> buildingRisk(List<FireLocation> locations, List<FireSafetyRecord> activeIssues, LocalDate today) {
    Map<String, List<FireLocation>> byBuilding = new LinkedHashMap<>();
    for (FireLocation location : locations) {
      if (location.getBuilding() == null || location.getBuilding().isBlank()) continue;
      byBuilding.computeIfAbsent(location.getBuilding().trim(), key -> new ArrayList<>()).add(location);
    }
    List<BuildingRisk> result = new ArrayList<>();
    byBuilding.forEach((building, points) -> {
      List<FireSafetyRecord> mine = activeIssues.stream()
          .filter(r -> r.getLocation() != null && r.getLocation().contains(building)).toList();
      long overdue = mine.stream().filter(r -> isOverdue(r, today)).count();
      long faults = mine.stream().filter(r -> "FAULT_MAINTENANCE".equals(r.getRecordType())).count();
      int keyParts = (int) points.stream().filter(p -> Integer.valueOf(1).equals(p.getKeyPart())).count();
      String level = overdue > 0 ? "DANGER" : mine.isEmpty() ? "OK" : "WARNING";
      result.add(new BuildingRisk(building, points.size(), keyParts, mine.size(), overdue, faults, level));
    });
    return result;
  }
}
