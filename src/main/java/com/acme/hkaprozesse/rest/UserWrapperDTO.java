package com.acme.hkaprozesse.rest;

public record UserWrapperDTO(
        String _id,
        String functionName,
        UserDTO user
) {
}
