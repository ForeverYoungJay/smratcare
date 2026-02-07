package com.zhiyangyun.care.visit.service;

import com.zhiyangyun.care.visit.model.VisitBookRequest;
import com.zhiyangyun.care.visit.model.VisitBookingResponse;
import com.zhiyangyun.care.visit.model.VisitCheckInRequest;
import com.zhiyangyun.care.visit.model.VisitCheckInResponse;
import java.util.List;

public interface VisitService {
  VisitBookingResponse book(VisitBookRequest request);

  List<VisitBookingResponse> listByFamily(Long orgId, Long familyUserId);

  List<VisitBookingResponse> todayList(Long orgId);

  VisitCheckInResponse checkIn(VisitCheckInRequest request, Long guardStaffId);
}
