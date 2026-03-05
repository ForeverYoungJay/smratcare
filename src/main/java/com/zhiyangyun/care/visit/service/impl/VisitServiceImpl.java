package com.zhiyangyun.care.visit.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.ElderFamily;
import com.zhiyangyun.care.elder.entity.FamilyUser;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.entity.Bed;
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

    VisitBooking booking = new VisitBooking();
    booking.setOrgId(request.getOrgId());
    booking.setElderId(request.getElderId());
    booking.setFamilyUserId(resolveFamilyUserId(request.getOrgId(), request.getElderId(), request.getFamilyUserId()));
    booking.setVisitTime(request.getVisitTime());
    booking.setVisitDate(request.getVisitTime().toLocalDate());
    booking.setVisitTimeSlot(request.getVisitTimeSlot());
    booking.setVisitorCount(request.getVisitorCount());
    booking.setCarPlate(request.getCarPlate());
    booking.setStatus(0);
    booking.setVerifyCode(generateUniqueCode(request.getOrgId()));
    booking.setVisitCode(generateUniqueCode(request.getOrgId()));
    booking.setRemark(request.getRemark());
    bookingMapper.insert(booking);

    FamilyUser familyUser = familyUserMapper.selectById(booking.getFamilyUserId());
    return toResponse(booking, elder, familyUser);
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
      responses.add(toResponse(booking, elder, familyUser));
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
      responses.add(toResponse(booking, elder, familyUser));
    }
    return responses;
  }

  @Override
  @Transactional
  public VisitCheckInResponse checkIn(VisitCheckInRequest request, Long guardStaffId) {
    if (guardStaffId == null) {
      throw new IllegalArgumentException("Guard staff required");
    }
    VisitBooking booking = null;
    if (request.getBookingId() != null) {
      booking = bookingMapper.selectById(request.getBookingId());
    }

    if (booking == null) {
      throw new IllegalArgumentException("Booking not found");
    }
    if (!Objects.equals(booking.getOrgId(), request.getOrgId()) || Integer.valueOf(1).equals(booking.getIsDeleted())) {
      throw new IllegalArgumentException("Booking not found");
    }

    VisitCheckLog log = new VisitCheckLog();
    log.setOrgId(booking.getOrgId());
    log.setBookingId(booking.getId());
    log.setCheckTime(LocalDateTime.now());
    log.setStaffId(guardStaffId);
    log.setQrcodeToken("DIRECT_CHECKIN");
    log.setResultStatus(1);
    log.setRemark(request.getRemark());
    checkLogMapper.insert(log);

    booking.setStatus(1);
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
    if (booking.getStatus() != null && booking.getStatus() == 1) {
      throw new IllegalStateException("已登记到访记录不可编辑");
    }
    ElderProfile elder = elderMapper.selectById(request.getElderId());
    if (elder == null) {
      throw new IllegalArgumentException("Elder not found");
    }
    booking.setElderId(request.getElderId());
    booking.setFamilyUserId(resolveFamilyUserId(orgId, request.getElderId(), request.getFamilyUserId()));
    booking.setVisitTime(request.getVisitTime());
    booking.setVisitDate(request.getVisitTime().toLocalDate());
    booking.setVisitTimeSlot(request.getVisitTimeSlot());
    booking.setVisitorCount(request.getVisitorCount());
    booking.setCarPlate(request.getCarPlate());
    booking.setRemark(request.getRemark());
    bookingMapper.updateById(booking);
    FamilyUser familyUser = familyUserMapper.selectById(booking.getFamilyUserId());
    return toResponse(booking, elder, familyUser);
  }

  @Override
  public void deleteBooking(Long orgId, Long bookingId) {
    VisitBooking booking = bookingMapper.selectById(bookingId);
    if (booking == null || !Objects.equals(booking.getOrgId(), orgId) || Integer.valueOf(1).equals(booking.getIsDeleted())) {
      throw new IllegalArgumentException("Booking not found");
    }
    if (booking.getStatus() != null && booking.getStatus() == 1) {
      throw new IllegalStateException("已登记到访记录不可删除");
    }
    booking.setIsDeleted(1);
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
    VisitBookingResponse source = toResponse(booking, elder, familyUser);

    VisitPrintTicketResponse response = new VisitPrintTicketResponse();
    response.setBookingId(booking.getId());
    response.setTicketNo("VST-" + booking.getId());
    response.setElderName(source.getElderName());
    response.setFamilyName(source.getFamilyName());
    response.setFloorNo(source.getFloorNo());
    response.setRoomNo(source.getRoomNo());
    response.setVisitTime(source.getVisitTime());
    response.setVisitorCount(source.getVisitorCount());
    response.setCarPlate(source.getCarPlate());
    response.setStatusText((source.getStatus() != null && source.getStatus() == 1) ? "已登记" : "待登记");
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

  private VisitBookingResponse toResponse(VisitBooking booking, ElderProfile elder, FamilyUser familyUser) {
    VisitBookingResponse response = new VisitBookingResponse();
    response.setId(booking.getId());
    response.setOrgId(booking.getOrgId());
    response.setElderId(booking.getElderId());
    response.setElderName(elder == null ? null : elder.getFullName());
    response.setFamilyUserId(booking.getFamilyUserId());
    response.setFamilyName(familyUser == null ? null : familyUser.getRealName());
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
}
