package com.elias.points.domin.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_points_rule")
public class PointsRulePO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String ruleCode;
    private String sourceType;
    private String skuCode;
    private Integer points;
    private Integer enabled;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
