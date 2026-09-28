package com.library.smartlibrary.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alert")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_id")
    private Long alertId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id")
    private Book book;

    @Column(name = "alert_type", nullable = false)
    private String alertType; // OVERDUE, PENDING_FINE, RETURN_REMINDER, RESERVED_AVAILABLE, MAINTENANCE_COMPLETED, LOW_AVAILABILITY

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "read_status", nullable = false)
    private String readStatus; // UNREAD, READ

    public Alert() {
        this.createdDate = LocalDateTime.now();
        this.readStatus = "UNREAD";
    }

    public Alert(Member member, Book book, String alertType, String message) {
        this.member = member;
        this.book = book;
        this.alertType = alertType;
        this.message = message;
        this.createdDate = LocalDateTime.now();
        this.readStatus = "UNREAD";
    }

    // Getters and Setters
    public Long getAlertId() {
        return alertId;
    }

    public void setAlertId(Long alertId) {
        this.alertId = alertId;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(String readStatus) {
        this.readStatus = readStatus;
    }
}
