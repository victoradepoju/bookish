package com.victor.bookish.book;

import com.victor.bookish.common.PageResponse;
import com.victor.bookish.history.BookTransactionHistory;
import com.victor.bookish.history.BookTransactionHistoryRepository;
import com.victor.bookish.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookMapper bookMapper;
    private final BookRepository bookRepository;
    private final BookTransactionHistoryRepository bookTransactionHistoryRepository;

    public Integer save(BookRequest bookRequest, Authentication connectedUser) {

        User user = (User) connectedUser.getPrincipal();

        Book book = bookMapper.toBook(bookRequest);
                /*Book.builder()
                .title(bookRequest.title())
                .authorName(bookRequest.authorName())
                .isbn(bookRequest.isbn())
                .synopsis(bookRequest.synopsis())
                .build();*/

        book.setOwner(user);

        return bookRepository.save(book).getId();
    }

    public BookResponse findById(Integer bookId) {
        return bookRepository.findById(bookId)
                .map(bookMapper::toBookResponse)
                .orElseThrow(() ->
                        new EntityNotFoundException("No book found with the ID:: " + bookId));
    }

    public PageResponse<BookResponse> findAllBooks(
            int page,
            int size,
            Authentication connectedUser
    ) {

        User user = (User) connectedUser.getPrincipal();

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdDate").descending());

        Page<Book> books = bookRepository.findAllDisplayableBooks(pageable, user.getId());

        // List<BookResponse> because PageResponse takes List<T> as one of its fields
        // .stream() because Page extends Slice which extends Streamable
        List<BookResponse> bookResponses = books.stream()
                .map(bookMapper::toBookResponse)
                .toList();

        return new PageResponse<>(
                bookResponses,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

    public PageResponse<BookResponse> findAllBooksOwner(
            int page,
            int size,
            Authentication connectedUser
    ) {

        User user = (User) connectedUser.getPrincipal();

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdDate").descending());

        // using Specification
        Page<Book> books = bookRepository
                .findAll(BookSpecification.withOwnerId(user.getId()), pageable);

        List<BookResponse> bookResponses = books.stream()
                .map(bookMapper::toBookResponse)
                .toList();


        return new PageResponse<>(
                bookResponses,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

    // Should be PageResponse<BorrowedBookResponse> ???????? YES!!!
    public PageResponse<BorrowedBookResponse> findAllBorrowedBooks(
            int page,
            int size,
            Authentication connectedUser
    ) {

        User user = (User) connectedUser.getPrincipal();

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdDate").descending());

        Page<BookTransactionHistory> allBorrowedBooks =
                bookTransactionHistoryRepository.findAllBorrowedBooks(pageable, user.getId());

        List<BorrowedBookResponse> bookResponses = allBorrowedBooks.stream()
                .map(bookMapper::toBorrowedBookResponse)
                .toList();

        return new PageResponse<>(
                bookResponses,
                allBorrowedBooks.getNumber(),
                allBorrowedBooks.getSize(),
                allBorrowedBooks.getTotalElements(),
                allBorrowedBooks.getTotalPages(),
                allBorrowedBooks.isFirst(),
                allBorrowedBooks.isLast()
        );
    }

    public PageResponse<BorrowedBookResponse> findAllReturnedBooks(
            int page,
            int size,
            Authentication connectedUser
    ) {

        User user = (User) connectedUser.getPrincipal();

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdDate").descending());

        Page<BookTransactionHistory> allBorrowedBooks =
                bookTransactionHistoryRepository.findAllReturnedBooks(pageable, user.getId());

        List<BorrowedBookResponse> bookResponses = allBorrowedBooks.stream()
                .map(bookMapper::toBorrowedBookResponse)
                .toList();

        return new PageResponse<>(
                bookResponses,
                allBorrowedBooks.getNumber(),
                allBorrowedBooks.getSize(),
                allBorrowedBooks.getTotalElements(),
                allBorrowedBooks.getTotalPages(),
                allBorrowedBooks.isFirst(),
                allBorrowedBooks.isLast()
        );
    }
}
