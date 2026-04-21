package ni.edu.mney.security;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import ni.edu.mney.domain.RoleDefinition;
import ni.edu.mney.repository.RoleDefinitionRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class PermissionAuthorityService {

    public static final String PERMISSION_PREFIX = "PERM_";

    private final RoleDefinitionRepository roleDefinitionRepository;

    public PermissionAuthorityService(RoleDefinitionRepository roleDefinitionRepository) {
        this.roleDefinitionRepository = roleDefinitionRepository;
    }

    public Collection<GrantedAuthority> buildAuthorities(Collection<String> roleNames) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        Set<String> normalizedRoles = normalizeRoles(roleNames);
        normalizedRoles.forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));
        resolvePermissions(normalizedRoles).forEach(permission -> authorities.add(new SimpleGrantedAuthority(permissionAuthority(permission))));
        return List.copyOf(authorities);
    }

    public Set<String> resolvePermissions(Collection<String> roleNames) {
        Set<String> normalizedRoles = normalizeRoles(roleNames);
        if (normalizedRoles.isEmpty()) {
            return Set.of();
        }
        return roleDefinitionRepository.findAllByRoleNameIn(normalizedRoles).stream()
            .map(RoleDefinition::getPermissions)
            .flatMap(Collection::stream)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<String> extractPermissions(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .filter(authority -> authority.startsWith(PERMISSION_PREFIX))
            .map(authority -> authority.substring(PERMISSION_PREFIX.length()))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static String permissionAuthority(String permission) {
        return PERMISSION_PREFIX + permission;
    }

    private Set<String> normalizeRoles(Collection<String> roleNames) {
        if (roleNames == null) {
            return Set.of();
        }
        return roleNames.stream()
            .filter(role -> role != null && role.startsWith("ROLE_"))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
