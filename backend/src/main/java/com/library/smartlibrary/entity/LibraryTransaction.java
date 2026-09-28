package com.library.smartlibrary.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "library_transaction")
public class LibraryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "transaction_type", nullable = false)
    private String transactionType; 
    // BOOK_ISSUED, BOOK_RETURNED, FINE_CREATED, FINE_PAID, BOOK_RESERVED, 
    // BOOK_ADDED, BOOK_UPDATED, BOOK_DELETED, MAINTENANCE_STARTED, MAINTENANCE_COMPLETED

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id")
    private Book book;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "status", nullable = false)
    private String status; // SUCCESS, FAILED, PENDING

    public LibraryTransaction() {
        this.transactionDate = LocalDateTime.now();
        this.status = "SUCCESS";
    }

    public LibraryTransaction(String transactionType, Member member, Book book, String description, String status) {
        this.transactionType = transactionType;
        this.member = member;
        this.book = book;
        this.description = description;
        this.transactionDate = LocalDateTime.now();
        this.status = status != null ? status : "SUCCESS";
    }

    // Getters and Setters
    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
