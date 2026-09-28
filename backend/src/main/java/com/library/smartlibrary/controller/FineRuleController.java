package com.library.smartlibrary.controller;

import com.library.smartlibrary.entity.FineRule;
import com.library.smartlibrary.service.FineRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fine-rules")
@CrossOrigin(origins = "*")
public class FineRuleController {

    @Autowired
    private FineRuleService fineRuleService;

    @GetMapping
    public ResponseEntity<List<FineRule>> getAllRules() {
        return ResponseEntity.ok(fineRuleService.getAllRules());
    }

    @GetMapping("/active")
    public ResponseEntity<FineRule> getActiveRule() {
        return ResponseEntity.ok(fineRuleService.getActiveRule());
    }

    @PostMapping
    public ResponseEntity<FineRule> addRule(@RequestBody FineRule rule) {
        FineRule created = fineRuleService.addRule(rule);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FineRule> updateRule(@PathVariable Long id, @RequestBody FineRule rule) {
        FineRule updated = fineRuleService.updateRule(id, rule);
        return ResponseEntity.ok(updated);
    }
}
