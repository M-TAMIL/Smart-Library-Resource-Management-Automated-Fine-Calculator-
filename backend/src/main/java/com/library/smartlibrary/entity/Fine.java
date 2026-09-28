package com.library.smartlibrary.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "fine")
public class Fine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fine_id")
    private Long fineId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "borrow_id", nullable = false)
    private Borrow borrow;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "overdue_days", nullable = false)
    private Integer overdueDays;

    @Column(name = "fine_amount", nullable = false)
    private Double fineAmount;

    @Column(name = "fine_date", nullable = false)
    private LocalDate fineDate;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "payment_status", nullable = false)
    private String paymentStatus; // PENDING, PAID

    public Fine() {
        this.fineDate = LocalDate.now();
        this.paymentStatus = "PENDING";
    }

    public Fine(Borrow borrow, Member member, Integer overdueDays, Double fineAmount, 
                LocalDate fineDate, String paymentStatus) {
        this.borrow = borrow;
        this.member = member;
        this.overdueDays = overdueDays;
        this.fineAmount = fineAmount;
        this.fineDate = fineDate != null ? fineDate : LocalDate.now();
        this.paymentStatus = paymentStatus != null ? paymentStatus : "PENDING";
    }

    // Getters and Setters
    public Long getFineId() {
        return fineId;
    }

    public void setFineId(Long fineId) {
        this.fineId = fineId;
    }

    public Borrow getBorrow() {
        return borrow;
    }

    public void setBorrow(Borrow borrow) {
        this.borrow = borrow;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Integer getOverdueDays() {
        return overdueDays;
    }

    public void setOverdueDays(Integer overdueDays) {
        this.overdueDays = overdueDays;
    }

    public Double getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(Double fineAmount) {
        this.fineAmount = fineAmount;
    }

    public LocalDate getFineDate() {
        return fineDate;
    }

    public void setFineDate(LocalDate fineDate) {
        this.fineDate = fineDate;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
