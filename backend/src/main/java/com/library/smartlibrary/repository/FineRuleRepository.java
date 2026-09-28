package com.library.smartlibrary.repository;

import com.library.smartlibrary.entity.FineRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FineRuleRepository extends JpaRepository<FineRule, Long> {
    List<FineRule> findByStatus(String status);
    
    Optional<FineRule> findFirstByStatusOrderByRuleIdDesc(String status);
}
