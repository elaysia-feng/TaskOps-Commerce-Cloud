package com.elias.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(name = "CreateTaskRequest", description = "创建接单任务请求")
public class CreateTaskRequest {

    @NotBlank
    @Schema(description = "任务标题", example = "校园跑腿代取快递")
    private String title;

    @NotBlank
    @Schema(description = "任务描述", example = "今天18:00前帮我从菜鸟驿站取快递并送到宿舍")
    private String description;

    @Schema(description = "原技术栈字段，兼容旧前端输入，现映射为标签", example = "跑腿,校园,快递")
    private String techStack;

    @Min(1)
    @Max(5)
    @Schema(description = "优先级，1-5，数值越小优先级越高", example = "3")
    private Integer priority;

    @Schema(description = "任务分类", example = "ERRAND")
    private String category;

    @DecimalMin(value = "0.00")
    @Schema(description = "任务赏金", example = "12.50")
    private BigDecimal rewardAmount;

    @DecimalMin(value = "0.00")
    @Schema(description = "平台服务费", example = "0.50")
    private BigDecimal serviceFee;

    @Schema(description = "任务地点", example = "东区菜鸟驿站")
    private String location;

    @Schema(description = "联系方式", example = "微信: taskops001")
    private String contactInfo;

    @Schema(description = "是否要求提交凭证", example = "true")
    private Boolean proofRequired;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "截止时间", example = "2026-03-10 18:00:00")
    private LocalDateTime deadline;
}