package com.library.smartlibrary.controller;

import com.library.smartlibrary.dto.ReservationRequest;
import com.library.smartlibrary.entity.Reservation;
import com.library.smartlibrary.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "*")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @GetMapping
    public ResponseEntity<List<Reservation>> getAllReservations(@RequestParam(required = false) String status) {
        if ("ACTIVE".equalsIgnoreCase(status)) {
            return ResponseEntity.ok(reservationService.getActiveReservations());
        }
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @PostMapping
    public ResponseEntity<Reservation> createReservation(@RequestBody ReservationRequest request) {
        Reservation created = reservationService.createReservation(request.getMemberId(), request.getBookId());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Reservation> cancelReservation(@PathVariable Long id) {
        Reservation cancelled = reservationService.cancelReservation(id);
        return ResponseEntity.ok(cancelled);
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<Reservation> completeReservation(@PathVariable Long id) {
        Reservation completed = reservationService.completeReservation(id);
        return ResponseEntity.ok(completed);
    }
}
