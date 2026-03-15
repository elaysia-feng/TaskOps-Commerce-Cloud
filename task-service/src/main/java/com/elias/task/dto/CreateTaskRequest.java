package com.elias.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(name = "CreateTaskRequest", description = "创建接单任务请求")
public class CreateTaskRequest {

    @NotBlank
    @Schema(description = "任务标题", example = "帮忙取快递送宿舍")
    private String title;

    @NotBlank
    @Schema(description = "任务描述", example = "今天17:30前从东区菜鸟驿站取件后送到3号宿舍楼。")
    private String description;

    @Schema(description = "任务分类", example = "ERRAND")
    private String category;

    @Schema(description = "任务标签，多个用英文逗号分隔", example = "跑腿,快递,校园")
    private String tags;

    @Schema(description = "任务地点", example = "东区菜鸟驿站")
    private String location;

    @Schema(description = "联系方式", example = "微信:test")
    private String contactInfo;

    @NotNull
    @DecimalMin(value = "0.01")
    @Schema(description = "任务赏金", example = "18.80")
    private BigDecimal rewardAmount;

    @DecimalMin(value = "0.00")
    @Schema(description = "平台服务费", example = "0.80")
    private BigDecimal serviceFee;

    @NotNull
    @Min(1)
    @Max(5)
    @Schema(description = "优先级，1最高，5最低", example = "3")
    private Integer priority;

    @Schema(description = "截止时间", example = "2026-03-10T18:40:00")
    private LocalDateTime deadline;

    @Schema(description = "是否要求提交凭证", example = "false")
    private Boolean proofRequired;
}