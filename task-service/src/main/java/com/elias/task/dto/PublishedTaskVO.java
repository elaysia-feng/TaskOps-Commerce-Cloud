package com.elias.task.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PublishedTaskVO {
    private Long id;
    private String title;
    private String status;
    private BigDecimal rewardAmount;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
}

