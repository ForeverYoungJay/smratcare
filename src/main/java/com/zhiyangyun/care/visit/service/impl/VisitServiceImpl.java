package com.zhiyangyun.care.visit.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.visit.entity.VisitBooking;
import com.zhiyangyun.care.visit.entity.VisitCheckLog;
import com.zhiyangyun.care.visit.mapper.VisitBookingMapper;
import com.zhiyangyun.care.visit.mapper.VisitCheckLogMapper;
import com.zhiyangyun.care.visit.model.VisitBookRequest;
import com.zhiyangyun.care.visit.model.VisitBookingResponse;
import com.zhiyangyun.care.visit.model.VisitCheckInRequest;
import com.zhiyangyun.care.visit.model.VisitCheckInResponse;
import com.zhiyangyun.care.visit.service.VisitService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VisitServiceImpl implements VisitService {
  private final VisitBookingMapper bookingMapper;
  private final VisitCheckLogMapper checkLogMapper;
  private final ElderMapper elderMapper;

  public VisitServiceImpl(VisitBookingMapper bookingMapper,
      VisitCheckLogMapper checkLogMapper,
      ElderMapper elderMapper) {
    this.bookingMapper = bookingMapper;
    this.checkLogMapper = checkLogMapper;
    this.elderMapper = elderMapper;
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
    booking.setFamilyUserId(request.getFamilyUserId());
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

    return toResponse(booking, elder.getFullName());
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
      responses.add(toResponse(booking, elder == null ? null : elder.getFullName()));
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
      responses.add(toResponse(booking, elder == null ? null : elder.getFullName()));
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
    } else if (request.getVisitCode() != null && !request.getVisitCode().isBlank()) {
      booking = bookingMapper.selectOne(Wrappers.lambdaQuery(VisitBooking.class)
          .eq(VisitBooking::getOrgId, request.getOrgId())
          .eq(VisitBooking::getVisitCode, request.getVisitCode())
          .eq(VisitBooking::getIsDeleted, 0));
    }

    if (booking == null) {
      throw new IllegalArgumentException("Booking not found");
    }

    VisitCheckLog log = new VisitCheckLog();
    log.setOrgId(booking.getOrgId());
    log.setBookingId(booking.getId());
    log.setCheckTime(LocalDateTime.now());
    log.setStaffId(guardStaffId);
    log.setQrcodeToken(request.getVisitCode() == null ? booking.getVisitCode() : request.getVisitCode());
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

  private VisitBookingResponse toResponse(VisitBooking booking, String elderName) {
    VisitBookingResponse response = new VisitBookingResponse();
    response.setId(booking.getId());
    response.setOrgId(booking.getOrgId());
    response.setElderId(booking.getElderId());
    response.setElderName(elderName);
    response.setFamilyUserId(booking.getFamilyUserId());
    response.setVisitDate(booking.getVisitDate());
    response.setVisitTime(booking.getVisitTime());
    response.setVisitTimeSlot(booking.getVisitTimeSlot());
    response.setVisitorCount(booking.getVisitorCount());
    response.setCarPlate(booking.getCarPlate());
    response.setStatus(booking.getStatus());
    response.setVisitCode(booking.getVisitCode());
    response.setRemark(booking.getRemark());
    return response;
  }
}
