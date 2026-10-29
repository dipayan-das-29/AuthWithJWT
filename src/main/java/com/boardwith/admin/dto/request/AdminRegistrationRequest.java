package com.boardwith.admin.dto.request;

import java.util.List;

public record AdminRegistrationRequest(String username, String password, List<String> roles) {}