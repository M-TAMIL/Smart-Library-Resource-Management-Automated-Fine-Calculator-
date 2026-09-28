package com.library.smartlibrary.controller;

import com.library.smartlibrary.dto.ApiResponse;
import com.library.smartlibrary.entity.Alert;
import com.library.smartlibrary.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @GetMapping
    public ResponseEntity<List<Alert>> getAllAlerts(@RequestParam(required = false) String status) {
        if ("UNREAD".equalsIgnoreCase(status)) {
            return ResponseEntity.ok(alertService.getUnreadAlerts());
        }
        return ResponseEntity.ok(alertService.getAllAlerts());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Alert> markAsRead(@PathVariable Long id) {
        Alert updated = alertService.markAsRead(id);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        alertService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.success("All alerts marked as read"));
    }
}
