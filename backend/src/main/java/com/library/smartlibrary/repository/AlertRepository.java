package com.library.smartlibrary.repository;

import com.library.smartlibrary.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByReadStatus(String readStatus);

    long countByReadStatus(String readStatus);

    List<Alert> findTop10ByOrderByCreatedDateDesc();

    List<Alert> findAllByOrderByCreatedDateDesc();
}
