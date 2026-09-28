package com.library.smartlibrary.service;

import com.library.smartlibrary.dto.BorrowRequest;
import com.library.smartlibrary.dto.ReturnRequest;
import com.library.smartlibrary.entity.*;
import com.library.smartlibrary.exception.BadRequestException;
import com.library.smartlibrary.exception.ResourceNotFoundException;
import com.library.smartlibrary.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
public class BorrowService {

    @Autowired
    private BorrowRepository borrowRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private FineRepository fineRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private FineRuleService fineRuleService;

    @Autowired
    private LibraryTransactionService transactionService;

    @Autowired
    private AlertService alertService;

    public Borrow borrowBook(BorrowRequest request) {
        if (request.getMemberId() == null) {
            throw new BadRequestException("Member ID is required");
        }
        if (request.getBookId() == null) {
            throw new BadRequestException("Book ID is required");
        }

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + request.getMemberId()));

        if (!"ACTIVE".equalsIgnoreCase(member.getStatus())) {
            throw new BadRequestException("Member '" + member.getName() + "' has status " + member.getStatus() + 
                    " and is not allowed to borrow books. Please resolve account status first.");
        }

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + request.getBookId()));

        if ("MAINTENANCE".equalsIgnoreCase(book.getStatus())) {
            throw new BadRequestException("Book '" + book.getTitle() + "' is currently under maintenance and cannot be issued.");
        }

        if (book.getAvailableQuantity() == null || book.getAvailableQuantity() <= 0) {
            throw new BadRequestException("No copies of book '" + book.getTitle() + "' are currently available for borrowing.");
        }

        // Reduce available copies
        book.setAvailableQuantity(book.getAvailableQuantity() - 1);
        if (book.getAvailableQuantity() == 0) {
            book.setStatus("BORROWED");
        }
        bookRepository.save(book);

        LocalDate borrowDate = request.getBorrowDate() != null ? request.getBorrowDate() : LocalDate.now();
        LocalDate dueDate = request.getDueDate() != null ? request.getDueDate() : borrowDate.plusDays(14);

        Borrow borrow = new Borrow();
        borrow.setMember(member);
        borrow.setBook(book);
        borrow.setBorrowDate(borrowDate);
        borrow.setDueDate(dueDate);
        borrow.setStatus("BORROWED");

        Borrow saved = borrowRepository.save(borrow);

        // Record transaction
        transactionService.recordTransaction(
                "BOOK_ISSUED",
                member,
                book,
                "Book '" + book.getTitle() + "' issued to " + member.getName() + ". Due date: " + dueDate,
                "SUCCESS"
        );

        return saved;
    }

    public Borrow returnBook(Long borrowId, ReturnRequest request) {
        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow record not found with id: " + borrowId));

        if ("RETURNED".equalsIgnoreCase(borrow.getStatus())) {
            throw new BadRequestException("This borrowing record has already been marked as returned on " + borrow.getReturnDate());
        }

        LocalDate returnDate = (request != null && request.getReturnDate() != null) 
                ? request.getReturnDate() 
                : LocalDate.now();

        borrow.setReturnDate(returnDate);
        borrow.setStatus("RETURNED");

        Book book = borrow.getBook();
        Member member = borrow.getMember();

        // Automated Fine Calculation
        if (returnDate.isAfter(borrow.getDueDate())) {
            long overdueDays = ChronoUnit.DAYS.between(borrow.getDueDate(), returnDate);
            FineRule activeRule = fineRuleService.getActiveRule();

            long chargeableDays = Math.max(0, overdueDays - activeRule.getGracePeriod());
            double calculatedFine = chargeableDays * activeRule.getFinePerDay();
            if (activeRule.getMaximumFine() != null && calculatedFine > activeRule.getMaximumFine()) {
                calculatedFine = activeRule.getMaximumFine();
            }

            if (calculatedFine > 0) {
                Fine fine = new Fine();
                fine.setBorrow(borrow);
                fine.setMember(member);
                fine.setOverdueDays((int) overdueDays);
                fine.setFineAmount(calculatedFine);
                fine.setFineDate(returnDate);
                fine.setPaymentStatus("PENDING");
                fineRepository.save(fine);

                // Transaction for fine creation
                transactionService.recordTransaction(
                        "FINE_CREATED",
                        member,
                        book,
                        "Automatic fine of ₹" + calculatedFine + " generated for " + member.getName() + 
                                " (" + overdueDays + " overdue days at ₹" + activeRule.getFinePerDay() + "/day)",
                        "SUCCESS"
                );

                // Alert for pending fine
                alertService.createAlert(
                        member,
                        book,
                        "PENDING_FINE",
                        "Overdue fine of ₹" + calculatedFine + " incurred by " + member.getName() + 
                                " for returning '" + book.getTitle() + "' " + overdueDays + " days late."
                );
            }
        }

        // Restore book availability
        int newAvailable = (book.getAvailableQuantity() != null ? book.getAvailableQuantity() : 0) + 1;
        book.setAvailableQuantity(newAvailable);
        if ("BORROWED".equalsIgnoreCase(book.getStatus()) || newAvailable > 0) {
            book.setStatus("AVAILABLE");
        }
        bookRepository.save(book);

        Borrow updatedBorrow = borrowRepository.save(borrow);

        // Record return transaction
        transactionService.recordTransaction(
                "BOOK_RETURNED",
                member,
                book,
                "Book '" + book.getTitle() + "' returned by " + member.getName() + " on " + returnDate,
                "SUCCESS"
        );

        // Check for active reservations for this book
        List<Reservation> activeReservations = reservationRepository.findByBook_BookIdAndStatus(book.getBookId(), "ACTIVE");
        if (!activeReservations.isEmpty()) {
            for (Reservation res : activeReservations) {
                alertService.createAlert(
                        res.getMember(),
                        book,
                        "RESERVED_AVAILABLE",
                        "Reserved book '" + book.getTitle() + "' is now available for member " + res.getMember().getName() + "!"
                );
            }
        }

        return updatedBorrow;
    }

    @Transactional(readOnly = true)
    public List<Borrow> getAllBorrows() {
        return borrowRepository.findAllOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public Borrow getBorrowById(Long id) {
        return borrowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow record not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Borrow> getActiveBorrows() {
        return borrowRepository.findByStatus("BORROWED");
    }

    @Transactional(readOnly = true)
    public List<Borrow> getOverdueBorrows() {
        return borrowRepository.findOverdueBorrows(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Borrow> getBorrowsByMember(Long memberId) {
        return borrowRepository.findByMember_MemberId(memberId);
    }
}
