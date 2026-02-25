-- M3 照护：服务计划自动生成预定扩展

ALTER TABLE service_plan
  ADD COLUMN preferred_time TIME DEFAULT '09:00:00' COMMENT '默认预约时间' AFTER end_date;

CREATE INDEX idx_service_plan_cycle ON service_plan (cycle_type, start_date, end_date);
CREATE INDEX idx_service_booking_plan_time ON service_booking (plan_id, booking_time);
