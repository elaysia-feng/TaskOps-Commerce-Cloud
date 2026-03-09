package com.elias.points.domin.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_points_ledger")
public class PointsLedgerPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String bizNo;
    private Long userId;
    private String changeType;
    private String sourceType;
    private Integer points;
    private Integer balanceAfter;
    private String remark;
    private LocalDateTime createdAt;
}
