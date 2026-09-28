package com.library.smartlibrary.repository;

import com.library.smartlibrary.entity.Fine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FineRepository extends JpaRepository<Fine, Long> {
    List<Fine> findByPaymentStatus(String paymentStatus);

    List<Fine> findByMember_MemberId(Long memberId);

    long countByPaymentStatus(String paymentStatus);

    @Query("SELECT COALESCE(SUM(f.fineAmount), 0.0) FROM Fine f WHERE f.paymentStatus = :status")
    Double sumFineAmountByPaymentStatus(@Param("status") String status);

    @Query("SELECT f FROM Fine f ORDER BY f.fineId DESC")
    List<Fine> findAllOrderByIdDesc();
}
