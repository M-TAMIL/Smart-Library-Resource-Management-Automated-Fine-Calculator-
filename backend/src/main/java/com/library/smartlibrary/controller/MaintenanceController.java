package com.library.smartlibrary.controller;

import com.library.smartlibrary.dto.MaintenanceRequest;
import com.library.smartlibrary.entity.Maintenance;
import com.library.smartlibrary.service.MaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/maintenance")
@CrossOrigin(origins = "*")
public class MaintenanceController {

    @Autowired
    private MaintenanceService maintenanceService;

    @GetMapping
    public ResponseEntity<List<Maintenance>> getAllMaintenance() {
        return ResponseEntity.ok(maintenanceService.getAllMaintenance());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Maintenance> getMaintenanceById(@PathVariable Long id) {
        return ResponseEntity.ok(maintenanceService.getMaintenanceById(id));
    }

    @PostMapping
    public ResponseEntity<Maintenance> startMaintenance(@RequestBody MaintenanceRequest request) {
        Maintenance created = maintenanceService.startMaintenance(
                request.getBookId(),
                request.getReason(),
                request.getExpectedCompletionDate(),
                request.getRemarks()
        );
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<Maintenance> completeMaintenance(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> payload) {
        String remarks = payload != null ? payload.get("remarks") : null;
        Maintenance completed = maintenanceService.completeMaintenance(id, remarks);
        return ResponseEntity.ok(completed);
    }
}
