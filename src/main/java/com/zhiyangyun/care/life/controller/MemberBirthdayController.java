package com.zhiyangyun.care.life.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.mapper.BedMapper;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/life/birthday")
public class MemberBirthdayController {
  private static final Logger log = LoggerFactory.getLogger(MemberBirthdayController.class);

  private final JdbcTemplate jdbcTemplate;
  private final BedMapper bedMapper;
  private final RoomMapper roomMapper;

  public MemberBirthdayController(JdbcTemplate jdbcTemplate, BedMapper bedMapper, RoomMapper roomMapper) {
    this.jdbcTemplate = jdbcTemplate;
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

    List<ElderProfile> elders = loadBirthdayElders(orgId, keyword).stream()
        .filter(Objects::nonNull)
        .filter(elder -> elder.getBirthDate() != null)
        .filter(elder -> !elder.getBirthDate().isBefore(minBirthDate))
        .filter(elder -> !elder.getBirthDate().isAfter(today))
        .toList();

    List<Long> bedIds = elders.stream()
        .map(ElderProfile::getBedId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, Bed> bedMap = bedIds.isEmpty()
        ? Map.of()
        : bedMapper.selectBatchIds(bedIds).stream()
            .filter(Objects::nonNull)
            .filter(bed -> bed.getId() != null)
            .collect(Collectors.toMap(Bed::getId, b -> b, (a, b) -> a));

    List<Long> roomIds = bedMap.values().stream()
        .map(Bed::getRoomId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, Room> roomMap = roomIds.isEmpty()
        ? Map.of()
        : roomMapper.selectBatchIds(roomIds).stream()
            .filter(Objects::nonNull)
            .filter(room -> room.getId() != null)
            .collect(Collectors.toMap(Room::getId, r -> r, (a, b) -> a));

    List<BirthdayReminderResponse> list = elders.stream()
        .map(elder -> safeToResponse(elder, safeMapGet(bedMap, elder == null ? null : elder.getBedId()), roomMap, today))
        .filter(Objects::nonNull)
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

  private BirthdayReminderResponse safeToResponse(
      ElderProfile elder, Bed bed, Map<Long, Room> roomMap, LocalDate today) {
    try {
      return toResponse(elder, bed, roomMap, today);
    } catch (RuntimeException ex) {
      log.warn("Skip invalid birthday reminder row, elderId={}, fullName={}, birthDate={}",
          elder == null ? null : elder.getId(),
          elder == null ? null : elder.getFullName(),
          elder == null ? null : elder.getBirthDate(),
          ex);
      return null;
    }
  }

  private <K, V> V safeMapGet(Map<K, V> map, K key) {
    if (map == null || key == null) {
      return null;
    }
    return map.get(key);
  }

  private List<ElderProfile> loadBirthdayElders(Long orgId, String keyword) {
    String normalizedKeyword = keyword == null ? "" : keyword.trim();
    return jdbcTemplate.query("""
        SELECT id, full_name, bed_id, CAST(birth_date AS CHAR) AS birth_date_text
        FROM elder
        WHERE is_deleted = 0
          AND status = 1
          AND birth_date IS NOT NULL
          AND CAST(birth_date AS CHAR) <> '0000-00-00'
          AND (? IS NULL OR org_id = ?)
          AND (? = '' OR full_name LIKE CONCAT('%', ?, '%'))
        """,
        (rs, rowNum) -> toElderProfile(rs.getLong("id"), rs.getString("full_name"), rs.getObject("bed_id"), rs.getString("birth_date_text")),
        orgId, orgId, normalizedKeyword, normalizedKeyword);
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

  private ElderProfile toElderProfile(Long id, String fullName, Object bedIdValue, String birthDateText) {
    LocalDate birthDate = parseBirthDate(birthDateText);
    if (birthDate == null) {
      return null;
    }
    ElderProfile elder = new ElderProfile();
    elder.setId(id);
    elder.setFullName(fullName);
    if (bedIdValue instanceof Number number) {
      elder.setBedId(number.longValue());
    }
    elder.setBirthDate(birthDate);
    return elder;
  }

  private LocalDate parseBirthDate(String birthDateText) {
    if (birthDateText == null) {
      return null;
    }
    String normalized = birthDateText.trim();
    if (normalized.isEmpty() || !normalized.matches("\\d{4}-\\d{2}-\\d{2}")) {
      return null;
    }
    String month = normalized.substring(5, 7);
    String day = normalized.substring(8, 10);
    if ("00".equals(month) || "00".equals(day)) {
      return null;
    }
    try {
      return LocalDate.parse(normalized);
    } catch (DateTimeException ignored) {
      return null;
    }
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
      Room room = safeMapGet(roomMap, bed.getRoomId());
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
