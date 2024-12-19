package com.acme.hkaprozesse.rest;
import java.util.List;

@SuppressWarnings("RecordComponentNumber")
public record RolleDTO(
        String rolleName,
        List<UserWrapperDTO> users
) {}




