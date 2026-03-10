package com.elias.common.mq.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRejectedEvent {
    private String messageId;
    private Long taskId;
    private Long submissionId;
    private Long publisherId;
    private Long acceptorId;
    private Integer roundNo;
    private String reason;
    private LocalDateTime reviewedAt;
}