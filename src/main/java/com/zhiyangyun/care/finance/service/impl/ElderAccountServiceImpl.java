package com.zhiyangyun.care.finance.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.zhiyangyun.care.auth.entity.Org;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.OrgMapper;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.finance.entity.ElderAccount;
import com.zhiyangyun.care.finance.entity.ElderAccountLog;
import com.zhiyangyun.care.finance.mapper.ElderAccountLogMapper;
import com.zhiyangyun.care.finance.mapper.ElderAccountMapper;
import com.zhiyangyun.care.finance.model.ElderAccountAdjustRequest;
import com.zhiyangyun.care.finance.model.ElderAccountLogResponse;
import com.zhiyangyun.care.finance.model.ElderAccountResponse;
import com.zhiyangyun.care.finance.model.ElderAccountUpdateRequest;
import com.zhiyangyun.care.finance.service.ElderAccountService;
import com.zhiyangyun.care.store.entity.ElderPointsAccount;
import com.zhiyangyun.care.store.mapper.ElderPointsAccountMapper;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ElderAccountServiceImpl implements ElderAccountService {
  private final ElderAccountMapper accountMapper;
  private final ElderAccountLogMapper logMapper;
  private final ElderMapper elderMapper;
  private final ElderPointsAccountMapper pointsAccountMapper;
  private final StaffMapper staffMapper;
  private final OrgMapper orgMapper;
  private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static final Pattern RECHARGE_METHOD_PATTERN = Pattern.compile("充值方式:([^|]+)");
  private static final Pattern RECHARGE_TIME_PATTERN = Pattern.compile("充值时间:([0-9\\-: ]+)");

  public ElderAccountServiceImpl(ElderAccountMapper accountMapper,
      ElderAccountLogMapper logMapper,
      ElderMapper elderMapper,
      ElderPointsAccountMapper pointsAccountMapper,
      StaffMapper staffMapper,
      OrgMapper orgMapper) {
    this.accountMapper = accountMapper;
    this.logMapper = logMapper;
    this.elderMapper = elderMapper;
    this.pointsAccountMapper = pointsAccountMapper;
    this.staffMapper = staffMapper;
    this.orgMapper = orgMapper;
  }

  @Override
  @Transactional
  public ElderAccountResponse getOrCreate(Long orgId, Long elderId, Long operatorId) {
    ElderAccount account = accountMapper.selectOne(Wrappers.lambdaQuery(ElderAccount.class)
        .eq(ElderAccount::getIsDeleted, 0)
        .eq(orgId != null, ElderAccount::getOrgId, orgId)
        .eq(ElderAccount::getElderId, elderId));
    if (account == null) {
      account = new ElderAccount();
      account.setTenantId(orgId);
      account.setOrgId(orgId);
      account.setElderId(elderId);
      account.setBalance(BigDecimal.ZERO);
      account.setCreditLimit(BigDecimal.ZERO);
      account.setWarnThreshold(BigDecimal.ZERO);
      account.setStatus(1);
      account.setCreatedBy(operatorId);
      accountMapper.insert(account);
    }
    List<Long> elderIds = List.of(elderId);
    return toAccountResponse(account,
        elderNameMap(elderIds).get(elderId),
        pointsAccountMap(elderIds).get(elderId));
  }

  @Override
  public IPage<ElderAccountResponse> page(Long orgId, long pageNo, long pageSize, String keyword) {
    List<Long> elderIds = null;
    if (keyword != null && !keyword.isBlank()) {
      elderIds = elderMapper.selectList(Wrappers.lambdaQuery(ElderProfile.class)
          .eq(ElderProfile::getIsDeleted, 0)
          .eq(orgId != null, ElderProfile::getOrgId, orgId)
          .like(ElderProfile::getFullName, keyword))
          .stream().map(ElderProfile::getId).toList();
      if (elderIds.isEmpty()) {
        return new Page<>(pageNo, pageSize, 0);
      }
    }

    var wrapper = Wrappers.lambdaQuery(ElderAccount.class)
        .eq(ElderAccount::getIsDeleted, 0)
        .eq(orgId != null, ElderAccount::getOrgId, orgId);
    if (elderIds != null) {
      wrapper.in(ElderAccount::getElderId, elderIds);
    }
    wrapper.orderByDesc(ElderAccount::getUpdateTime);

    IPage<ElderAccount> page = accountMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    List<Long> pageElderIds = page.getRecords().stream()
        .map(ElderAccount::getElderId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, String> elderNameMap = elderNameMap(pageElderIds);
    Map<Long, ElderPointsAccount> pointsMap = pointsAccountMap(pageElderIds);

    IPage<ElderAccountResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream()
        .map(acc -> toAccountResponse(acc, elderNameMap.get(acc.getElderId()), pointsMap.get(acc.getElderId())))
        .toList());
    return resp;
  }

  @Override
  public IPage<ElderAccountLogResponse> logPage(Long orgId, long pageNo, long pageSize,
      Long elderId, Long accountId, String keyword) {
    LambdaQueryWrapper<ElderAccountLog> wrapper = buildLogQuery(orgId, elderId, accountId, keyword);
    if (wrapper == null) {
      return new Page<>(pageNo, pageSize, 0);
    }
    wrapper.orderByDesc(ElderAccountLog::getCreateTime);
    IPage<ElderAccountLog> page = logMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    Map<Long, String> elderNameMap = elderNameMap(page.getRecords().stream()
        .map(ElderAccountLog::getElderId)
        .filter(Objects::nonNull)
        .distinct()
        .toList());

    IPage<ElderAccountLogResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream()
        .map(log -> toLogResponse(log, elderNameMap.get(log.getElderId())))
        .toList());
    return resp;
  }

  @Override
  public byte[] exportLogPdf(Long orgId, Long operatorId, Long elderId, Long accountId, String keyword) {
    if (elderId == null) {
      throw new IllegalArgumentException("elderId required");
    }
    LambdaQueryWrapper<ElderAccountLog> wrapper = buildLogQuery(orgId, elderId, accountId, keyword);
    if (wrapper == null) {
      wrapper = Wrappers.lambdaQuery(ElderAccountLog.class).eq(ElderAccountLog::getId, -1L);
    }
    wrapper.orderByDesc(ElderAccountLog::getCreateTime);
    List<ElderAccountLog> logs = logMapper.selectList(wrapper);

    Map<Long, String> elderNameMap = elderNameMap(logs.stream()
        .map(ElderAccountLog::getElderId)
        .filter(Objects::nonNull)
        .distinct()
        .toList());
    List<ElderAccountLogResponse> records = logs.stream()
        .map(log -> toLogResponse(log, elderNameMap.get(log.getElderId())))
        .toList();

    String orgName = resolveOrgName(orgId);
    String operatorName = resolveOperatorName(operatorId);
    String elderName = records.isEmpty() ? ("老人#" + elderId)
        : safe(records.get(0).getElderName(), "老人#" + elderId);
    LocalDateTime now = LocalDateTime.now();
    String exportTime = DATE_TIME_FMT.format(now);

    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      Document document = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
      PdfWriter.getInstance(document, out);
      document.open();

      BaseFont baseFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
      Font titleFont = new Font(baseFont, 16, Font.BOLD);
      Font bodyFont = new Font(baseFont, 10, Font.NORMAL);
      Font headerFont = new Font(baseFont, 10, Font.BOLD);

      Paragraph title = new Paragraph(safe(orgName, "机构") + " - 老人账户流水单", titleFont);
      title.setAlignment(Element.ALIGN_CENTER);
      title.setSpacingAfter(12);
      document.add(title);

      Paragraph meta = new Paragraph(
          "老人：" + elderName + "    打印时间：" + exportTime + "    操作人：" + operatorName,
          bodyFont);
      meta.setSpacingAfter(10);
      document.add(meta);

      PdfPTable table = new PdfPTable(new float[] {1.2f, 0.9f, 1.0f, 1.0f, 1.2f, 1.1f, 1.4f, 1.4f, 1.8f});
      table.setWidthPercentage(100);
      addHeaderCell(table, "老人", headerFont);
      addHeaderCell(table, "方向", headerFont);
      addHeaderCell(table, "金额", headerFont);
      addHeaderCell(table, "余额", headerFont);
      addHeaderCell(table, "来源", headerFont);
      addHeaderCell(table, "充值方式", headerFont);
      addHeaderCell(table, "充值时间", headerFont);
      addHeaderCell(table, "记账时间", headerFont);
      addHeaderCell(table, "备注", headerFont);

      for (ElderAccountLogResponse row : records) {
        addBodyCell(table, row.getElderName(), bodyFont);
        addBodyCell(table, formatDirection(row.getDirection()), bodyFont);
        addBodyCell(table, formatAmount(row.getAmount()), bodyFont);
        addBodyCell(table, formatAmount(row.getBalanceAfter()), bodyFont);
        addBodyCell(table, row.getSourceType(), bodyFont);
        addBodyCell(table, parseRechargeMethod(row.getRemark()), bodyFont);
        addBodyCell(table, parseRechargeTime(row.getRemark()), bodyFont);
        addBodyCell(table, formatDateTime(row.getCreateTime()), bodyFont);
        addBodyCell(table, row.getRemark(), bodyFont);
      }

      if (records.isEmpty()) {
        PdfPCell emptyCell = new PdfPCell(new Phrase("暂无流水数据", bodyFont));
        emptyCell.setColspan(9);
        emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        emptyCell.setPadding(8);
        table.addCell(emptyCell);
      }

      document.add(table);
      document.close();
      return out.toByteArray();
    } catch (Exception ex) {
      throw new IllegalStateException("failed to build account log pdf", ex);
    }
  }

  @Override
  @Transactional
  public ElderAccountResponse adjust(Long orgId, Long operatorId, ElderAccountAdjustRequest request) {
    if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("amount must be positive");
    }
    Long elderId = request.getElderId();
    if (elderId == null && request.getElderName() != null && !request.getElderName().isBlank()) {
      elderId = resolveElderId(orgId, request.getElderName());
    }
    if (elderId == null) {
      throw new IllegalArgumentException("elderId or elderName required");
    }
    ElderAccount account = accountMapper.selectOne(Wrappers.lambdaQuery(ElderAccount.class)
        .eq(ElderAccount::getIsDeleted, 0)
        .eq(orgId != null, ElderAccount::getOrgId, orgId)
        .eq(ElderAccount::getElderId, elderId));
    if (account == null) {
      account = new ElderAccount();
      account.setTenantId(orgId);
      account.setOrgId(orgId);
      account.setElderId(elderId);
      account.setBalance(BigDecimal.ZERO);
      account.setCreditLimit(BigDecimal.ZERO);
      account.setWarnThreshold(BigDecimal.ZERO);
      account.setStatus(1);
      account.setCreatedBy(operatorId);
      accountMapper.insert(account);
    }
    applyChange(account, request.getAmount(), request.getDirection(), "ADJUST", null,
        request.getRemark(), operatorId);
    List<Long> elderIds = List.of(account.getElderId());
    return toAccountResponse(account,
        elderNameMap(elderIds).get(account.getElderId()),
        pointsAccountMap(elderIds).get(account.getElderId()));
  }

  @Override
  @Transactional
  public ElderAccountResponse updateAccount(Long orgId, Long operatorId, ElderAccountUpdateRequest request) {
    Long elderId = request.getElderId();
    if (elderId == null && request.getElderName() != null && !request.getElderName().isBlank()) {
      elderId = resolveElderId(orgId, request.getElderName());
    }
    if (elderId == null) {
      throw new IllegalArgumentException("elderId or elderName required");
    }
    ElderAccount account = accountMapper.selectOne(Wrappers.lambdaQuery(ElderAccount.class)
        .eq(ElderAccount::getIsDeleted, 0)
        .eq(orgId != null, ElderAccount::getOrgId, orgId)
        .eq(ElderAccount::getElderId, elderId));
    if (account == null) {
      account = new ElderAccount();
      account.setTenantId(orgId);
      account.setOrgId(orgId);
      account.setElderId(elderId);
      account.setBalance(BigDecimal.ZERO);
      account.setCreditLimit(BigDecimal.ZERO);
      account.setWarnThreshold(BigDecimal.ZERO);
      account.setStatus(1);
      account.setCreatedBy(operatorId);
      accountMapper.insert(account);
    }
    if (request.getCreditLimit() != null) {
      account.setCreditLimit(request.getCreditLimit());
    }
    if (request.getWarnThreshold() != null) {
      account.setWarnThreshold(request.getWarnThreshold());
    }
    if (request.getStatus() != null) {
      account.setStatus(request.getStatus());
    }
    if (request.getRemark() != null) {
      account.setRemark(request.getRemark());
    }
    accountMapper.updateById(account);
    List<Long> elderIds = List.of(account.getElderId());
    return toAccountResponse(account,
        elderNameMap(elderIds).get(account.getElderId()),
        pointsAccountMap(elderIds).get(account.getElderId()));
  }

  @Override
  @Transactional
  public void chargeForTask(Long orgId, Long elderId, Long taskDailyId, BigDecimal amount,
      String remark, Long operatorId) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      return;
    }
    ElderAccount account = accountMapper.selectOne(Wrappers.lambdaQuery(ElderAccount.class)
        .eq(ElderAccount::getIsDeleted, 0)
        .eq(orgId != null, ElderAccount::getOrgId, orgId)
        .eq(ElderAccount::getElderId, elderId));
    if (account == null) {
      account = new ElderAccount();
      account.setTenantId(orgId);
      account.setOrgId(orgId);
      account.setElderId(elderId);
      account.setBalance(BigDecimal.ZERO);
      account.setCreditLimit(BigDecimal.ZERO);
      account.setWarnThreshold(BigDecimal.ZERO);
      account.setStatus(1);
      account.setCreatedBy(operatorId);
      accountMapper.insert(account);
    }
    applyChange(account, amount, "DEBIT", "TASK", taskDailyId, remark, operatorId);
  }

  @Override
  public List<ElderAccountResponse> warnings(Long orgId) {
    List<ElderAccount> accounts = accountMapper.selectList(Wrappers.lambdaQuery(ElderAccount.class)
        .eq(ElderAccount::getIsDeleted, 0)
        .eq(orgId != null, ElderAccount::getOrgId, orgId)
        .eq(ElderAccount::getStatus, 1));
    List<ElderAccount> warningList = accounts.stream()
        .filter(acc -> acc.getWarnThreshold() != null
            && acc.getBalance() != null
            && acc.getBalance().compareTo(acc.getWarnThreshold()) <= 0)
        .toList();
    List<Long> warningElderIds = warningList.stream()
        .map(ElderAccount::getElderId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, String> elderNameMap = elderNameMap(warningElderIds);
    Map<Long, ElderPointsAccount> pointsMap = pointsAccountMap(warningElderIds);
    return warningList.stream()
        .map(acc -> toAccountResponse(acc, elderNameMap.get(acc.getElderId()), pointsMap.get(acc.getElderId())))
        .toList();
  }

  private void applyChange(ElderAccount account, BigDecimal amount, String direction,
      String sourceType, Long sourceId, String remark, Long operatorId) {
    if (direction == null || (!direction.equalsIgnoreCase("DEBIT") && !direction.equalsIgnoreCase("CREDIT"))) {
      throw new IllegalArgumentException("direction must be DEBIT or CREDIT");
    }
    BigDecimal balance = account.getBalance() == null ? BigDecimal.ZERO : account.getBalance();
    BigDecimal delta = amount;
    if (direction.equalsIgnoreCase("DEBIT")) {
      balance = balance.subtract(delta);
    } else {
      balance = balance.add(delta);
    }
    account.setBalance(balance);
    accountMapper.updateById(account);

    ElderAccountLog log = new ElderAccountLog();
    log.setTenantId(account.getTenantId());
    log.setOrgId(account.getOrgId());
    log.setElderId(account.getElderId());
    log.setAccountId(account.getId());
    log.setAmount(amount);
    log.setBalanceAfter(balance);
    log.setDirection(direction.toUpperCase());
    log.setSourceType(sourceType);
    log.setSourceId(sourceId);
    log.setRemark(remark);
    log.setCreatedBy(operatorId);
    logMapper.insert(log);
  }

  private Map<Long, String> elderNameMap(List<Long> elderIds) {
    if (elderIds == null || elderIds.isEmpty()) {
      return Map.of();
    }
    return elderMapper.selectList(Wrappers.lambdaQuery(ElderProfile.class)
            .in(ElderProfile::getId, elderIds))
        .stream()
        .collect(Collectors.toMap(ElderProfile::getId, ElderProfile::getFullName, (a, b) -> a));
  }

  private Long resolveElderId(Long orgId, String elderName) {
    if (elderName == null || elderName.isBlank()) {
      return null;
    }
    ElderProfile elder = elderMapper.selectOne(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(orgId != null, ElderProfile::getOrgId, orgId)
        .eq(ElderProfile::getFullName, elderName)
        .last("LIMIT 1"));
    return elder == null ? null : elder.getId();
  }

  private LambdaQueryWrapper<ElderAccountLog> buildLogQuery(Long orgId, Long elderId, Long accountId, String keyword) {
    LambdaQueryWrapper<ElderAccountLog> wrapper = Wrappers.lambdaQuery(ElderAccountLog.class)
        .eq(ElderAccountLog::getIsDeleted, 0)
        .eq(orgId != null, ElderAccountLog::getOrgId, orgId);
    if (elderId != null) {
      wrapper.eq(ElderAccountLog::getElderId, elderId);
    }
    if (accountId != null) {
      wrapper.eq(ElderAccountLog::getAccountId, accountId);
    }
    if (keyword != null && !keyword.isBlank()) {
      List<Long> elderIds = elderMapper.selectList(Wrappers.lambdaQuery(ElderProfile.class)
          .eq(ElderProfile::getIsDeleted, 0)
          .eq(orgId != null, ElderProfile::getOrgId, orgId)
          .like(ElderProfile::getFullName, keyword))
          .stream().map(ElderProfile::getId).toList();
      if (elderIds.isEmpty()) {
        return null;
      }
      wrapper.in(ElderAccountLog::getElderId, elderIds);
    }
    return wrapper;
  }

  private ElderAccountResponse toAccountResponse(ElderAccount account, String elderName,
      ElderPointsAccount pointsAccount) {
    ElderAccountResponse resp = new ElderAccountResponse();
    resp.setId(account.getId());
    resp.setElderId(account.getElderId());
    resp.setElderName(elderName);
    resp.setBalance(account.getBalance());
    resp.setCreditLimit(account.getCreditLimit());
    resp.setWarnThreshold(account.getWarnThreshold());
    resp.setStatus(account.getStatus());
    if (pointsAccount != null) {
      resp.setPointsBalance(pointsAccount.getPointsBalance());
      resp.setPointsStatus(pointsAccount.getStatus());
    }
    resp.setRemark(account.getRemark());
    resp.setCreateTime(account.getCreateTime());
    resp.setUpdateTime(account.getUpdateTime());
    return resp;
  }

  private ElderAccountLogResponse toLogResponse(ElderAccountLog log, String elderName) {
    ElderAccountLogResponse resp = new ElderAccountLogResponse();
    resp.setId(log.getId());
    resp.setAccountId(log.getAccountId());
    resp.setElderId(log.getElderId());
    resp.setElderName(elderName);
    resp.setAmount(log.getAmount());
    resp.setBalanceAfter(log.getBalanceAfter());
    resp.setDirection(log.getDirection());
    resp.setSourceType(log.getSourceType());
    resp.setSourceId(log.getSourceId());
    resp.setRemark(log.getRemark());
    resp.setCreateTime(log.getCreateTime());
    return resp;
  }

  private Map<Long, ElderPointsAccount> pointsAccountMap(List<Long> elderIds) {
    if (elderIds == null || elderIds.isEmpty()) {
      return Map.of();
    }
    return pointsAccountMapper.selectList(Wrappers.lambdaQuery(ElderPointsAccount.class)
            .in(ElderPointsAccount::getElderId, elderIds))
        .stream()
        .collect(Collectors.toMap(ElderPointsAccount::getElderId, item -> item, (a, b) -> a));
  }

  private String resolveOrgName(Long orgId) {
    if (orgId == null) {
      return "机构";
    }
    Org org = orgMapper.selectById(orgId);
    return org == null || org.getOrgName() == null || org.getOrgName().isBlank()
        ? "机构"
        : org.getOrgName();
  }

  private String resolveOperatorName(Long operatorId) {
    if (operatorId == null) {
      return "系统";
    }
    StaffAccount staff = staffMapper.selectById(operatorId);
    if (staff == null) {
      return "员工#" + operatorId;
    }
    if (staff.getRealName() != null && !staff.getRealName().isBlank()) {
      return staff.getRealName();
    }
    if (staff.getUsername() != null && !staff.getUsername().isBlank()) {
      return staff.getUsername();
    }
    return "员工#" + operatorId;
  }

  private void addHeaderCell(PdfPTable table, String value, Font font) {
    PdfPCell cell = new PdfPCell(new Phrase(value, font));
    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    cell.setPadding(6);
    table.addCell(cell);
  }

  private void addBodyCell(PdfPTable table, String value, Font font) {
    PdfPCell cell = new PdfPCell(new Phrase(safe(value, "-"), font));
    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    cell.setPadding(5);
    table.addCell(cell);
  }

  private String formatDirection(String direction) {
    if (direction == null) {
      return "-";
    }
    if ("DEBIT".equalsIgnoreCase(direction)) {
      return "支出";
    }
    if ("CREDIT".equalsIgnoreCase(direction)) {
      return "收入";
    }
    return direction;
  }

  private String formatAmount(BigDecimal amount) {
    return amount == null ? "-" : amount.toPlainString();
  }

  private String formatDateTime(LocalDateTime time) {
    return time == null ? "-" : DATE_TIME_FMT.format(time);
  }

  private String parseRechargeMethod(String remark) {
    Matcher matcher = RECHARGE_METHOD_PATTERN.matcher(safe(remark, ""));
    if (!matcher.find()) {
      return "-";
    }
    String raw = matcher.group(1).trim().toUpperCase();
    return switch (raw) {
      case "ALIPAY" -> "支付宝";
      case "WECHAT" -> "微信";
      case "QR_CODE" -> "扫码";
      case "CASH" -> "现金";
      case "BANK" -> "转账";
      default -> raw;
    };
  }

  private String parseRechargeTime(String remark) {
    Matcher matcher = RECHARGE_TIME_PATTERN.matcher(safe(remark, ""));
    return matcher.find() ? safe(matcher.group(1), "-").trim() : "-";
  }

  private String safe(String value, String fallback) {
    if (value == null || value.isBlank()) {
      return fallback;
    }
    return value;
  }
}
