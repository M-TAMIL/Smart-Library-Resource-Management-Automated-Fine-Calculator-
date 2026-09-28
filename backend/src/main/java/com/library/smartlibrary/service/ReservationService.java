package com.library.smartlibrary.service;

import com.library.smartlibrary.entity.Book;
import com.library.smartlibrary.entity.Member;
import com.library.smartlibrary.entity.Reservation;
import com.library.smartlibrary.exception.BadRequestException;
import com.library.smartlibrary.exception.ResourceNotFoundException;
import com.library.smartlibrary.repository.BookRepository;
import com.library.smartlibrary.repository.MemberRepository;
import com.library.smartlibrary.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LibraryTransactionService transactionService;

    @Autowired
    private AlertService alertService;

    @Transactional(readOnly = true)
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAllOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public List<Reservation> getActiveReservations() {
        return reservationRepository.findByStatus("ACTIVE");
    }

    public Reservation createReservation(Long memberId, Long bookId) {
        if (memberId == null || bookId == null) {
            throw new BadRequestException("Member ID and Book ID are required to reserve a book");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));

        if (!"ACTIVE".equalsIgnoreCase(member.getStatus())) {
            throw new BadRequestException("Member " + member.getName() + " is not active and cannot place reservations");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        // Check if member already has active reservation for this book
        List<Reservation> existing = reservationRepository.findByBook_BookIdAndStatus(bookId, "ACTIVE");
        boolean alreadyReserved = existing.stream().anyMatch(r -> r.getMember().getMemberId().equals(memberId));
        if (alreadyReserved) {
            throw new BadRequestException("Member already has an active reservation for book '" + book.getTitle() + "'");
        }

        Reservation res = new Reservation();
        res.setMember(member);
        res.setBook(book);
        res.setReservationDate(LocalDate.now());
        res.setExpiryDate(LocalDate.now().plusDays(7));
        res.setStatus("ACTIVE");

        Reservation saved = reservationRepository.save(res);

        transactionService.recordTransaction(
                "BOOK_RESERVED",
                member,
                book,
                "Book '" + book.getTitle() + "' reserved by " + member.getName() + " (Hold until: " + res.getExpiryDate() + ")",
                "SUCCESS"
        );

        alertService.createAlert(
                member,
                book,
                "RETURN_REMINDER",
                "Reservation confirmed for '" + book.getTitle() + "'. You will be notified when it becomes available."
        );

        return saved;
    }

    public Reservation cancelReservation(Long reservationId) {
        Reservation res = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + reservationId));

        res.setStatus("CANCELLED");
        Reservation saved = reservationRepository.save(res);

        transactionService.recordTransaction(
                "RESERVATION_CANCELLED",
                res.getMember(),
                res.getBook(),
                "Reservation ID " + reservationId + " cancelled for book '" + res.getBook().getTitle() + "'",
                "SUCCESS"
        );

        return saved;
    }

    public Reservation completeReservation(Long reservationId) {
        Reservation res = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + reservationId));

        res.setStatus("COMPLETED");
        Reservation saved = reservationRepository.save(res);

        transactionService.recordTransaction(
                "RESERVATION_COMPLETED",
                res.getMember(),
                res.getBook(),
                "Reservation ID " + reservationId + " completed (book picked up)",
                "SUCCESS"
        );

        return saved;
    }
}
