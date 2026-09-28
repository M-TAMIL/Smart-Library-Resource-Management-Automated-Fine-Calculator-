package com.library.smartlibrary.service;

import com.library.smartlibrary.entity.Book;
import com.library.smartlibrary.entity.LibraryTransaction;
import com.library.smartlibrary.entity.Member;
import com.library.smartlibrary.repository.LibraryTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class LibraryTransactionService {

    @Autowired
    private LibraryTransactionRepository transactionRepository;

    public LibraryTransaction recordTransaction(String type, Member member, Book book, String description, String status) {
        LibraryTransaction tx = new LibraryTransaction();
        tx.setTransactionType(type);
        tx.setMember(member);
        tx.setBook(book);
        tx.setDescription(description);
        tx.setTransactionDate(LocalDateTime.now());
        tx.setStatus(status != null ? status : "SUCCESS");
        return transactionRepository.save(tx);
    }

    @Transactional(readOnly = true)
    public List<LibraryTransaction> getAllTransactions() {
        return transactionRepository.findAllByOrderByTransactionDateDesc();
    }

    @Transactional(readOnly = true)
    public List<LibraryTransaction> getTransactionsByType(String type) {
        return transactionRepository.findByTransactionType(type);
    }

    @Transactional(readOnly = true)
    public List<LibraryTransaction> searchTransactions(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllTransactions();
        }
        return transactionRepository.searchTransactions(query.trim());
    }

    @Transactional(readOnly = true)
    public List<LibraryTransaction> getRecentTransactions() {
        return transactionRepository.findTop10ByOrderByTransactionDateDesc();
    }
}
