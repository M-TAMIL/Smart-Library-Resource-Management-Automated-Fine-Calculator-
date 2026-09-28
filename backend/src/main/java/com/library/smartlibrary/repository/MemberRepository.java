package com.library.smartlibrary.repository;

import com.library.smartlibrary.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    List<Member> findByStatus(String status);

    @Query("SELECT m FROM Member m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(m.email) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(m.phone) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(m.department) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Member> searchMembers(@Param("query") String query);
}
