package com.library.smartlibrary.dto;

import java.time.LocalDate;

public class MaintenanceRequest {
    private Long bookId;
    private String reason;
    private LocalDate expectedCompletionDate;
    private String remarks;

    public MaintenanceRequest() {}

    public MaintenanceRequest(Long bookId, String reason, LocalDate expectedCompletionDate, String remarks) {
        this.bookId = bookId;
        this.reason = reason;
        this.expectedCompletionDate = expectedCompletionDate;
        this.remarks = remarks;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDate getExpectedCompletionDate() {
        return expectedCompletionDate;
    }

    public void setExpectedCompletionDate(LocalDate expectedCompletionDate) {
        this.expectedCompletionDate = expectedCompletionDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
