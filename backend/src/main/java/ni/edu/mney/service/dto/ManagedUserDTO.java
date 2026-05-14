package ni.edu.mney.service.dto;

import java.util.List;

public record ManagedUserDTO(
    String id,
    String login,
    String firstName,
    String lastName,
    String email,
    boolean activated,
    List<String> roles,
    List<String> requiredActions
) {}
