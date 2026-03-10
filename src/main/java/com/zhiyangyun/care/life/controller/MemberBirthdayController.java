package com.zhiyangyun.care.life.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.life.model.BirthdayReminderResponse;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/life/birthday")
public class MemberBirthdayController {
  private final ElderMapper elderMapper;
  private final BedMapper bedMapper;
  private final RoomMapper roomMapper;

  public MemberBirthdayController(ElderMapper elderMapper, BedMapper bedMapper, RoomMapper roomMapper) {
    this.elderMapper = elderMapper;
    this.bedMapper = bedMapper;
    this.roomMapper = roomMapper;
  }

  @GetMapping("/page")
  public Result<IPage<BirthdayReminderResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Integer month,
      @RequestParam(required = false) Integer daysAhead,
      @RequestParam(required = false) String keyword) {
    long safePageNo = normalizePageNo(pageNo);
    long safePageSize = normalizePageSize(pageSize);
    Integer normalizedMonth = normalizeMonth(month);
    Integer normalizedDaysAhead = normalizeDaysAhead(daysAhead);
    Long orgId = AuthContext.getOrgId();
    LocalDate today = LocalDate.now();
    LocalDate minBirthDate = LocalDate.of(1900, 1, 1);

    List<ElderProfile> elders = elderMapper.selectList(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(orgId != null, ElderProfile::getOrgId, orgId)
        .eq(ElderProfile::getStatus, 1)
        .isNotNull(ElderProfile::getBirthDate)
        // 屏蔽异常生日值（如 0000-00-00 / 极端日期）导致的映射与计算异常。
        .ge(ElderProfile::getBirthDate, minBirthDate)
        .le(ElderProfile::getBirthDate, today)
        .like(keyword != null && !keyword.isBlank(), ElderProfile::getFullName, keyword));

    List<Long> bedIds = elders.stream()
        .map(ElderProfile::getBedId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, Bed> bedMap = bedIds.isEmpty()
        ? Map.of()
        : bedMapper.selectBatchIds(bedIds).stream()
            .collect(Collectors.toMap(Bed::getId, b -> b, (a, b) -> a));

    List<Long> roomIds = bedMap.values().stream()
        .map(Bed::getRoomId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, Room> roomMap = roomIds.isEmpty()
        ? Map.of()
        : roomMapper.selectBatchIds(roomIds).stream()
            .collect(Collectors.toMap(Room::getId, r -> r, (a, b) -> a));

    List<BirthdayReminderResponse> list = elders.stream()
        .map(elder -> toResponse(elder, bedMap.get(elder.getBedId()), roomMap, today))
        .filter(item -> item.getNextBirthday() != null)
        .filter(item -> normalizedMonth == null || item.getNextBirthday().getMonthValue() == normalizedMonth)
        .filter(item -> normalizedDaysAhead == null || item.getDaysUntil() <= normalizedDaysAhead)
        .sorted(Comparator.comparing(
                BirthdayReminderResponse::getDaysUntil,
                Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(
                BirthdayReminderResponse::getElderId,
                Comparator.nullsLast(Comparator.naturalOrder())))
        .toList();

    long total = list.size();
    int from = (int) Math.max((safePageNo - 1) * safePageSize, 0);
    int to = (int) Math.min(from + safePageSize, total);
    List<BirthdayReminderResponse> records = from >= to ? List.of() : list.subList(from, to);

    IPage<BirthdayReminderResponse> page = new Page<>(safePageNo, safePageSize, total);
    page.setRecords(records);
    return Result.ok(page);
  }

  private long normalizePageNo(long pageNo) {
    return pageNo <= 0 ? 1 : pageNo;
  }

  private long normalizePageSize(long pageSize) {
    if (pageSize <= 0) return 20;
    return Math.min(pageSize, 500);
  }

  private Integer normalizeMonth(Integer month) {
    if (month == null) return null;
    if (month < 1 || month > 12) {
      throw new IllegalArgumentException("month 仅支持 1-12");
    }
    return month;
  }

  private Integer normalizeDaysAhead(Integer daysAhead) {
    if (daysAhead == null) return null;
    if (daysAhead < 0) {
      throw new IllegalArgumentException("daysAhead 不能小于 0");
    }
    return Math.min(daysAhead, 3660);
  }

  private BirthdayReminderResponse toResponse(
      ElderProfile elder, Bed bed, Map<Long, Room> roomMap, LocalDate today) {
    BirthdayReminderResponse resp = new BirthdayReminderResponse();
    resp.setElderId(elder.getId());
    resp.setElderName(elder.getFullName());
    resp.setBirthDate(elder.getBirthDate());
    LocalDate nextBirthday = nextBirthday(elder.getBirthDate(), today);
    resp.setNextBirthday(nextBirthday);
    if (nextBirthday != null) {
      resp.setDaysUntil(ChronoUnit.DAYS.between(today, nextBirthday));
      resp.setAgeOnNextBirthday(nextBirthday.getYear() - elder.getBirthDate().getYear());
    }
    if (bed != null) {
      resp.setBedNo(bed.getBedNo());
      Room room = roomMap.get(bed.getRoomId());
      if (room != null) {
        resp.setRoomNo(room.getRoomNo());
      }
    }
    return resp;
  }

  private LocalDate nextBirthday(LocalDate birthDate, LocalDate today) {
    if (birthDate == null) {
      return null;
    }
    LocalDate thisYear = safeWithYear(birthDate, today.getYear());
    if (thisYear.isBefore(today)) {
      return safeWithYear(birthDate, today.getYear() + 1);
    }
    return thisYear;
  }

  private LocalDate safeWithYear(LocalDate date, int year) {
    try {
      return date.withYear(year);
    } catch (DateTimeException ignored) {
      return LocalDate.of(year, 2, 28);
    }
  }
}
