package com.library.smartlibrary.config;

import com.library.smartlibrary.entity.*;
import com.library.smartlibrary.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BorrowRepository borrowRepository;

    @Autowired
    private FineRepository fineRepository;

    @Autowired
    private FineRuleRepository fineRuleRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private LibraryTransactionRepository transactionRepository;

    @Override
    public void run(String... args) throws Exception {
        // Initialize Default Fine Rule if none exists
        if (fineRuleRepository.count() == 0) {
            FineRule defaultRule = new FineRule(5.0, 0, 100.0, "ALL", "ACTIVE");
            fineRuleRepository.save(defaultRule);
        }

        // Initialize Books if empty
        if (bookRepository.count() == 0) {
            Book b1 = new Book("978-0132350884", "Clean Code", "Robert C. Martin", "Programming", 
                    "Prentice Hall", 2008, 5, 4, "CS-01", "AVAILABLE");
            Book b2 = new Book("978-0262033848", "Introduction to Algorithms", "Thomas H. Cormen", "Algorithms", 
                    "MIT Press", 2009, 4, 3, "CS-02", "AVAILABLE");
            Book b3 = new Book("978-0073523323", "Database System Concepts", "Abraham Silberschatz", "Database", 
                    "McGraw-Hill", 2010, 6, 5, "DB-01", "AVAILABLE");
            Book b4 = new Book("978-0132126953", "Computer Networks", "Andrew S. Tanenbaum", "Networking", 
                    "Pearson", 2011, 3, 2, "NW-01", "AVAILABLE");
            Book b5 = new Book("978-0136042594", "Artificial Intelligence: A Modern Approach", "Stuart Russell", "AI", 
                    "Pearson", 2010, 4, 3, "AI-01", "AVAILABLE");

            bookRepository.save(b1);
            bookRepository.save(b2);
            bookRepository.save(b3);
            bookRepository.save(b4);
            bookRepository.save(b5);

            // Initialize Members
            Member m1 = new Member("Arun", "arun@college.edu", "9876543210", "CSE", "Student", LocalDate.now().minusMonths(6), "ACTIVE");
            Member m2 = new Member("Priya", "priya@college.edu", "9876543211", "CSE", "Student", LocalDate.now().minusMonths(5), "ACTIVE");
            Member m3 = new Member("Karthik", "karthik@college.edu", "9876543212", "ECE", "Student", LocalDate.now().minusMonths(4), "ACTIVE");
            Member m4 = new Member("Divya", "divya@college.edu", "9876543213", "IT", "Student", LocalDate.now().minusMonths(3), "ACTIVE");
            Member m5 = new Member("Rahul", "rahul@college.edu", "9876543214", "CSE", "Student", LocalDate.now().minusMonths(2), "BLOCKED");

            memberRepository.save(m1);
            memberRepository.save(m2);
            memberRepository.save(m3);
            memberRepository.save(m4);
            memberRepository.save(m5);

            // Active Borrow (Non-Overdue)
            Borrow borrowActive = new Borrow(m1, b1, LocalDate.now().minusDays(3), LocalDate.now().plusDays(11));
            borrowRepository.save(borrowActive);

            // Overdue Borrow (Due 5 days ago)
            Borrow borrowOverdue = new Borrow(m3, b4, LocalDate.now().minusDays(19), LocalDate.now().minusDays(5));
            borrowRepository.save(borrowOverdue);

            // Past Borrow (Returned with fine)
            Borrow borrowReturned = new Borrow(m2, b3, LocalDate.now().minusDays(25), LocalDate.now().minusDays(15));
            borrowReturned.setReturnDate(LocalDate.now().minusDays(10));
            borrowReturned.setStatus("RETURNED");
            borrowRepository.save(borrowReturned);

            // Sample Pending Fine for overdue book
            Fine pendingFine = new Fine(borrowOverdue, m3, 5, 25.0, LocalDate.now(), "PENDING");
            fineRepository.save(pendingFine);

            // Sample Paid Fine for previous returned book
            Fine paidFine = new Fine(borrowReturned, m2, 5, 25.0, LocalDate.now().minusDays(10), "PAID");
            paidFine.setPaymentDate(LocalDate.now().minusDays(9));
            fineRepository.save(paidFine);

            // Sample Active Reservation
            Reservation res = new Reservation(m4, b2, LocalDate.now().minusDays(1), LocalDate.now().plusDays(6), "ACTIVE");
            reservationRepository.save(res);

            // Sample Maintenance
            Maintenance maint = new Maintenance(b5, "Binding and cover reinforcement", LocalDate.now().minusDays(2), 
                    LocalDate.now().plusDays(3), "UNDER_MAINTENANCE", "Book sent to Central Binder");
            maintenanceRepository.save(maint);

            // Sample Alerts
            Alert a1 = new Alert(m3, b4, "OVERDUE", "Book 'Computer Networks' issued to Karthik is overdue by 5 days!");
            Alert a2 = new Alert(m3, b4, "PENDING_FINE", "Pending fine of ₹25.00 due for Karthik.");
            Alert a3 = new Alert(m4, b2, "RETURN_REMINDER", "Reservation confirmed for 'Introduction to Algorithms' for Divya.");
            alertRepository.save(a1);
            alertRepository.save(a2);
            alertRepository.save(a3);

            // Sample Transactions
            transactionRepository.save(new LibraryTransaction("BOOK_ADDED", null, b1, "Clean Code added to library inventory", "SUCCESS"));
            transactionRepository.save(new LibraryTransaction("BOOK_ADDED", null, b2, "Introduction to Algorithms added to inventory", "SUCCESS"));
            transactionRepository.save(new LibraryTransaction("BOOK_ISSUED", m1, b1, "Clean Code issued to Arun", "SUCCESS"));
            transactionRepository.save(new LibraryTransaction("BOOK_ISSUED", m3, b4, "Computer Networks issued to Karthik", "SUCCESS"));
            transactionRepository.save(new LibraryTransaction("FINE_CREATED", m3, b4, "Overdue fine generated for Karthik (₹25.00)", "SUCCESS"));
            transactionRepository.save(new LibraryTransaction("BOOK_RESERVED", m4, b2, "Introduction to Algorithms reserved by Divya", "SUCCESS"));
            transactionRepository.save(new LibraryTransaction("MAINTENANCE_STARTED", null, b5, "Artificial Intelligence sent for maintenance", "SUCCESS"));
        }
    }
}
