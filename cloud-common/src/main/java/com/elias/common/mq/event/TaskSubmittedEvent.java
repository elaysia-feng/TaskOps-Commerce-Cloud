package com.elias.common.mq.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskSubmittedEvent {
    private String messageId;
    private Long taskId;
    private Long submissionId;
    private Long publisherId;
    private Long acceptorId;
    private Integer roundNo;
    private String content;
    private String proofUrls;
    private LocalDateTime submittedAt;
}