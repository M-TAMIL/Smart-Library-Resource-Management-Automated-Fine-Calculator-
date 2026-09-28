package com.library.smartlibrary.repository;

import com.library.smartlibrary.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByStatus(String status);

    List<Reservation> findByBook_BookIdAndStatus(Long bookId, String status);

    List<Reservation> findByMember_MemberId(Long memberId);

    long countByStatus(String status);

    @Query("SELECT r FROM Reservation r ORDER BY r.reservationId DESC")
    List<Reservation> findAllOrderByIdDesc();
}
