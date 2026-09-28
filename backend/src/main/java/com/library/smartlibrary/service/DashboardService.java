package com.library.smartlibrary.service;

import com.library.smartlibrary.dto.DashboardSummaryDTO;
import com.library.smartlibrary.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BorrowRepository borrowRepository;

    @Autowired
    private FineRepository fineRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    @Autowired
    private LibraryTransactionRepository transactionRepository;

    @Autowired
    private AlertRepository alertRepository;

    public DashboardSummaryDTO getSummary() {
        DashboardSummaryDTO summary = new DashboardSummaryDTO();

        Long totalCopies = bookRepository.sumTotalCopies();
        summary.setTotalBooks(totalCopies != null ? totalCopies : 0L);

        Long availableCopies = bookRepository.sumAvailableCopies();
        summary.setAvailableBooks(availableCopies != null ? availableCopies : 0L);

        summary.setBorrowedBooks(borrowRepository.countByStatus("BORROWED"));
        summary.setTotalMembers(memberRepository.count());
        summary.setActiveReservations(reservationRepository.countByStatus("ACTIVE"));
        summary.setPendingFines(fineRepository.countByPaymentStatus("PENDING"));

        Double pendingAmount = fineRepository.sumFineAmountByPaymentStatus("PENDING");
        summary.setTotalPendingFineAmount(pendingAmount != null ? pendingAmount : 0.0);

        summary.setOverdueBooks(borrowRepository.countOverdueBorrows(LocalDate.now()));
        summary.setBooksUnderMaintenance(maintenanceRepository.countByStatus("UNDER_MAINTENANCE"));

        summary.setRecentTransactions(transactionRepository.findTop10ByOrderByTransactionDateDesc());
        summary.setRecentAlerts(alertRepository.findTop10ByOrderByCreatedDateDesc());

        return summary;
    }
}
