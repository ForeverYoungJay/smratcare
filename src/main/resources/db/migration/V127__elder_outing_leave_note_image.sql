ALTER TABLE elder_outing_record
  ADD COLUMN leave_note_image_url VARCHAR(255) DEFAULT NULL COMMENT '请假条照片URL' AFTER reason;
