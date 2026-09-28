package com.library.smartlibrary.repository;

import com.library.smartlibrary.entity.Borrow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BorrowRepository extends JpaRepository<Borrow, Long> {
    List<Borrow> findByStatus(String status);

    List<Borrow> findByMember_MemberId(Long memberId);

    List<Borrow> findByBook_BookId(Long bookId);

    @Query("SELECT b FROM Borrow b WHERE b.status = 'BORROWED' AND b.dueDate < :currentDate")
    List<Borrow> findOverdueBorrows(@Param("currentDate") LocalDate currentDate);

    long countByStatus(String status);

    @Query("SELECT COUNT(b) FROM Borrow b WHERE b.status = 'BORROWED' AND b.dueDate < :currentDate")
    long countOverdueBorrows(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT b FROM Borrow b ORDER BY b.borrowId DESC")
    List<Borrow> findAllOrderByIdDesc();
}
