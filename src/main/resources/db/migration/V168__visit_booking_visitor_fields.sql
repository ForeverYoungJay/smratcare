ALTER TABLE visit_booking
  ADD COLUMN visitor_name VARCHAR(64) NULL COMMENT '来访人姓名' AFTER family_user_id,
  ADD COLUMN visitor_phone VARCHAR(32) NULL COMMENT '来访人电话' AFTER visitor_name,
  ADD COLUMN visitor_relation VARCHAR(64) NULL COMMENT '与长者关系' AFTER visitor_phone;
