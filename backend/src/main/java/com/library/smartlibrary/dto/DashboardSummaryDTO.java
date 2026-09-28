package com.library.smartlibrary.dto;

import com.library.smartlibrary.entity.Alert;
import com.library.smartlibrary.entity.LibraryTransaction;

import java.util.List;

public class DashboardSummaryDTO {
    private long totalBooks;
    private long availableBooks;
    private long borrowedBooks;
    private long totalMembers;
    private long activeReservations;
    private long pendingFines;
    private double totalPendingFineAmount;
    private long overdueBooks;
    private long booksUnderMaintenance;
    private List<LibraryTransaction> recentTransactions;
    private List<Alert> recentAlerts;

    public DashboardSummaryDTO() {}

    public long getTotalBooks() {
        return totalBooks;
    }

    public void setTotalBooks(long totalBooks) {
        this.totalBooks = totalBooks;
    }

    public long getAvailableBooks() {
        return availableBooks;
    }

    public void setAvailableBooks(long availableBooks) {
        this.availableBooks = availableBooks;
    }

    public long getBorrowedBooks() {
        return borrowedBooks;
    }

    public void setBorrowedBooks(long borrowedBooks) {
        this.borrowedBooks = borrowedBooks;
    }

    public long getTotalMembers() {
        return totalMembers;
    }

    public void setTotalMembers(long totalMembers) {
        this.totalMembers = totalMembers;
    }

    public long getActiveReservations() {
        return activeReservations;
    }

    public void setActiveReservations(long activeReservations) {
        this.activeReservations = activeReservations;
    }

    public long getPendingFines() {
        return pendingFines;
    }

    public void setPendingFines(long pendingFines) {
        this.pendingFines = pendingFines;
    }

    public double getTotalPendingFineAmount() {
        return totalPendingFineAmount;
    }

    public void setTotalPendingFineAmount(double totalPendingFineAmount) {
        this.totalPendingFineAmount = totalPendingFineAmount;
    }

    public long getOverdueBooks() {
        return overdueBooks;
    }

    public void setOverdueBooks(long overdueBooks) {
        this.overdueBooks = overdueBooks;
    }

    public long getBooksUnderMaintenance() {
        return booksUnderMaintenance;
    }

    public void setBooksUnderMaintenance(long booksUnderMaintenance) {
        this.booksUnderMaintenance = booksUnderMaintenance;
    }

    public List<LibraryTransaction> getRecentTransactions() {
        return recentTransactions;
    }

    public void setRecentTransactions(List<LibraryTransaction> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }

    public List<Alert> getRecentAlerts() {
        return recentAlerts;
    }

    public void setRecentAlerts(List<Alert> recentAlerts) {
        this.recentAlerts = recentAlerts;
    }
}
