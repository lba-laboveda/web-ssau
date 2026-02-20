package ru.ssau.todo.dto;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.AssertTrue;

public class TaskFilterDto {
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime from;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime to;
    
    private Long userId;

    public TaskFilterDto() {}

    public TaskFilterDto(LocalDateTime from, LocalDateTime to, Long userId) {
        this.from = from;
        this.to = to;
        this.userId = userId;
    }

    @AssertTrue(message = "Start date cannot be after end date")
    public boolean isDateRangeValid() {
        if (from == null || to == null) {
            return true;
        }
        return !from.isAfter(to);
    }

    public LocalDateTime getFrom() { return from; }
    public void setFrom(LocalDateTime from) { this.from = from; }

    public LocalDateTime getTo() { return to; }
    public void setTo(LocalDateTime to) { this.to = to; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}