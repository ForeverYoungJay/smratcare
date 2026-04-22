-- 补齐入住床位类型与房间类型默认项

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
       preset.config_group,
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
  SELECT 'ADMISSION_BED_TYPE' AS config_group, 'BED_STANDARD' AS item_code, '标准床' AS item_name, 10 AS sort_no
  UNION ALL SELECT 'ADMISSION_BED_TYPE', 'BED_CARE', '护理床', 20
  UNION ALL SELECT 'ADMISSION_BED_TYPE', 'BED_ELECTRIC', '电动护理床', 30
  UNION ALL SELECT 'ADMISSION_BED_TYPE', 'BED_ANTI_DECUBITUS', '防压疮床', 40
  UNION ALL SELECT 'ADMISSION_BED_TYPE', 'BED_MEDICAL', '医用床', 50
  UNION ALL SELECT 'ADMISSION_ROOM_TYPE', 'ROOM_SINGLE', '单人间', 10
  UNION ALL SELECT 'ADMISSION_ROOM_TYPE', 'ROOM_DOUBLE', '双人间', 20
  UNION ALL SELECT 'ADMISSION_ROOM_TYPE', 'ROOM_TRIPLE', '三人间', 30
  UNION ALL SELECT 'ADMISSION_ROOM_TYPE', 'ROOM_CARE', '护理房', 40
  UNION ALL SELECT 'ADMISSION_ROOM_TYPE', 'ROOM_SUITE', '套间', 45
  UNION ALL SELECT 'ADMISSION_ROOM_TYPE', 'ROOM_NURSING_STATION', '护理站', 50
  UNION ALL SELECT 'ADMISSION_ROOM_TYPE', 'ROOM_WATER', '开水房', 60
  UNION ALL SELECT 'ADMISSION_ROOM_TYPE', 'ROOM_LAUNDRY', '洗衣房', 70
  UNION ALL SELECT 'ADMISSION_ROOM_TYPE', 'ROOM_TOILET', '卫生间', 80
  UNION ALL SELECT 'ADMISSION_ROOM_TYPE', 'ROOM_BATH', '浴室', 90
  UNION ALL SELECT 'ADMISSION_ROOM_TYPE', 'ROOM_TREATMENT', '治疗室', 100
  UNION ALL SELECT 'ADMISSION_ROOM_TYPE', 'ROOM_STORAGE', '库房', 110
  UNION ALL SELECT 'ADMISSION_ROOM_TYPE', 'ROOM_ACTIVITY', '活动室', 120
  UNION ALL SELECT 'ADMISSION_ROOM_TYPE', 'ROOM_DINING', '餐厅', 130
) preset
LEFT JOIN base_data_item existing_code
  ON existing_code.tenant_id = org.id
 AND existing_code.config_group = preset.config_group
 AND existing_code.item_code = preset.item_code
LEFT JOIN base_data_item existing_name
  ON existing_name.tenant_id = org.id
 AND existing_name.config_group = preset.config_group
 AND existing_name.item_name = preset.item_name
WHERE org.is_deleted = 0
  AND existing_code.id IS NULL
  AND existing_name.id IS NULL;

UPDATE base_data_item
SET item_name = CASE item_code
      WHEN 'BED_STANDARD' THEN '标准床'
      WHEN 'BED_CARE' THEN '护理床'
      WHEN 'BED_ELECTRIC' THEN '电动护理床'
      WHEN 'BED_ANTI_DECUBITUS' THEN '防压疮床'
      WHEN 'BED_MEDICAL' THEN '医用床'
      WHEN 'ROOM_SINGLE' THEN '单人间'
      WHEN 'ROOM_DOUBLE' THEN '双人间'
      WHEN 'ROOM_TRIPLE' THEN '三人间'
      WHEN 'ROOM_CARE' THEN '护理房'
      WHEN 'ROOM_SUITE' THEN '套间'
      WHEN 'ROOM_NURSING_STATION' THEN '护理站'
      WHEN 'ROOM_WATER' THEN '开水房'
      WHEN 'ROOM_LAUNDRY' THEN '洗衣房'
      WHEN 'ROOM_TOILET' THEN '卫生间'
      WHEN 'ROOM_BATH' THEN '浴室'
      WHEN 'ROOM_TREATMENT' THEN '治疗室'
      WHEN 'ROOM_STORAGE' THEN '库房'
      WHEN 'ROOM_ACTIVITY' THEN '活动室'
      WHEN 'ROOM_DINING' THEN '餐厅'
      ELSE item_name
    END,
    status = 1,
    is_deleted = 0,
    sort_no = CASE item_code
      WHEN 'BED_STANDARD' THEN 10
      WHEN 'BED_CARE' THEN 20
      WHEN 'BED_ELECTRIC' THEN 30
      WHEN 'BED_ANTI_DECUBITUS' THEN 40
      WHEN 'BED_MEDICAL' THEN 50
      WHEN 'ROOM_SINGLE' THEN 10
      WHEN 'ROOM_DOUBLE' THEN 20
      WHEN 'ROOM_TRIPLE' THEN 30
      WHEN 'ROOM_CARE' THEN 40
      WHEN 'ROOM_SUITE' THEN 45
      WHEN 'ROOM_NURSING_STATION' THEN 50
      WHEN 'ROOM_WATER' THEN 60
      WHEN 'ROOM_LAUNDRY' THEN 70
      WHEN 'ROOM_TOILET' THEN 80
      WHEN 'ROOM_BATH' THEN 90
      WHEN 'ROOM_TREATMENT' THEN 100
      WHEN 'ROOM_STORAGE' THEN 110
      WHEN 'ROOM_ACTIVITY' THEN 120
      WHEN 'ROOM_DINING' THEN 130
      ELSE sort_no
    END,
    update_time = NOW()
WHERE config_group IN ('ADMISSION_BED_TYPE', 'ADMISSION_ROOM_TYPE')
  AND item_code IN (
    'BED_STANDARD',
    'BED_CARE',
    'BED_ELECTRIC',
    'BED_ANTI_DECUBITUS',
    'BED_MEDICAL',
    'ROOM_SINGLE',
    'ROOM_DOUBLE',
    'ROOM_TRIPLE',
    'ROOM_CARE',
    'ROOM_SUITE',
    'ROOM_NURSING_STATION',
    'ROOM_WATER',
    'ROOM_LAUNDRY',
    'ROOM_TOILET',
    'ROOM_BATH',
    'ROOM_TREATMENT',
    'ROOM_STORAGE',
    'ROOM_ACTIVITY',
    'ROOM_DINING'
  );
