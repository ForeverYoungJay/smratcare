-- 将历史上仍沿用普通房号的功能房，统一重命名到功能房专属号段

DROP TEMPORARY TABLE IF EXISTS tmp_functional_room_rename_candidates;
CREATE TEMPORARY TABLE tmp_functional_room_rename_candidates AS
SELECT
  candidate.room_id,
  candidate.tenant_id,
  candidate.floor_id,
  candidate.target_prefix,
  COALESCE(existing.max_seq, 0) AS base_seq,
  ROW_NUMBER() OVER (
    PARTITION BY candidate.tenant_id, candidate.floor_id, candidate.func_code
    ORDER BY candidate.current_room_no, candidate.room_id
  ) AS seq_offset
FROM (
  SELECT
    r.id AS room_id,
    r.tenant_id,
    r.floor_id,
    r.room_no AS current_room_no,
    CONCAT(
      CASE
        WHEN b.code IS NOT NULL AND TRIM(b.code) <> '' THEN UPPER(REGEXP_REPLACE(TRIM(b.code), '[^A-Za-z0-9]', ''))
        WHEN b.name IS NOT NULL AND REGEXP_REPLACE(TRIM(b.name), '[^A-Za-z0-9]', '') <> '' THEN UPPER(LEFT(REGEXP_REPLACE(TRIM(b.name), '[^A-Za-z0-9]', ''), 1))
        ELSE 'A'
      END,
      CASE
        WHEN REGEXP_REPLACE(COALESCE(NULLIF(TRIM(r.floor_no), ''), '1'), '[^0-9]', '') <> '' THEN REGEXP_REPLACE(COALESCE(NULLIF(TRIM(r.floor_no), ''), '1'), '[^0-9]', '')
        ELSE '1'
      END,
      CASE
        WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%NURSING%' OR UPPER(COALESCE(r.room_type, '')) LIKE '%STATION%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%护理站%' THEN 'N'
        WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%WATER%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%开水房%' THEN 'W'
        WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%LAUNDRY%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%洗衣房%' THEN 'L'
        WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%TOILET%' OR UPPER(COALESCE(r.room_type, '')) LIKE '%WC%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%卫生间%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%厕所%' THEN 'T'
        WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%BATH%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%浴室%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%沐浴%' THEN 'B'
        WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%TREATMENT%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%治疗室%' THEN 'Z'
        WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%STORAGE%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%库房%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%储物%' THEN 'K'
        WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%ACTIVITY%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%活动室%' THEN 'H'
        WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%DINING%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%餐厅%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%餐饮%' THEN 'C'
        ELSE NULL
      END
    ) AS target_prefix,
    CASE
      WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%NURSING%' OR UPPER(COALESCE(r.room_type, '')) LIKE '%STATION%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%护理站%' THEN 'N'
      WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%WATER%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%开水房%' THEN 'W'
      WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%LAUNDRY%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%洗衣房%' THEN 'L'
      WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%TOILET%' OR UPPER(COALESCE(r.room_type, '')) LIKE '%WC%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%卫生间%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%厕所%' THEN 'T'
      WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%BATH%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%浴室%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%沐浴%' THEN 'B'
      WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%TREATMENT%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%治疗室%' THEN 'Z'
      WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%STORAGE%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%库房%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%储物%' THEN 'K'
      WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%ACTIVITY%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%活动室%' THEN 'H'
      WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%DINING%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%餐厅%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%餐饮%' THEN 'C'
      ELSE NULL
    END AS func_code
  FROM room r
  LEFT JOIN building b ON b.id = r.building_id
  WHERE r.is_deleted = 0
    AND EXISTS (
      SELECT 1
      FROM base_data_item item
      WHERE item.is_deleted = 0
        AND item.tenant_id = r.tenant_id
        AND item.config_group = 'ADMISSION_ROOM_TYPE'
        AND (
          CONVERT(item.item_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(r.room_type USING utf8mb4) COLLATE utf8mb4_unicode_ci
          OR CONVERT(item.item_name USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(r.room_type USING utf8mb4) COLLATE utf8mb4_unicode_ci
        )
        AND (
          (JSON_VALID(item.remark) = 1 AND CONVERT(JSON_UNQUOTE(JSON_EXTRACT(item.remark, '$.roomKind')) USING utf8mb4) COLLATE utf8mb4_unicode_ci = 'FUNCTIONAL')
          OR (JSON_VALID(item.remark) = 1 AND JSON_EXTRACT(item.remark, '$.defaultCapacity') = 0)
        )
    )
) candidate
LEFT JOIN (
  SELECT
    room_seq.tenant_id,
    room_seq.floor_id,
    room_seq.func_code,
    MAX(room_seq.seq_no) AS max_seq
  FROM (
    SELECT
      r.tenant_id,
      r.floor_id,
      CASE
        WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%NURSING%' OR UPPER(COALESCE(r.room_type, '')) LIKE '%STATION%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%护理站%' THEN 'N'
        WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%WATER%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%开水房%' THEN 'W'
        WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%LAUNDRY%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%洗衣房%' THEN 'L'
        WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%TOILET%' OR UPPER(COALESCE(r.room_type, '')) LIKE '%WC%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%卫生间%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%厕所%' THEN 'T'
        WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%BATH%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%浴室%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%沐浴%' THEN 'B'
        WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%TREATMENT%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%治疗室%' THEN 'Z'
        WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%STORAGE%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%库房%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%储物%' THEN 'K'
        WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%ACTIVITY%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%活动室%' THEN 'H'
        WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%DINING%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%餐厅%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%餐饮%' THEN 'C'
        ELSE NULL
      END AS func_code,
      CAST(
        SUBSTRING(
          UPPER(TRIM(r.room_no)),
          LENGTH(
            CONCAT(
              CASE
                WHEN b.code IS NOT NULL AND TRIM(b.code) <> '' THEN UPPER(REGEXP_REPLACE(TRIM(b.code), '[^A-Za-z0-9]', ''))
                WHEN b.name IS NOT NULL AND REGEXP_REPLACE(TRIM(b.name), '[^A-Za-z0-9]', '') <> '' THEN UPPER(LEFT(REGEXP_REPLACE(TRIM(b.name), '[^A-Za-z0-9]', ''), 1))
                ELSE 'A'
              END,
              CASE
                WHEN REGEXP_REPLACE(COALESCE(NULLIF(TRIM(r.floor_no), ''), '1'), '[^0-9]', '') <> '' THEN REGEXP_REPLACE(COALESCE(NULLIF(TRIM(r.floor_no), ''), '1'), '[^0-9]', '')
                ELSE '1'
              END,
              CASE
                WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%NURSING%' OR UPPER(COALESCE(r.room_type, '')) LIKE '%STATION%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%护理站%' THEN 'N'
                WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%WATER%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%开水房%' THEN 'W'
                WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%LAUNDRY%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%洗衣房%' THEN 'L'
                WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%TOILET%' OR UPPER(COALESCE(r.room_type, '')) LIKE '%WC%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%卫生间%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%厕所%' THEN 'T'
                WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%BATH%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%浴室%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%沐浴%' THEN 'B'
                WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%TREATMENT%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%治疗室%' THEN 'Z'
                WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%STORAGE%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%库房%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%储物%' THEN 'K'
                WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%ACTIVITY%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%活动室%' THEN 'H'
                WHEN UPPER(COALESCE(r.room_type, '')) LIKE '%DINING%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%餐厅%' OR CONVERT(COALESCE(r.room_type, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE '%餐饮%' THEN 'C'
                ELSE ''
              END
            )
          ) + 1
        ) AS UNSIGNED
      ) AS seq_no
    FROM room r
    LEFT JOIN building b ON b.id = r.building_id
    WHERE r.is_deleted = 0
      AND TRIM(COALESCE(r.room_no, '')) <> ''
  ) room_seq
  WHERE room_seq.func_code IS NOT NULL
    AND room_seq.seq_no IS NOT NULL
  GROUP BY room_seq.tenant_id, room_seq.floor_id, room_seq.func_code
) existing
  ON existing.tenant_id = candidate.tenant_id
 AND existing.floor_id = candidate.floor_id
 AND existing.func_code = candidate.func_code
WHERE candidate.func_code IS NOT NULL
  AND candidate.target_prefix IS NOT NULL
  AND (
    candidate.current_room_no IS NULL
    OR TRIM(candidate.current_room_no) = ''
    OR UPPER(TRIM(candidate.current_room_no)) NOT REGEXP CONCAT('^', candidate.target_prefix, '[0-9]+$')
  );

UPDATE room r
JOIN tmp_functional_room_rename_candidates candidate
  ON candidate.room_id = r.id
SET r.room_no = CONCAT(candidate.target_prefix, candidate.base_seq + candidate.seq_offset),
    r.update_time = NOW();

DROP TEMPORARY TABLE IF EXISTS tmp_functional_room_rename_candidates;
