package com.library.smartlibrary.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "fine_rule")
public class FineRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id")
    private Long ruleId;

    @Column(name = "fine_per_day", nullable = false)
    private Double finePerDay;

    @Column(name = "grace_period", nullable = false)
    private Integer gracePeriod;

    @Column(name = "maximum_fine", nullable = false)
    private Double maximumFine;

    @Column(name = "membership_type")
    private String membershipType; // ALL, Student, Faculty, Staff

    @Column(name = "status", nullable = false)
    private String status; // ACTIVE, INACTIVE

    public FineRule() {
        this.finePerDay = 5.0;
        this.gracePeriod = 0;
        this.maximumFine = 100.0;
        this.membershipType = "ALL";
        this.status = "ACTIVE";
    }

    public FineRule(Double finePerDay, Integer gracePeriod, Double maximumFine, String membershipType, String status) {
        this.finePerDay = finePerDay;
        this.gracePeriod = gracePeriod;
        this.maximumFine = maximumFine;
        this.membershipType = membershipType != null ? membershipType : "ALL";
        this.status = status != null ? status : "ACTIVE";
    }

    // Getters and Setters
    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public Double getFinePerDay() {
        return finePerDay;
    }

    public void setFinePerDay(Double finePerDay) {
        this.finePerDay = finePerDay;
    }

    public Integer getGracePeriod() {
        return gracePeriod;
    }

    public void setGracePeriod(Integer gracePeriod) {
        this.gracePeriod = gracePeriod;
    }

    public Double getMaximumFine() {
        return maximumFine;
    }

    public void setMaximumFine(Double maximumFine) {
        this.maximumFine = maximumFine;
    }

    public String getMembershipType() {
        return membershipType;
    }

    public void setMembershipType(String membershipType) {
        this.membershipType = membershipType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
