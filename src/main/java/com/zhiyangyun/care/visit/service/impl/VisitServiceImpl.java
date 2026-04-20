package com.zhiyangyun.care.visit.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.ElderFamily;
import com.zhiyangyun.care.elder.entity.FamilyUser;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.model.ElderLifecycleStatus;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.ElderFamilyMapper;
import com.zhiyangyun.care.elder.mapper.FamilyUserMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.visit.entity.VisitBooking;
import com.zhiyangyun.care.visit.entity.VisitCheckLog;
import com.zhiyangyun.care.visit.mapper.VisitBookingMapper;
import com.zhiyangyun.care.visit.mapper.VisitCheckLogMapper;
import com.zhiyangyun.care.visit.model.VisitBookRequest;
import com.zhiyangyun.care.visit.model.VisitBookingResponse;
import com.zhiyangyun.care.visit.model.VisitCheckInRequest;
import com.zhiyangyun.care.visit.model.VisitCheckInResponse;
import com.zhiyangyun.care.visit.model.VisitPrintTicketResponse;
import com.zhiyangyun.care.visit.service.VisitService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VisitServiceImpl implements VisitService {
  private static final int STATUS_BOOKED = 0;
  private static final int STATUS_CHECKED_IN = 1;
  private static final int STATUS_CANCELLED = 2;
  private static final int MAX_VISITOR_COUNT = 5;
  private static final int MAX_ADVANCE_DAYS = 30;
  private final VisitBookingMapper bookingMapper;
  private final VisitCheckLogMapper checkLogMapper;
  private final ElderMapper elderMapper;
  private final ElderFamilyMapper elderFamilyMapper;
  private final FamilyUserMapper familyUserMapper;
  private final BedMapper bedMapper;
  private final RoomMapper roomMapper;

  public VisitServiceImpl(VisitBookingMapper bookingMapper,
      VisitCheckLogMapper checkLogMapper,
      ElderMapper elderMapper,
      ElderFamilyMapper elderFamilyMapper,
      FamilyUserMapper familyUserMapper,
      BedMapper bedMapper,
      RoomMapper roomMapper) {
    this.bookingMapper = bookingMapper;
    this.checkLogMapper = checkLogMapper;
    this.elderMapper = elderMapper;
    this.elderFamilyMapper = elderFamilyMapper;
    this.familyUserMapper = familyUserMapper;
    this.bedMapper = bedMapper;
    this.roomMapper = roomMapper;
  }

  @Override
  public VisitBookingResponse book(VisitBookRequest request) {
    ElderProfile elder = elderMapper.selectById(request.getElderId());
    if (elder == null) {
      throw new IllegalArgumentException("Elder not found");
    }
    validateBookingRequest(request, elder);
    Long familyUserId = resolveFamilyUserId(request.getOrgId(), request.getElderId(), request.getFamilyUserId());
    FamilyUser familyUser = familyUserMapper.selectById(familyUserId);
    ElderFamily relation = resolveFamilyRelation(request.getOrgId(), request.getElderId(), familyUserId);

    VisitBooking booking = new VisitBooking();
    booking.setOrgId(request.getOrgId());
    booking.setElderId(request.getElderId());
    booking.setFamilyUserId(familyUserId);
    booking.setVisitorName(resolveVisitorName(request, familyUser));
    booking.setVisitorPhone(resolveVisitorPhone(request, familyUser));
    booking.setVisitorRelation(resolveVisitorRelation(request, relation));
    booking.setVisitTime(request.getVisitTime());
    booking.setVisitDate(request.getVisitTime().toLocalDate());
    booking.setVisitTimeSlot(request.getVisitTimeSlot());
    booking.setVisitorCount(request.getVisitorCount());
    booking.setCarPlate(request.getCarPlate());
    booking.setStatus(STATUS_BOOKED);
    booking.setVerifyCode(generateUniqueCode(request.getOrgId()));
    booking.setVisitCode(generateUniqueCode(request.getOrgId()));
    booking.setRemark(request.getRemark());
    bookingMapper.insert(booking);

    return toResponse(booking, elder, familyUser, relation);
  }

  @Override
  public List<VisitBookingResponse> todayList(Long orgId) {
    LocalDate today = LocalDate.now();
    List<VisitBooking> bookings = bookingMapper.selectList(
        Wrappers.lambdaQuery(VisitBooking.class)
            .eq(VisitBooking::getOrgId, orgId)
            .eq(VisitBooking::getVisitDate, today)
            .eq(VisitBooking::getIsDeleted, 0)
            .orderByAsc(VisitBooking::getVisitTime));

    List<VisitBookingResponse> responses = new ArrayList<>();
    for (VisitBooking booking : bookings) {
      ElderProfile elder = elderMapper.selectById(booking.getElderId());
      FamilyUser familyUser = familyUserMapper.selectById(booking.getFamilyUserId());
      ElderFamily relation = resolveFamilyRelation(orgId, booking.getElderId(), booking.getFamilyUserId());
      responses.add(toResponse(booking, elder, familyUser, relation));
    }
    return responses;
  }

  @Override
  public List<VisitBookingResponse> listByFamily(Long orgId, Long familyUserId) {
    List<VisitBooking> bookings = bookingMapper.selectList(
        Wrappers.lambdaQuery(VisitBooking.class)
            .eq(VisitBooking::getOrgId, orgId)
            .eq(VisitBooking::getFamilyUserId, familyUserId)
            .eq(VisitBooking::getIsDeleted, 0)
            .orderByDesc(VisitBooking::getVisitTime));
    List<VisitBookingResponse> responses = new ArrayList<>();
    for (VisitBooking booking : bookings) {
      ElderProfile elder = elderMapper.selectById(booking.getElderId());
      FamilyUser familyUser = familyUserMapper.selectById(booking.getFamilyUserId());
      ElderFamily relation = resolveFamilyRelation(orgId, booking.getElderId(), booking.getFamilyUserId());
      responses.add(toResponse(booking, elder, familyUser, relation));
    }
    return responses;
  }

  @Override
  @Transactional
  public VisitCheckInResponse checkIn(VisitCheckInRequest request, Long guardStaffId) {
    if (guardStaffId == null) {
      throw new IllegalArgumentException("Guard staff required");
    }
    VisitBooking booking = resolveCheckInBooking(request);
    if (booking == null) {
      throw new IllegalArgumentException("Booking not found");
    }
    if (!Objects.equals(booking.getOrgId(), request.getOrgId()) || Integer.valueOf(1).equals(booking.getIsDeleted())) {
      throw new IllegalArgumentException("Booking not found");
    }

    if (booking.getStatus() == null || booking.getStatus() != STATUS_BOOKED) {
      throw new IllegalStateException("Booking is not awaiting check-in");
    }
    if (booking.getVisitDate() == null || !booking.getVisitDate().equals(LocalDate.now())) {
      throw new IllegalStateException("Only today's visits can be checked in");
    }
    if (request.getVisitCode() != null && !request.getVisitCode().isBlank()
        && !request.getVisitCode().trim().equalsIgnoreCase(booking.getVisitCode())) {
      throw new IllegalArgumentException("Visit code mismatch");
    }

    VisitCheckLog log = new VisitCheckLog();
    log.setOrgId(booking.getOrgId());
    log.setBookingId(booking.getId());
    log.setCheckTime(LocalDateTime.now());
    log.setStaffId(guardStaffId);
    log.setQrcodeToken(booking.getVisitCode());
    log.setResultStatus(1);
    log.setRemark(request.getRemark());
    checkLogMapper.insert(log);

    booking.setStatus(STATUS_CHECKED_IN);
    bookingMapper.updateById(booking);

    VisitCheckInResponse response = new VisitCheckInResponse();
    response.setBookingId(booking.getId());
    response.setGuardStaffId(guardStaffId);
    response.setCheckTime(log.getCheckTime());
    response.setStatus(booking.getStatus());
    return response;
  }

  @Override
  public VisitBookingResponse updateBooking(Long orgId, Long bookingId, VisitBookRequest request) {
    VisitBooking booking = bookingMapper.selectById(bookingId);
    if (booking == null || !Objects.equals(booking.getOrgId(), orgId) || Integer.valueOf(1).equals(booking.getIsDeleted())) {
      throw new IllegalArgumentException("Booking not found");
    }
    if (booking.getStatus() != null && booking.getStatus() == STATUS_CHECKED_IN) {
      throw new IllegalStateException("已登记到访记录不可编辑");
    }
    ElderProfile elder = elderMapper.selectById(request.getElderId());
    if (elder == null) {
      throw new IllegalArgumentException("Elder not found");
    }
    validateBookingRequest(request, elder, booking.getId());
    Long familyUserId = resolveFamilyUserId(orgId, request.getElderId(), request.getFamilyUserId());
    FamilyUser familyUser = familyUserMapper.selectById(familyUserId);
    ElderFamily relation = resolveFamilyRelation(orgId, request.getElderId(), familyUserId);
    booking.setElderId(request.getElderId());
    booking.setFamilyUserId(familyUserId);
    booking.setVisitorName(resolveVisitorName(request, familyUser));
    booking.setVisitorPhone(resolveVisitorPhone(request, familyUser));
    booking.setVisitorRelation(resolveVisitorRelation(request, relation));
    booking.setVisitTime(request.getVisitTime());
    booking.setVisitDate(request.getVisitTime().toLocalDate());
    booking.setVisitTimeSlot(request.getVisitTimeSlot());
    booking.setVisitorCount(request.getVisitorCount());
    booking.setCarPlate(request.getCarPlate());
    booking.setRemark(request.getRemark());
    bookingMapper.updateById(booking);
    return toResponse(booking, elder, familyUser, relation);
  }

  @Override
  public void deleteBooking(Long orgId, Long bookingId) {
    VisitBooking booking = bookingMapper.selectById(bookingId);
    if (booking == null || !Objects.equals(booking.getOrgId(), orgId) || Integer.valueOf(1).equals(booking.getIsDeleted())) {
      throw new IllegalArgumentException("Booking not found");
    }
    if (booking.getStatus() != null && booking.getStatus() == STATUS_CHECKED_IN) {
      throw new IllegalStateException("已登记到访记录不可删除");
    }
    booking.setStatus(STATUS_CANCELLED);
    booking.setRemark(appendRemark(booking.getRemark(), "预约已取消"));
    bookingMapper.updateById(booking);
  }

  @Override
  public VisitPrintTicketResponse buildPrintTicket(Long orgId, Long bookingId) {
    VisitBooking booking = bookingMapper.selectById(bookingId);
    if (booking == null || !Objects.equals(booking.getOrgId(), orgId) || Integer.valueOf(1).equals(booking.getIsDeleted())) {
      throw new IllegalArgumentException("Booking not found");
    }
    ElderProfile elder = elderMapper.selectById(booking.getElderId());
    FamilyUser familyUser = familyUserMapper.selectById(booking.getFamilyUserId());
    ElderFamily relation = resolveFamilyRelation(orgId, booking.getElderId(), booking.getFamilyUserId());
    VisitBookingResponse source = toResponse(booking, elder, familyUser, relation);

    VisitPrintTicketResponse response = new VisitPrintTicketResponse();
    response.setBookingId(booking.getId());
    response.setTicketNo("VST-" + booking.getId());
    response.setElderName(source.getElderName());
    response.setFamilyName(source.getFamilyName());
    response.setVisitorName(source.getVisitorName());
    response.setVisitorPhone(source.getVisitorPhone());
    response.setVisitorRelation(source.getVisitorRelation());
    response.setFloorNo(source.getFloorNo());
    response.setRoomNo(source.getRoomNo());
    response.setVisitTime(source.getVisitTime());
    response.setVisitorCount(source.getVisitorCount());
    response.setCarPlate(source.getCarPlate());
    response.setStatusText(switch (source.getStatus() == null ? -1 : source.getStatus()) {
      case STATUS_CHECKED_IN -> "已登记";
      case STATUS_CANCELLED -> "已取消";
      default -> "待登记";
    });
    response.setGeneratedAt(LocalDateTime.now());
    return response;
  }

  private String generateUniqueCode(Long orgId) {
    for (int i = 0; i < 5; i++) {
      String code = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
      Long count = bookingMapper.selectCount(Wrappers.lambdaQuery(VisitBooking.class)
          .eq(VisitBooking::getOrgId, orgId)
          .eq(VisitBooking::getVisitCode, code));
      if (count == null || count == 0) {
        return code;
      }
    }
    return UUID.randomUUID().toString().replace("-", "").toUpperCase();
  }

  private Long resolveFamilyUserId(Long orgId, Long elderId, Long requestFamilyUserId) {
    if (requestFamilyUserId != null && requestFamilyUserId > 0) {
      return requestFamilyUserId;
    }
    ElderFamily relation = elderFamilyMapper.selectOne(Wrappers.lambdaQuery(ElderFamily.class)
        .eq(ElderFamily::getOrgId, orgId)
        .eq(ElderFamily::getIsDeleted, 0)
        .eq(ElderFamily::getElderId, elderId)
        .orderByDesc(ElderFamily::getIsPrimary)
        .orderByDesc(ElderFamily::getUpdateTime)
        .last("LIMIT 1"));
    if (relation != null && relation.getFamilyUserId() != null) {
      return relation.getFamilyUserId();
    }
    return 0L;
  }

  private ElderFamily resolveFamilyRelation(Long orgId, Long elderId, Long familyUserId) {
    if (familyUserId == null || familyUserId <= 0 || elderId == null) {
      return null;
    }
    return elderFamilyMapper.selectOne(Wrappers.lambdaQuery(ElderFamily.class)
        .eq(ElderFamily::getOrgId, orgId)
        .eq(ElderFamily::getIsDeleted, 0)
        .eq(ElderFamily::getElderId, elderId)
        .eq(ElderFamily::getFamilyUserId, familyUserId)
        .orderByDesc(ElderFamily::getIsPrimary)
        .orderByDesc(ElderFamily::getUpdateTime)
        .last("LIMIT 1"));
  }

  private String resolveVisitorName(VisitBookRequest request, FamilyUser familyUser) {
    String visitorName = trimToNull(request.getVisitorName());
    if (visitorName != null) {
      return visitorName;
    }
    String familyName = trimToNull(familyUser == null ? null : familyUser.getRealName());
    if (familyName != null) {
      return familyName;
    }
    throw new IllegalArgumentException("来访人姓名不能为空");
  }

  private String resolveVisitorPhone(VisitBookRequest request, FamilyUser familyUser) {
    String visitorPhone = trimToNull(request.getVisitorPhone());
    if (visitorPhone != null) {
      return visitorPhone;
    }
    return trimToNull(familyUser == null ? null : familyUser.getPhone());
  }

  private String resolveVisitorRelation(VisitBookRequest request, ElderFamily relation) {
    String visitorRelation = trimToNull(request.getVisitorRelation());
    if (visitorRelation != null) {
      return visitorRelation;
    }
    return trimToNull(relation == null ? null : relation.getRelation());
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String normalized = value.trim();
    return normalized.isEmpty() ? null : normalized;
  }

  private VisitBookingResponse toResponse(VisitBooking booking, ElderProfile elder, FamilyUser familyUser, ElderFamily relation) {
    VisitBookingResponse response = new VisitBookingResponse();
    response.setId(booking.getId());
    response.setOrgId(booking.getOrgId());
    response.setElderId(booking.getElderId());
    response.setElderName(elder == null ? null : elder.getFullName());
    response.setFamilyUserId(booking.getFamilyUserId());
    response.setFamilyName(familyUser == null ? null : familyUser.getRealName());
    response.setVisitorName(trimToNull(booking.getVisitorName()) != null ? booking.getVisitorName() : response.getFamilyName());
    response.setVisitorPhone(trimToNull(booking.getVisitorPhone()) != null ? booking.getVisitorPhone() : (familyUser == null ? null : familyUser.getPhone()));
    response.setVisitorRelation(trimToNull(booking.getVisitorRelation()) != null ? booking.getVisitorRelation() : (relation == null ? null : relation.getRelation()));
    response.setVisitCode(booking.getVisitCode());
    response.setVerifyCode(booking.getVerifyCode());
    if (elder != null && elder.getBedId() != null) {
      Bed bed = bedMapper.selectById(elder.getBedId());
      if (bed != null && bed.getRoomId() != null) {
        Room room = roomMapper.selectById(bed.getRoomId());
        if (room != null) {
          response.setFloorNo(room.getFloorNo());
          response.setRoomNo(room.getRoomNo());
        }
      }
    }
    response.setVisitDate(booking.getVisitDate());
    response.setVisitTime(booking.getVisitTime());
    response.setVisitTimeSlot(booking.getVisitTimeSlot());
    response.setVisitorCount(booking.getVisitorCount());
    response.setCarPlate(booking.getCarPlate());
    response.setStatus(booking.getStatus());
    response.setRemark(booking.getRemark());
    return response;
  }

  private void validateBookingRequest(VisitBookRequest request, ElderProfile elder) {
    validateBookingRequest(request, elder, null);
  }

  private void validateBookingRequest(VisitBookRequest request, ElderProfile elder, Long excludeBookingId) {
    if (request == null || request.getVisitTime() == null) {
      throw new IllegalArgumentException("Visit time required");
    }
    if (!canReceiveVisit(elder)) {
      throw new IllegalStateException("当前老人状态不支持预约探视");
    }
    LocalDateTime now = LocalDateTime.now();
    if (request.getVisitTime().isBefore(now.plusMinutes(30))) {
      throw new IllegalArgumentException("预约时间需至少晚于当前30分钟");
    }
    if (request.getVisitTime().toLocalDate().isAfter(LocalDate.now().plusDays(MAX_ADVANCE_DAYS))) {
      throw new IllegalArgumentException("预约时间超出可预约窗口");
    }
    int visitorCount = request.getVisitorCount() == null ? 1 : request.getVisitorCount();
    if (visitorCount < 1 || visitorCount > MAX_VISITOR_COUNT) {
      throw new IllegalArgumentException("来访人数需在1到" + MAX_VISITOR_COUNT + "之间");
    }
    long duplicateCount = bookingMapper.selectCount(Wrappers.lambdaQuery(VisitBooking.class)
        .eq(VisitBooking::getIsDeleted, 0)
        .eq(VisitBooking::getOrgId, request.getOrgId())
        .eq(VisitBooking::getElderId, request.getElderId())
        .eq(VisitBooking::getVisitDate, request.getVisitTime().toLocalDate())
        .eq(VisitBooking::getVisitTimeSlot, request.getVisitTimeSlot())
        .ne(excludeBookingId != null, VisitBooking::getId, excludeBookingId)
        .in(VisitBooking::getStatus, STATUS_BOOKED, STATUS_CHECKED_IN));
    if (duplicateCount > 0) {
      throw new IllegalStateException("该老人当前时段已有有效探视预约");
    }
  }

  private VisitBooking resolveCheckInBooking(VisitCheckInRequest request) {
    if (request == null) {
      return null;
    }
    if (request.getBookingId() != null) {
      return bookingMapper.selectById(request.getBookingId());
    }
    if (request.getVisitCode() == null || request.getVisitCode().isBlank()) {
      return null;
    }
    return bookingMapper.selectOne(Wrappers.lambdaQuery(VisitBooking.class)
        .eq(VisitBooking::getOrgId, request.getOrgId())
        .eq(VisitBooking::getVisitCode, request.getVisitCode().trim())
        .eq(VisitBooking::getIsDeleted, 0)
        .last("LIMIT 1"));
  }

  private boolean canReceiveVisit(ElderProfile elder) {
    if (elder == null) {
      return false;
    }
    String lifecycleStatus = ElderLifecycleStatus.normalize(elder.getLifecycleStatus());
    if (lifecycleStatus == null) {
      return elder.getStatus() != null && elder.getStatus() == 1;
    }
    return ElderLifecycleStatus.IN_HOSPITAL.equals(lifecycleStatus);
  }

  private String appendRemark(String remark, String suffix) {
    if (suffix == null || suffix.isBlank()) {
      return remark;
    }
    if (remark == null || remark.isBlank()) {
      return suffix;
    }
    return remark + "；" + suffix;
  }
}
