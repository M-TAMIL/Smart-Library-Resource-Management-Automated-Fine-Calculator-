package com.library.smartlibrary.controller;

import com.library.smartlibrary.entity.Fine;
import com.library.smartlibrary.service.FineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fines")
@CrossOrigin(origins = "*")
public class FineController {

    @Autowired
    private FineService fineService;

    @GetMapping
    public ResponseEntity<List<Fine>> getAllFines(@RequestParam(required = false) Long memberId) {
        if (memberId != null) {
            return ResponseEntity.ok(fineService.getFinesByMember(memberId));
        }
        return ResponseEntity.ok(fineService.getAllFines());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Fine>> getPendingFines() {
        return ResponseEntity.ok(fineService.getPendingFines());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Fine> getFineById(@PathVariable Long id) {
        return ResponseEntity.ok(fineService.getFineById(id));
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<Fine> payFine(@PathVariable Long id) {
        Fine paid = fineService.payFine(id);
        return ResponseEntity.ok(paid);
    }
}
