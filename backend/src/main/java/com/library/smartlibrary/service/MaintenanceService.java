package com.library.smartlibrary.service;

import com.library.smartlibrary.entity.Book;
import com.library.smartlibrary.entity.Maintenance;
import com.library.smartlibrary.exception.BadRequestException;
import com.library.smartlibrary.exception.ResourceNotFoundException;
import com.library.smartlibrary.repository.BookRepository;
import com.library.smartlibrary.repository.MaintenanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class MaintenanceService {

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LibraryTransactionService transactionService;

    @Autowired
    private AlertService alertService;

    @Transactional(readOnly = true)
    public List<Maintenance> getAllMaintenance() {
        return maintenanceRepository.findAllOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public Maintenance getMaintenanceById(Long id) {
        return maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance record not found with id: " + id));
    }

    public Maintenance startMaintenance(Long bookId, String reason, LocalDate expectedDate, String remarks) {
        if (bookId == null) {
            throw new BadRequestException("Book ID is required");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new BadRequestException("Maintenance reason is required");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        if (book.getAvailableQuantity() == null || book.getAvailableQuantity() <= 0) {
            throw new BadRequestException("No available copies of '" + book.getTitle() + "' to send to maintenance");
        }

        // Reduce available copies
        book.setAvailableQuantity(book.getAvailableQuantity() - 1);
        if (book.getAvailableQuantity() == 0) {
            book.setStatus("MAINTENANCE");
        }
        bookRepository.save(book);

        Maintenance m = new Maintenance();
        m.setBook(book);
        m.setReason(reason.trim());
        m.setStartDate(LocalDate.now());
        m.setExpectedCompletionDate(expectedDate != null ? expectedDate : LocalDate.now().plusDays(5));
        m.setStatus("UNDER_MAINTENANCE");
        m.setRemarks(remarks);

        Maintenance saved = maintenanceRepository.save(m);

        transactionService.recordTransaction(
                "MAINTENANCE_STARTED",
                null,
                book,
                "Book '" + book.getTitle() + "' sent for maintenance: " + reason,
                "SUCCESS"
        );

        return saved;
    }

    public Maintenance completeMaintenance(Long maintenanceId, String remarks) {
        Maintenance m = getMaintenanceById(maintenanceId);

        if (!"UNDER_MAINTENANCE".equalsIgnoreCase(m.getStatus())) {
            throw new BadRequestException("Maintenance record is already " + m.getStatus());
        }

        m.setStatus("COMPLETED");
        m.setActualCompletionDate(LocalDate.now());
        if (remarks != null && !remarks.trim().isEmpty()) {
            m.setRemarks((m.getRemarks() != null ? m.getRemarks() + " | " : "") + remarks.trim());
        }

        // Restore copy
        Book book = m.getBook();
        int newAvailable = (book.getAvailableQuantity() != null ? book.getAvailableQuantity() : 0) + 1;
        book.setAvailableQuantity(newAvailable);
        if ("MAINTENANCE".equalsIgnoreCase(book.getStatus()) || newAvailable > 0) {
            book.setStatus("AVAILABLE");
        }
        bookRepository.save(book);

        Maintenance saved = maintenanceRepository.save(m);

        transactionService.recordTransaction(
                "MAINTENANCE_COMPLETED",
                null,
                book,
                "Maintenance completed for '" + book.getTitle() + "'. Book restored to inventory.",
                "SUCCESS"
        );

        alertService.createAlert(
                null,
                book,
                "MAINTENANCE_COMPLETED",
                "Maintenance completed for '" + book.getTitle() + "'. Available quantity: " + newAvailable
        );

        return saved;
    }
}
