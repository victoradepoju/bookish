package com.victor.bookish.auth;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class RegistrationRequest {

    @NotEmpty(message = "Firstname is needed for registration") // from validation package
    @NotBlank(message = "Firstname is needed for registration")
    private String firstname;

    @NotEmpty(message = "Lastname is needed for registration") // from validation package
    @NotBlank(message = "Lastname is needed for registration")
    private String lastname;

    @Email(message = "Email is not properly formatted")
    @NotEmpty(message = "Email is needed for registration") // from validation package
    @NotBlank(message = "Email is needed for registration")
    private String email;

    @Size(min = 8, message = "Password should be 8 characters or more")
    @NotEmpty(message = "Password is needed for registration") // from validation package
    @NotBlank(message = "Password is needed for registration")
    private String password;
}
