package com.victor.bookish.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthenticationRequest {

    @Email(message = "Email is not properly formatted")
    @NotEmpty(message = "Email is needed for registration") // from validation package
    @NotBlank(message = "Email is needed for registration")
    private String email;

    @Size(min = 8, message = "Password should be 8 characters or more")
    @NotEmpty(message = "Password is needed for registration") // from validation package
    @NotBlank(message = "Password is needed for registration")
    private String password;
}
