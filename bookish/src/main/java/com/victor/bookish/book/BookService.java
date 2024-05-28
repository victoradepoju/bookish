package com.victor.bookish.book;

import com.victor.bookish.common.PageResponse;
import com.victor.bookish.exception.OperationNotPermittedException;
import com.victor.bookish.file.FileStorageService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookMapper bookMapper;
    private final BookRepository bookRepository;
    private final BookTransactionHistoryRepository bookTransactionHistoryRepository;
    private final FileStorageService fileStorageService;

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

    public Integer updateSharableStatus(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(()->
                        new EntityNotFoundException("No book found with ID:: " + bookId));

        User user = (User) connectedUser.getPrincipal();

        if(!Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException(
                    "You can only update sharable status for your own book"
            );
        }

        book.setShareable(!book.isShareable());
        bookRepository.save(book);

        return bookId;
    }

    public Integer updateArchivedStatus(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(()->
                        new EntityNotFoundException("No book found with ID:: " + bookId));

        User user = (User) connectedUser.getPrincipal();

        if(!Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException(
                    "You can only update archive status for your own book"
            );
        }

        book.setArchived(!book.isArchived());
        bookRepository.save(book);
        return bookId;
    }

    public Integer borrowBook(Integer bookId, Authentication connectedUser) {
        Book book =  bookRepository.findById(bookId)
                .orElseThrow(() ->
                        new EntityNotFoundException("No book found with ID:: " + bookId));

        if(book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException(
                    "The requested book cannot be borrowed " +
                            "since it is archived or not sharable"
            );
        }

        User user = (User) connectedUser.getPrincipal();

        if(Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException(
                    "You cannot borrow your own book"
            );
        }

        // To check if the book is borrowed by another user, even if the 'user' is the
        // user himself
        final boolean isAlreadyBorrowed = bookTransactionHistoryRepository
                .isAlreadyBorrowedByUser(bookId, user.getId());

        if (isAlreadyBorrowed) {
            throw new OperationNotPermittedException(
                    "The requested book is already borrowed"
            );
        }

        // since borrowing book will affect the book transaction history table, we need
        // to add these to the db
        BookTransactionHistory  bookTransactionHistory = BookTransactionHistory.builder()
                .book(book)
                .user(user)
                .returned(false)
                .returnApproved(false)
                .build();

        return bookTransactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    public Integer returnBorrowedBook(Integer bookId, Authentication connectedUser) {
        Book book =  bookRepository.findById(bookId)
                .orElseThrow(() ->
                        new EntityNotFoundException("No book found with ID:: " + bookId));

        if(book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException(
                    "The requested book cannot be borrowed or returned " +
                            "since it is archived or not sharable"
            );
        }

        User user = (User) connectedUser.getPrincipal();

        if(Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException(
                    "You cannot borrow or return your own book"
            );
        }

        // we need to make sure the user has already borrowed this book.
        // we also need a BookTransactionHistory object so we can be able to update
        // the table and save the changes in the db.
        BookTransactionHistory bookTransactionHistory = bookTransactionHistoryRepository
                .findByBookIdAndUserId(bookId, user.getId())
                // should be EntityNotFoundException("You did not borrow this book")??
                .orElseThrow(() ->
                        new OperationNotPermittedException("You did not borrow this book")
                );

        bookTransactionHistory.setReturned(true);
//        bookTransactionHistory.setId(book.getOwner().getId());

        return bookTransactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    public Integer approveReturnBorrowedBook(Integer bookId, Authentication connectedUser) {
        Book book =  bookRepository.findById(bookId)
                .orElseThrow(() ->
                        new EntityNotFoundException("No book found with ID:: " + bookId));

        if(book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException(
                    "The requested book cannot be borrowed or returned " +
                            "since it is archived or not sharable"
            );
        }

        User user = (User) connectedUser.getPrincipal();

        if(Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException(
                    "You cannot borrow or return your own book"
            );
        }

        BookTransactionHistory bookTransactionHistory = bookTransactionHistoryRepository
                .findByBookIdAndOwnerId(bookId, user.getId())
                .orElseThrow(() ->
                        new OperationNotPermittedException(
                                "The book is not returned yet. You cannot approve " +
                                        "its return"
                        )
                );

        bookTransactionHistory.setReturnApproved(true);
        return bookTransactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    public void uploadBookCoverPicture(
            MultipartFile file,
            Integer bookId,
            Authentication connectedUser
    ) {
        Book book =  bookRepository.findById(bookId)
                .orElseThrow(() ->
                        new EntityNotFoundException("No book found with ID:: " + bookId));

        User user = (User) connectedUser.getPrincipal();

        var bookCover = fileStorageService.savefile(file, user.getId());
        book.setBookCover(bookCover);
        bookRepository.save(book);
    }
}
