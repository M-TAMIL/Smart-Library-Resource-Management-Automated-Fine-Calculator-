package com.library.smartlibrary.controller;

import com.library.smartlibrary.dto.BorrowRequest;
import com.library.smartlibrary.dto.ReturnRequest;
import com.library.smartlibrary.entity.Borrow;
import com.library.smartlibrary.service.BorrowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrows")
@CrossOrigin(origins = "*")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;

    @GetMapping
    public ResponseEntity<List<Borrow>> getAllBorrows(@RequestParam(required = false) Long memberId) {
        if (memberId != null) {
            return ResponseEntity.ok(borrowService.getBorrowsByMember(memberId));
        }
        return ResponseEntity.ok(borrowService.getAllBorrows());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Borrow> getBorrowById(@PathVariable Long id) {
        return ResponseEntity.ok(borrowService.getBorrowById(id));
    }

    @GetMapping("/active")
    public ResponseEntity<List<Borrow>> getActiveBorrows() {
        return ResponseEntity.ok(borrowService.getActiveBorrows());
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<Borrow>> getOverdueBorrows() {
        return ResponseEntity.ok(borrowService.getOverdueBorrows());
    }

    @PostMapping
    public ResponseEntity<Borrow> issueBook(@RequestBody BorrowRequest request) {
        Borrow issued = borrowService.borrowBook(request);
        return new ResponseEntity<>(issued, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<Borrow> returnBook(@PathVariable Long id, @RequestBody(required = false) ReturnRequest request) {
        Borrow returned = borrowService.returnBook(id, request);
        return ResponseEntity.ok(returned);
    }
}
