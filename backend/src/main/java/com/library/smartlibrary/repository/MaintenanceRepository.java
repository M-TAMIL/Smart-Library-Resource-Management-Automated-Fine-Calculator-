package com.library.smartlibrary.repository;

import com.library.smartlibrary.entity.Maintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {
    List<Maintenance> findByStatus(String status);

    List<Maintenance> findByBook_BookId(Long bookId);

    long countByStatus(String status);

    @Query("SELECT m FROM Maintenance m ORDER BY m.maintenanceId DESC")
    List<Maintenance> findAllOrderByIdDesc();
}
