package com.library.smartlibrary.repository;

import com.library.smartlibrary.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByIsbn(String isbn);
    
    List<Book> findByStatus(String status);
    
    List<Book> findByAvailableQuantityGreaterThan(Integer quantity);

    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(b.category) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Book> searchBooks(@Param("query") String query);

    @Query("SELECT COALESCE(SUM(b.quantity), 0) FROM Book b")
    Long sumTotalCopies();

    @Query("SELECT COALESCE(SUM(b.availableQuantity), 0) FROM Book b")
    Long sumAvailableCopies();
}
