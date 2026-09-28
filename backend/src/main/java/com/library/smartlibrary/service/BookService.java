package com.library.smartlibrary.service;

import com.library.smartlibrary.entity.Book;
import com.library.smartlibrary.entity.Borrow;
import com.library.smartlibrary.exception.BadRequestException;
import com.library.smartlibrary.exception.ResourceNotFoundException;
import com.library.smartlibrary.repository.BookRepository;
import com.library.smartlibrary.repository.BorrowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BorrowRepository borrowRepository;

    @Autowired
    private LibraryTransactionService transactionService;

    @Transactional(readOnly = true)
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
    }

    public Book addBook(Book book) {
        if (book.getIsbn() == null || book.getIsbn().trim().isEmpty()) {
            throw new BadRequestException("ISBN is required");
        }
        if (bookRepository.findByIsbn(book.getIsbn().trim()).isPresent()) {
            throw new BadRequestException("Book with ISBN " + book.getIsbn() + " already exists");
        }
        if (book.getQuantity() == null || book.getQuantity() < 1) {
            throw new BadRequestException("Book quantity must be at least 1");
        }

        if (book.getAvailableQuantity() == null) {
            book.setAvailableQuantity(book.getQuantity());
        }
        if (book.getStatus() == null || book.getStatus().trim().isEmpty()) {
            book.setStatus("AVAILABLE");
        }

        Book saved = bookRepository.save(book);
        transactionService.recordTransaction(
                "BOOK_ADDED",
                null,
                saved,
                "New book added: '" + saved.getTitle() + "' (ISBN: " + saved.getIsbn() + ", Qty: " + saved.getQuantity() + ")",
                "SUCCESS"
        );
        return saved;
    }

    public Book updateBook(Long id, Book updated) {
        Book existing = getBookById(id);

        if (updated.getIsbn() != null && !updated.getIsbn().trim().equalsIgnoreCase(existing.getIsbn())) {
            bookRepository.findByIsbn(updated.getIsbn().trim()).ifPresent(b -> {
                if (!b.getBookId().equals(id)) {
                    throw new BadRequestException("Another book with ISBN " + updated.getIsbn() + " already exists");
                }
            });
            existing.setIsbn(updated.getIsbn().trim());
        }

        if (updated.getTitle() != null) existing.setTitle(updated.getTitle());
        if (updated.getAuthor() != null) existing.setAuthor(updated.getAuthor());
        if (updated.getCategory() != null) existing.setCategory(updated.getCategory());
        if (updated.getPublisher() != null) existing.setPublisher(updated.getPublisher());
        if (updated.getPublicationYear() != null) existing.setPublicationYear(updated.getPublicationYear());
        if (updated.getShelfNumber() != null) existing.setShelfNumber(updated.getShelfNumber());
        if (updated.getStatus() != null) existing.setStatus(updated.getStatus());

        if (updated.getQuantity() != null) {
            int diff = updated.getQuantity() - existing.getQuantity();
            existing.setQuantity(updated.getQuantity());
            int newAvailable = existing.getAvailableQuantity() + diff;
            existing.setAvailableQuantity(Math.max(0, newAvailable));
            if (existing.getAvailableQuantity() > 0 && "BORROWED".equals(existing.getStatus())) {
                existing.setStatus("AVAILABLE");
            }
        }
        if (updated.getAvailableQuantity() != null) {
            existing.setAvailableQuantity(updated.getAvailableQuantity());
        }

        Book saved = bookRepository.save(existing);
        transactionService.recordTransaction(
                "BOOK_UPDATED",
                null,
                saved,
                "Book updated: '" + saved.getTitle() + "' (ID: " + saved.getBookId() + ")",
                "SUCCESS"
        );
        return saved;
    }

    public void deleteBook(Long id) {
        Book book = getBookById(id);

        List<Borrow> activeBorrows = borrowRepository.findByBook_BookId(id);
        boolean hasActive = activeBorrows.stream().anyMatch(b -> "BORROWED".equals(b.getStatus()));
        if (hasActive) {
            throw new BadRequestException("Cannot delete book '" + book.getTitle() + "' as it currently has active borrows");
        }

        transactionService.recordTransaction(
                "BOOK_DELETED",
                null,
                null,
                "Book deleted: '" + book.getTitle() + "' (ISBN: " + book.getIsbn() + ")",
                "SUCCESS"
        );
        bookRepository.delete(book);
    }

    @Transactional(readOnly = true)
    public List<Book> searchBooks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllBooks();
        }
        return bookRepository.searchBooks(query.trim());
    }

    @Transactional(readOnly = true)
    public List<Book> getBooksByStatus(String status) {
        return bookRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Book> getAvailableBooks() {
        return bookRepository.findByAvailableQuantityGreaterThan(0);
    }
}
