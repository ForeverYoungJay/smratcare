-- 长护险失能等级评估模板（对齐《长期护理失能等级评估标准（试行）》2021-08-03）
-- 3 个一级指标（日常生活活动能力/认知能力/感知觉与沟通）+ 17 个二级指标，组合法判级 0-5。
CREATE TABLE IF NOT EXISTS ltci_assess_template (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NULL,
  template_code VARCHAR(64) NOT NULL,
  template_name VARCHAR(150) NOT NULL,
  standard_version VARCHAR(64) NOT NULL DEFAULT 'NATIONAL_2021',
  indicators_json JSON NOT NULL,
  combine_rule_json JSON NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(500) NULL,
  created_by BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_ltci_tpl_code (org_id, template_code, is_deleted),
  KEY idx_ltci_tpl_std (standard_version, status)
);

-- 内置国家标准模板（org_id 为 NULL 表示全局默认模板，各机构可复制后定制）
INSERT INTO ltci_assess_template
  (id, tenant_id, org_id, template_code, template_name, standard_version, indicators_json, combine_rule_json, status, remark)
SELECT 1, NULL, NULL, 'NATIONAL_2021', '国家长期护理失能等级评估标准（试行）', 'NATIONAL_2021',
  CAST('{
    "dimensions": [
      {"code": "ADL", "name": "日常生活活动能力", "scoreType": "BARTHEL",
       "indicators": [
         {"code": "adl_feeding", "name": "进食", "max": 10},
         {"code": "adl_bathing", "name": "洗澡", "max": 5},
         {"code": "adl_grooming", "name": "修饰", "max": 5},
         {"code": "adl_dressing", "name": "穿衣", "max": 10},
         {"code": "adl_bowel", "name": "大便控制", "max": 10},
         {"code": "adl_bladder", "name": "小便控制", "max": 10},
         {"code": "adl_toilet", "name": "如厕", "max": 10},
         {"code": "adl_transfer", "name": "床椅转移", "max": 15},
         {"code": "adl_walking", "name": "平地行走", "max": 15},
         {"code": "adl_stairs", "name": "上下楼梯", "max": 10}
       ]},
      {"code": "COG", "name": "认知能力", "scoreType": "ORDINAL_0_2",
       "indicators": [
         {"code": "cog_orientation_time", "name": "时间与空间定向", "max": 2},
         {"code": "cog_orientation_person", "name": "人物定向", "max": 2},
         {"code": "cog_memory", "name": "记忆(近事)", "max": 2}
       ]},
      {"code": "PER", "name": "感知觉与沟通", "scoreType": "ORDINAL_0_2",
       "indicators": [
         {"code": "per_consciousness", "name": "意识水平", "max": 2},
         {"code": "per_vision", "name": "视力", "max": 2},
         {"code": "per_hearing", "name": "听力", "max": 2},
         {"code": "per_communication", "name": "沟通(理解与表达)", "max": 2}
       ]}
    ]
  }' AS JSON),
  CAST('{
    "method": "COMBINATION",
    "primary": "ADL",
    "bands": [
      {"min": 90, "max": 100.0001, "level": 0, "label": "能力完好"},
      {"min": 70, "max": 90,       "level": 1, "label": "轻度失能"},
      {"min": 50, "max": 70,       "level": 2, "label": "中度失能"},
      {"min": 30, "max": 50,       "level": 3, "label": "中度失能"},
      {"min": 10, "max": 30,       "level": 4, "label": "重度失能"},
      {"min": 0,  "max": 10,       "level": 5, "label": "重度失能"}
    ],
    "escalation": {"cognitiveSevereBelow": 30, "perceptionSevereBelow": 30, "maxLevel": 5, "step": 1},
    "levelLabels": {"0": "能力完好", "1": "轻度失能", "2": "中度失能", "3": "中度失能", "4": "重度失能", "5": "重度失能"}
  }' AS JSON),
  1, '系统内置国家标准模板，禁止直接删除；机构定制请复制新模板'
WHERE NOT EXISTS (SELECT 1 FROM ltci_assess_template WHERE id = 1);
