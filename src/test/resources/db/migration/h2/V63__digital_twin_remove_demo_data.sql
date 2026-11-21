-- 数字孪生不再使用演示数据：删掉 V298 生成的 10 栋示例占位楼栋，园区与场景不再标为概念示意。
-- 真实楼栋由接口按楼栋主数据自动接入（GF-BLD-B<building.id>）。
DELETE FROM dt_business_binding WHERE asset_code LIKE 'GF-BLD-TMP-%';
DELETE FROM dt_spatial_asset WHERE asset_code LIKE 'GF-BLD-TMP-%';
UPDATE dt_spatial_asset SET name = '园区', verification_status = 'MASTER_DATA', source_reference = '机构园区'
WHERE asset_code = 'GF-CAMPUS-001';
UPDATE dt_scene_version SET verification_status = 'UNVERIFIED', version = '0.1.0' WHERE version = '0.1.0-concept';
