-- 修复并补齐入住房型默认项，确保单/双/三人间与功能房类型可用

UPDATE base_data_item
SET item_code = 'ROOM_SINGLE',
    item_name = '单人间',
    status = 1,
    sort_no = 10,
    is_deleted = 0,
    update_time = NOW()
WHERE config_group = 'ADMISSION_ROOM_TYPE'
  AND (item_code = 'ROOM_SINGLE' OR item_name = '单人间');

UPDATE base_data_item
SET item_code = 'ROOM_DOUBLE',
    item_name = '双人间',
    status = 1,
    sort_no = 20,
    is_deleted = 0,
    update_time = NOW()
WHERE config_group = 'ADMISSION_ROOM_TYPE'
  AND (item_code = 'ROOM_DOUBLE' OR item_name = '双人间');

UPDATE base_data_item
SET item_code = 'ROOM_TRIPLE',
    item_name = '三人间',
    status = 1,
    sort_no = 30,
    is_deleted = 0,
    update_time = NOW()
WHERE config_group = 'ADMISSION_ROOM_TYPE'
  AND (item_code = 'ROOM_TRIPLE' OR item_name = '三人间');

UPDATE base_data_item
SET item_code = 'ROOM_NURSING_STATION',
    item_name = '护理站',
    status = 1,
    sort_no = 50,
    is_deleted = 0,
    update_time = NOW()
WHERE config_group = 'ADMISSION_ROOM_TYPE'
  AND (item_code = 'ROOM_NURSING_STATION' OR item_name = '护理站');

UPDATE base_data_item
SET item_code = 'ROOM_WATER',
    item_name = '开水房',
    status = 1,
    sort_no = 60,
    is_deleted = 0,
    update_time = NOW()
WHERE config_group = 'ADMISSION_ROOM_TYPE'
  AND (item_code = 'ROOM_WATER' OR item_name = '开水房');

UPDATE base_data_item
SET item_code = 'ROOM_LAUNDRY',
    item_name = '洗衣房',
    status = 1,
    sort_no = 70,
    is_deleted = 0,
    update_time = NOW()
WHERE config_group = 'ADMISSION_ROOM_TYPE'
  AND (item_code = 'ROOM_LAUNDRY' OR item_name = '洗衣房');

UPDATE base_data_item
SET item_code = 'ROOM_TOILET',
    item_name = '卫生间',
    status = 1,
    sort_no = 80,
    is_deleted = 0,
    update_time = NOW()
WHERE config_group = 'ADMISSION_ROOM_TYPE'
  AND (item_code = 'ROOM_TOILET' OR item_name = '卫生间');

INSERT INTO base_data_item (
  id,
  tenant_id,
  org_id,
  config_group,
  item_code,
  item_name,
  status,
  sort_no,
  remark,
  created_by,
  create_time,
  update_time,
  is_deleted
)
SELECT UUID_SHORT(),
       org.id,
       org.id,
       'ADMISSION_ROOM_TYPE',
       preset.item_code,
       preset.item_name,
       1,
       preset.sort_no,
       NULL,
       NULL,
       NOW(),
       NOW(),
       0
FROM org
JOIN (
  SELECT 'ROOM_SINGLE' AS item_code, '单人间' AS item_name, 10 AS sort_no
  UNION ALL SELECT 'ROOM_DOUBLE', '双人间', 20
  UNION ALL SELECT 'ROOM_TRIPLE', '三人间', 30
  UNION ALL SELECT 'ROOM_NURSING_STATION', '护理站', 50
  UNION ALL SELECT 'ROOM_WATER', '开水房', 60
  UNION ALL SELECT 'ROOM_LAUNDRY', '洗衣房', 70
  UNION ALL SELECT 'ROOM_TOILET', '卫生间', 80
) preset
LEFT JOIN base_data_item existing_code
  ON existing_code.tenant_id = org.id
 AND existing_code.config_group = 'ADMISSION_ROOM_TYPE'
 AND existing_code.item_code = preset.item_code
LEFT JOIN base_data_item existing_name
  ON existing_name.tenant_id = org.id
 AND existing_name.config_group = 'ADMISSION_ROOM_TYPE'
 AND existing_name.item_name = preset.item_name
WHERE org.is_deleted = 0
  AND existing_code.id IS NULL
  AND existing_name.id IS NULL;
