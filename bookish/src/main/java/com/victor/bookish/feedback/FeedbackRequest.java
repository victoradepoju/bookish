package com.victor.bookish.feedback;

import jakarta.validation.constraints.*;

// it doesn't have an id field because we decided that
// feedback shouldn't be updatable
public record FeedbackRequest (
        @Positive(message = "200")
        @Min(value = 0, message = "201")
        @Max(value = 5, message = "202")
        Double note,

        @NotNull(message = "203")
        @NotEmpty(message = "203")
        @NotBlank(message = "203")
        String comment,

        @NotNull(message = "203")
        Integer bookId
) {}
