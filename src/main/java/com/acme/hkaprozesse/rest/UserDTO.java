package com.acme.hkaprozesse.rest;

public record UserDTO(
        String _id,
        String userId,
        String userType,
        String userRole,
        String orgUnit,
        boolean active,
        String validFrom,
        String validUntil,
        EmployeeDTO employee
) {}
