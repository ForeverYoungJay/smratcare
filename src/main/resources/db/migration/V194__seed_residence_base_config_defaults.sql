INSERT INTO base_data_item (id, tenant_id, org_id, config_group, item_code, item_name, status, sort_no, remark, created_by, create_time, update_time, is_deleted)
SELECT UUID_SHORT(), o.id, o.id, 'ADMISSION_ROOM_TYPE', 'ROOM_SINGLE', '单人间', 1, 10, 'Flyway补齐入住配置默认项', NULL, NOW(), NOW(), 0
FROM org o
WHERE o.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1
    FROM base_data_item b
    WHERE b.tenant_id = o.id
      AND b.config_group = 'ADMISSION_ROOM_TYPE'
      AND b.item_code = 'ROOM_SINGLE'
      AND b.is_deleted = 0
  );

INSERT INTO base_data_item (id, tenant_id, org_id, config_group, item_code, item_name, status, sort_no, remark, created_by, create_time, update_time, is_deleted)
SELECT UUID_SHORT(), o.id, o.id, 'ADMISSION_ROOM_TYPE', 'ROOM_DOUBLE', '双人间', 1, 20, 'Flyway补齐入住配置默认项', NULL, NOW(), NOW(), 0
FROM org o
WHERE o.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1
    FROM base_data_item b
    WHERE b.tenant_id = o.id
      AND b.config_group = 'ADMISSION_ROOM_TYPE'
      AND b.item_code = 'ROOM_DOUBLE'
      AND b.is_deleted = 0
  );

INSERT INTO base_data_item (id, tenant_id, org_id, config_group, item_code, item_name, status, sort_no, remark, created_by, create_time, update_time, is_deleted)
SELECT UUID_SHORT(), o.id, o.id, 'ADMISSION_ROOM_TYPE', 'ROOM_TRIPLE', '三人间', 1, 30, 'Flyway补齐入住配置默认项', NULL, NOW(), NOW(), 0
FROM org o
WHERE o.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1
    FROM base_data_item b
    WHERE b.tenant_id = o.id
      AND b.config_group = 'ADMISSION_ROOM_TYPE'
      AND b.item_code = 'ROOM_TRIPLE'
      AND b.is_deleted = 0
  );

INSERT INTO base_data_item (id, tenant_id, org_id, config_group, item_code, item_name, status, sort_no, remark, created_by, create_time, update_time, is_deleted)
SELECT UUID_SHORT(), o.id, o.id, 'ADMISSION_ROOM_TYPE', 'ROOM_NURSING_STATION', '护理站', 1, 40, 'Flyway补齐入住配置默认项', NULL, NOW(), NOW(), 0
FROM org o
WHERE o.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1
    FROM base_data_item b
    WHERE b.tenant_id = o.id
      AND b.config_group = 'ADMISSION_ROOM_TYPE'
      AND b.item_code = 'ROOM_NURSING_STATION'
      AND b.is_deleted = 0
  );

INSERT INTO base_data_item (id, tenant_id, org_id, config_group, item_code, item_name, status, sort_no, remark, created_by, create_time, update_time, is_deleted)
SELECT UUID_SHORT(), o.id, o.id, 'ADMISSION_ROOM_TYPE', 'ROOM_WATER', '开水房', 1, 50, 'Flyway补齐入住配置默认项', NULL, NOW(), NOW(), 0
FROM org o
WHERE o.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1
    FROM base_data_item b
    WHERE b.tenant_id = o.id
      AND b.config_group = 'ADMISSION_ROOM_TYPE'
      AND b.item_code = 'ROOM_WATER'
      AND b.is_deleted = 0
  );

INSERT INTO base_data_item (id, tenant_id, org_id, config_group, item_code, item_name, status, sort_no, remark, created_by, create_time, update_time, is_deleted)
SELECT UUID_SHORT(), o.id, o.id, 'ADMISSION_ROOM_TYPE', 'ROOM_LAUNDRY', '洗衣房', 1, 60, 'Flyway补齐入住配置默认项', NULL, NOW(), NOW(), 0
FROM org o
WHERE o.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1
    FROM base_data_item b
    WHERE b.tenant_id = o.id
      AND b.config_group = 'ADMISSION_ROOM_TYPE'
      AND b.item_code = 'ROOM_LAUNDRY'
      AND b.is_deleted = 0
  );

INSERT INTO base_data_item (id, tenant_id, org_id, config_group, item_code, item_name, status, sort_no, remark, created_by, create_time, update_time, is_deleted)
SELECT UUID_SHORT(), o.id, o.id, 'ADMISSION_ROOM_TYPE', 'ROOM_TOILET', '卫生间', 1, 70, 'Flyway补齐入住配置默认项', NULL, NOW(), NOW(), 0
FROM org o
WHERE o.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1
    FROM base_data_item b
    WHERE b.tenant_id = o.id
      AND b.config_group = 'ADMISSION_ROOM_TYPE'
      AND b.item_code = 'ROOM_TOILET'
      AND b.is_deleted = 0
  );

INSERT INTO base_data_item (id, tenant_id, org_id, config_group, item_code, item_name, status, sort_no, remark, created_by, create_time, update_time, is_deleted)
SELECT UUID_SHORT(), o.id, o.id, 'ADMISSION_BED_TYPE', 'BED_STANDARD', '标准床', 1, 10, 'Flyway补齐入住配置默认项', NULL, NOW(), NOW(), 0
FROM org o
WHERE o.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1
    FROM base_data_item b
    WHERE b.tenant_id = o.id
      AND b.config_group = 'ADMISSION_BED_TYPE'
      AND b.item_code = 'BED_STANDARD'
      AND b.is_deleted = 0
  );

INSERT INTO base_data_item (id, tenant_id, org_id, config_group, item_code, item_name, status, sort_no, remark, created_by, create_time, update_time, is_deleted)
SELECT UUID_SHORT(), o.id, o.id, 'ADMISSION_BED_TYPE', 'BED_CARE', '护理床', 1, 20, 'Flyway补齐入住配置默认项', NULL, NOW(), NOW(), 0
FROM org o
WHERE o.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1
    FROM base_data_item b
    WHERE b.tenant_id = o.id
      AND b.config_group = 'ADMISSION_BED_TYPE'
      AND b.item_code = 'BED_CARE'
      AND b.is_deleted = 0
  );

INSERT INTO base_data_item (id, tenant_id, org_id, config_group, item_code, item_name, status, sort_no, remark, created_by, create_time, update_time, is_deleted)
SELECT UUID_SHORT(), o.id, o.id, 'ADMISSION_AREA', 'AREA_A', 'A区', 1, 10, 'Flyway补齐入住配置默认项', NULL, NOW(), NOW(), 0
FROM org o
WHERE o.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1
    FROM base_data_item b
    WHERE b.tenant_id = o.id
      AND b.config_group = 'ADMISSION_AREA'
      AND b.item_code = 'AREA_A'
      AND b.is_deleted = 0
  );

INSERT INTO base_data_item (id, tenant_id, org_id, config_group, item_code, item_name, status, sort_no, remark, created_by, create_time, update_time, is_deleted)
SELECT UUID_SHORT(), o.id, o.id, 'ADMISSION_AREA', 'AREA_B', 'B区', 1, 20, 'Flyway补齐入住配置默认项', NULL, NOW(), NOW(), 0
FROM org o
WHERE o.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1
    FROM base_data_item b
    WHERE b.tenant_id = o.id
      AND b.config_group = 'ADMISSION_AREA'
      AND b.item_code = 'AREA_B'
      AND b.is_deleted = 0
  );
