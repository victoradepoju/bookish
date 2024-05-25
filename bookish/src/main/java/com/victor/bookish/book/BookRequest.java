package com.victor.bookish.book;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record BookRequest(
        /* if id is null, that means we want to create a new book.
           if id is non-null, that means we want to update a book.
         */
        Integer id,

        @NotNull(message = "100")
        @NotEmpty(message = "100")
        String title,

        @NotNull(message = "101")
        @NotEmpty(message = "101")
        String authorName,

        @NotNull(message = "102")
        @NotEmpty(message = "102")
        String isbn,

        @NotNull(message = "103")
        @NotEmpty(message = "103")
        String synopsis,

        boolean sharable
) {
}
