package com.library.smartlibrary.controller;

import com.library.smartlibrary.entity.LibraryTransaction;
import com.library.smartlibrary.service.LibraryTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    @Autowired
    private LibraryTransactionService transactionService;

    @GetMapping
    public ResponseEntity<List<LibraryTransaction>> getTransactions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type) {
        if (search != null && !search.trim().isEmpty()) {
            return ResponseEntity.ok(transactionService.searchTransactions(search));
        }
        if (type != null && !type.trim().isEmpty()) {
            return ResponseEntity.ok(transactionService.getTransactionsByType(type));
        }
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }
}
