package com.library.smartlibrary.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "department")
    private String department;

    @Column(name = "membership_type")
    private String membershipType; // Student, Faculty, Staff

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Column(name = "status", nullable = false)
    private String status; // ACTIVE, INACTIVE, BLOCKED

    public Member() {
        this.registrationDate = LocalDate.now();
        this.status = "ACTIVE";
    }

    public Member(String name, String email, String phone, String department, 
                  String membershipType, LocalDate registrationDate, String status) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.membershipType = membershipType;
        this.registrationDate = registrationDate != null ? registrationDate : LocalDate.now();
        this.status = status != null ? status : "ACTIVE";
    }

    // Getters and Setters
    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getMembershipType() {
        return membershipType;
    }

    public void setMembershipType(String membershipType) {
        this.membershipType = membershipType;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
