package com.library.user.interfaces.http.request;

import jakarta.validation.constraints.NotBlank;

public record ChangeUserStatusRequest(boolean blocked, @NotBlank String reason) {}
