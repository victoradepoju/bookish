package com.victor.bookish.book;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface BookRepository  extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {


    @Query("""
            SELECT book
            FROM Book book
            WHERE book.archived = false
            AND book.sharable = true
            AND book.owner.id != :userId
            """)
    // a JpaRepository method that takes a Pageable as a parameter tells Spring that
    // pagination is required?
    // No. I think returning a 'Page' class does this...?
    Page<Book> findAllDisplayableBooks(Pageable pageable, Integer userId);
}
