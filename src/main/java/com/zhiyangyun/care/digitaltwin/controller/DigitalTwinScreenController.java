package com.zhiyangyun.care.digitaltwin.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.entity.CareTaskDaily;
import com.zhiyangyun.care.finance.entity.FinanceElectricityReading;
import com.zhiyangyun.care.finance.mapper.FinanceElectricityReadingMapper;
import com.zhiyangyun.care.fire.entity.FireSafetyRecord;
import com.zhiyangyun.care.fire.mapper.FireSafetyRecordMapper;
import com.zhiyangyun.care.health.entity.HealthDataRecord;
import com.zhiyangyun.care.health.mapper.HealthDataRecordMapper;
import com.zhiyangyun.care.life.entity.MaintenanceRequest;
import com.zhiyangyun.care.life.mapper.MaintenanceRequestMapper;
import com.zhiyangyun.care.logistics.entity.LogisticsCamera;
import com.zhiyangyun.care.logistics.mapper.LogisticsCameraMapper;
import com.zhiyangyun.care.mapper.CareTaskDailyMapper;
import com.zhiyangyun.care.medicalcare.entity.MedicalEmergencyEvent;
import com.zhiyangyun.care.medicalcare.mapper.MedicalEmergencyEventMapper;
import com.zhiyangyun.care.nursing.entity.CareLevel;
import com.zhiyangyun.care.nursing.mapper.CareLevelMapper;
import com.zhiyangyun.care.smart.entity.SmartAlert;
import com.zhiyangyun.care.smart.mapper.SmartAlertMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数字孪生运营大屏（院长、管理员）：只读汇聚全院业务表，不另存一份孪生数据。
 * 每块都带 source；没有数据源的块（人员定位、视频流、水/温度）由前端显示「未接入」，这里不造数。
 * 床位、在住、收费、人员等全院 KPI 直接复用经营驾驶舱 /api/stats/executive/overview，不在这里重算。
 */
@RestController
@RequestMapping("/api/digital-twin/screen")
@PreAuthorize("hasAnyRole('DIRECTOR','ADMIN')")
public class DigitalTwinScreenController {
  private static final List<String> OPEN_ALERT = List.of("OPEN", "ACKNOWLEDGED");
  private static final List<String> OPEN_EMERGENCY = List.of("INITIATED", "CALLED", "TRANSFERRED");
  private static final List<String> OPEN_MAINTENANCE = List.of("OPEN", "PROCESSING");

  private final ElderMapper elderMapper;
  private final CareTaskDailyMapper careTaskMapper;
  private final SmartAlertMapper smartAlertMapper;
  private final FireSafetyRecordMapper fireRecordMapper;
  private final MaintenanceRequestMapper maintenanceMapper;
  private final MedicalEmergencyEventMapper emergencyMapper;
  private final HealthDataRecordMapper healthMapper;
  private final FinanceElectricityReadingMapper electricityMapper;
  private final LogisticsCameraMapper cameraMapper;
  private final CareLevelMapper careLevelMapper;

  public DigitalTwinScreenController(ElderMapper elderMapper, CareTaskDailyMapper careTaskMapper,
      SmartAlertMapper smartAlertMapper, FireSafetyRecordMapper fireRecordMapper,
      MaintenanceRequestMapper maintenanceMapper, MedicalEmergencyEventMapper emergencyMapper,
      HealthDataRecordMapper healthMapper, FinanceElectricityReadingMapper electricityMapper,
      LogisticsCameraMapper cameraMapper, CareLevelMapper careLevelMapper) {
    this.careLevelMapper = careLevelMapper;
    this.elderMapper = elderMapper;
    this.careTaskMapper = careTaskMapper;
    this.smartAlertMapper = smartAlertMapper;
    this.fireRecordMapper = fireRecordMapper;
    this.maintenanceMapper = maintenanceMapper;
    this.emergencyMapper = emergencyMapper;
    this.healthMapper = healthMapper;
    this.electricityMapper = electricityMapper;
    this.cameraMapper = cameraMapper;
  }

  @GetMapping("/overview")
  public Result<Map<String, Object>> overview() {
    Long orgId = orgId();
    LocalDate today = LocalDate.now();
    LocalDateTime dayStart = today.atStartOfDay();
    LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("careLevels", careLevels(orgId));
    out.put("alerts", alerts(orgId));
    out.put("tasks", tasks(orgId, today, dayStart));
    out.put("workOrders", workOrders(orgId, monthStart));
    out.put("health", health(orgId, dayStart));
    out.put("energy", energy(orgId, today));
    out.put("cameras", cameras(orgId));
    out.put("updatedAt", LocalDateTime.now());
    return Result.ok(out);
  }

  /** 在住长者按护理等级分布（未填写的单列「未评定」；存的是等级编码时按护理等级字典换成名称，字典里没有就原样显示） */
  private Map<String, Object> careLevels(Long orgId) {
    Map<String, String> names = careLevelMapper.selectList(Wrappers.lambdaQuery(CareLevel.class)
            .eq(CareLevel::getOrgId, orgId).eq(CareLevel::getIsDeleted, 0))
        .stream().filter(l -> l.getLevelCode() != null && l.getLevelName() != null)
        .collect(Collectors.toMap(CareLevel::getLevelCode, CareLevel::getLevelName, (a, b) -> a));
    List<ElderProfile> elders = elderMapper.selectList(Wrappers.lambdaQuery(ElderProfile.class)
        .select(ElderProfile::getId, ElderProfile::getCareLevel)
        .eq(ElderProfile::getOrgId, orgId).eq(ElderProfile::getIsDeleted, 0).eq(ElderProfile::getStatus, 1));
    Map<String, Long> counts = elders.stream().collect(Collectors.groupingBy(
        e -> e.getCareLevel() == null || e.getCareLevel().isBlank() ? "未评定"
            : names.getOrDefault(e.getCareLevel().trim(), e.getCareLevel().trim()),
        TreeMap::new, Collectors.counting()));
    List<Map<String, Object>> items = counts.entrySet().stream()
        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
        .map(e -> row("name", e.getKey(), "count", e.getValue())).toList();
    return block("长者档案（在住）", "/elder/list", "total", elders.size(), "items", items);
  }

  /** 实时预警：设备/健康告警、消防未闭环、高优先级报修、进行中的急救，按时间倒序取最近 8 条 */
  private Map<String, Object> alerts(Long orgId) {
    List<Map<String, Object>> items = new ArrayList<>();
    smartAlertMapper.selectList(Wrappers.lambdaQuery(SmartAlert.class)
            .eq(SmartAlert::getOrgId, orgId).eq(SmartAlert::getIsDeleted, 0).in(SmartAlert::getStatus, OPEN_ALERT)
            .orderByDesc(SmartAlert::getLatestTriggeredAt).last("LIMIT 8"))
        .forEach(a -> items.add(alert("DEVICE", "设备告警", a.getTitle(), a.getLocation(), a.getLatestTriggeredAt(), "/medical-care/smart-alerts")));
    fireRecordMapper.selectList(Wrappers.lambdaQuery(FireSafetyRecord.class)
            .eq(FireSafetyRecord::getOrgId, orgId).eq(FireSafetyRecord::getIsDeleted, 0).eq(FireSafetyRecord::getStatus, "OPEN")
            .orderByDesc(FireSafetyRecord::getCheckTime).last("LIMIT 8"))
        .forEach(r -> items.add(alert("FIRE", "消防", r.getTitle(), r.getLocation(), r.getCheckTime(), "/fire/overview")));
    maintenanceMapper.selectList(Wrappers.lambdaQuery(MaintenanceRequest.class)
            .eq(MaintenanceRequest::getOrgId, orgId).eq(MaintenanceRequest::getIsDeleted, 0)
            .in(MaintenanceRequest::getStatus, OPEN_MAINTENANCE).eq(MaintenanceRequest::getPriority, "HIGH")
            .orderByDesc(MaintenanceRequest::getReportedAt).last("LIMIT 8"))
        .forEach(m -> items.add(alert("MAINTENANCE", "报修", m.getIssueType() == null ? "高优先级报修" : m.getIssueType(),
            m.getRoomNo(), m.getReportedAt(), "/logistics/assets/maintenance-record")));
    emergencyMapper.selectList(Wrappers.lambdaQuery(MedicalEmergencyEvent.class)
            .eq(MedicalEmergencyEvent::getOrgId, orgId).eq(MedicalEmergencyEvent::getIsDeleted, 0)
            .in(MedicalEmergencyEvent::getStatus, OPEN_EMERGENCY)
            .orderByDesc(MedicalEmergencyEvent::getEventTime).last("LIMIT 8"))
        .forEach(e -> items.add(alert("HEALTH", "急救", e.getSymptom() == null ? "急救事件进行中" : e.getSymptom(),
            e.getLocation(), e.getEventTime(), "/medical-care/emergency")));
    List<Map<String, Object>> latest = items.stream()
        .sorted(Comparator.comparing((Map<String, Object> m) -> (LocalDateTime) m.get("time"),
            Comparator.nullsLast(Comparator.reverseOrder())))
        .limit(8).toList();
    return block("智能告警 / 消防记录 / 报修 / 急救事件", null, "openCount", items.size(), "items", latest);
  }

  /** 今日重点任务：护理任务完成/总数，今日报修完成/新增，今日消防检查记录数 */
  private Map<String, Object> tasks(Long orgId, LocalDate today, LocalDateTime dayStart) {
    long careTotal = careTaskMapper.selectCount(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getOrgId, orgId).eq(CareTaskDaily::getIsDeleted, 0).eq(CareTaskDaily::getTaskDate, today));
    long careDone = careTaskMapper.selectCount(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getOrgId, orgId).eq(CareTaskDaily::getIsDeleted, 0).eq(CareTaskDaily::getTaskDate, today)
        .eq(CareTaskDaily::getStatus, "DONE"));
    long reported = maintenanceMapper.selectCount(Wrappers.lambdaQuery(MaintenanceRequest.class)
        .eq(MaintenanceRequest::getOrgId, orgId).eq(MaintenanceRequest::getIsDeleted, 0).ge(MaintenanceRequest::getReportedAt, dayStart));
    long repaired = maintenanceMapper.selectCount(Wrappers.lambdaQuery(MaintenanceRequest.class)
        .eq(MaintenanceRequest::getOrgId, orgId).eq(MaintenanceRequest::getIsDeleted, 0).eq(MaintenanceRequest::getStatus, "COMPLETED")
        .ge(MaintenanceRequest::getCompletedAt, dayStart));
    long fireChecks = fireRecordMapper.selectCount(Wrappers.lambdaQuery(FireSafetyRecord.class)
        .eq(FireSafetyRecord::getOrgId, orgId).eq(FireSafetyRecord::getIsDeleted, 0).ge(FireSafetyRecord::getCheckTime, dayStart));
    return block("护理任务 / 报修工单 / 消防记录", null, "items", List.of(
        row("key", "care", "label", "护理任务", "done", careDone, "total", careTotal, "link", "/care/workbench"),
        row("key", "repair", "label", "今日报修处理", "done", repaired, "total", reported, "link", "/logistics/assets/maintenance-record"),
        row("key", "fire", "label", "今日消防检查", "done", fireChecks, "total", null, "link", "/fire/overview")));
  }

  /** 本月报修工单：按状态计数 */
  private Map<String, Object> workOrders(Long orgId, LocalDateTime monthStart) {
    List<MaintenanceRequest> rows = maintenanceMapper.selectList(Wrappers.lambdaQuery(MaintenanceRequest.class)
        .select(MaintenanceRequest::getId, MaintenanceRequest::getStatus)
        .eq(MaintenanceRequest::getOrgId, orgId).eq(MaintenanceRequest::getIsDeleted, 0).ge(MaintenanceRequest::getReportedAt, monthStart));
    Map<String, Long> byStatus = rows.stream().collect(Collectors.groupingBy(
        r -> Objects.requireNonNullElse(r.getStatus(), "UNKNOWN"), Collectors.counting()));
    return block("报修工单（本月新报）", "/logistics/assets/maintenance-record", "total", rows.size(),
        "completed", byStatus.getOrDefault("COMPLETED", 0L),
        "processing", byStatus.getOrDefault("PROCESSING", 0L),
        "open", byStatus.getOrDefault("OPEN", 0L));
  }

  /** 今日健康监测：测量次数、异常次数、异常人数，及按小时的测量/异常分布 */
  private Map<String, Object> health(Long orgId, LocalDateTime dayStart) {
    List<HealthDataRecord> rows = healthMapper.selectList(Wrappers.lambdaQuery(HealthDataRecord.class)
        .select(HealthDataRecord::getId, HealthDataRecord::getElderId, HealthDataRecord::getMeasuredAt, HealthDataRecord::getAbnormalFlag)
        .eq(HealthDataRecord::getOrgId, orgId).eq(HealthDataRecord::getIsDeleted, 0).ge(HealthDataRecord::getMeasuredAt, dayStart));
    long[] measured = new long[24];
    long[] abnormal = new long[24];
    for (HealthDataRecord r : rows) {
      int h = r.getMeasuredAt().getHour();
      measured[h]++;
      if (Integer.valueOf(1).equals(r.getAbnormalFlag())) abnormal[h]++;
    }
    long abnormalElders = rows.stream().filter(r -> Integer.valueOf(1).equals(r.getAbnormalFlag()))
        .map(HealthDataRecord::getElderId).filter(Objects::nonNull).distinct().count();
    return block("健康数据记录（今日）", "/health/management/data", "measured", rows.size(),
        "abnormal", java.util.Arrays.stream(abnormal).sum(), "abnormalElders", abnormalElders,
        "hourlyMeasured", measured, "hourlyAbnormal", abnormal);
  }

  /** 用电：抄表数据（按账单月）。没有本月抄表时取最近一个有抄表的月份，并标明月份 */
  private Map<String, Object> energy(Long orgId, LocalDate today) {
    String month = YearMonth.from(today).toString();
    List<FinanceElectricityReading> rows = readings(orgId, month);
    if (rows.isEmpty()) {
      FinanceElectricityReading latest = electricityMapper.selectOne(Wrappers.lambdaQuery(FinanceElectricityReading.class)
          .select(FinanceElectricityReading::getId, FinanceElectricityReading::getBillMonth)
          .eq(FinanceElectricityReading::getOrgId, orgId).eq(FinanceElectricityReading::getIsDeleted, 0)
          .orderByDesc(FinanceElectricityReading::getBillMonth).last("LIMIT 1"));
      if (latest == null) {
        return block("电费抄表", "/finance/electricity-fee", "connected", false);
      }
      month = latest.getBillMonth();
      rows = readings(orgId, month);
    }
    BigDecimal usage = rows.stream().map(FinanceElectricityReading::getUsageAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal fee = rows.stream().map(FinanceElectricityReading::getFeeAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    Map<String, BigDecimal> byBuilding = rows.stream().collect(Collectors.groupingBy(
        r -> r.getBuilding() == null || r.getBuilding().isBlank() ? "未登记楼栋" : r.getBuilding(), TreeMap::new,
        Collectors.reducing(BigDecimal.ZERO, r -> r.getUsageAmount() == null ? BigDecimal.ZERO : r.getUsageAmount(), BigDecimal::add)));
    return block("电费抄表（" + month + "）", "/finance/electricity-fee", "connected", true, "month", month,
        "usageKwh", usage, "feeAmount", fee, "rooms", rows.size(),
        "byBuilding", byBuilding.entrySet().stream().map(e -> row("name", e.getKey(), "usageKwh", e.getValue())).toList());
  }

  private List<FinanceElectricityReading> readings(Long orgId, String month) {
    return electricityMapper.selectList(Wrappers.lambdaQuery(FinanceElectricityReading.class)
        .eq(FinanceElectricityReading::getOrgId, orgId).eq(FinanceElectricityReading::getIsDeleted, 0)
        .eq(FinanceElectricityReading::getBillMonth, month));
  }

  /** 摄像头台账（不含视频流；视频流未接入） */
  private Map<String, Object> cameras(Long orgId) {
    List<LogisticsCamera> rows = cameraMapper.selectList(Wrappers.lambdaQuery(LogisticsCamera.class)
        .eq(LogisticsCamera::getOrgId, orgId).orderByDesc(LogisticsCamera::getEnabled).last("LIMIT 50"));
    long enabled = rows.stream().filter(c -> Integer.valueOf(1).equals(c.getEnabled())).count();
    return block("摄像头台账（视频流未接入）", "/fire/video", "total", rows.size(), "enabled", enabled,
        "items", rows.stream().limit(6).map(c -> row("name", c.getCameraName(), "location", c.getLocation(),
            "enabled", Integer.valueOf(1).equals(c.getEnabled()))).toList());
  }

  private static Map<String, Object> alert(String type, String typeLabel, String title, String location,
      LocalDateTime time, String link) {
    return row("type", type, "typeLabel", typeLabel, "title", title, "location", location, "time", time, "link", link);
  }

  private static Map<String, Object> block(String source, String link, Object... kv) {
    Map<String, Object> m = row(kv);
    m.put("source", source);
    if (link != null) m.put("link", link);
    return m;
  }

  private static Map<String, Object> row(Object... kv) {
    Map<String, Object> m = new LinkedHashMap<>();
    for (int i = 0; i < kv.length; i += 2) m.put((String) kv[i], kv[i + 1]);
    return m;
  }

  private static Long orgId() {
    Long orgId = AuthContext.getOrgId();
    if (orgId == null) throw new AccessDeniedException("无法确定所属机构");
    return orgId;
  }
}
