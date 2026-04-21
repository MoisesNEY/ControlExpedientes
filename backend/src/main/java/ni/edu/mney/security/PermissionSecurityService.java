package ni.edu.mney.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("permissionSecurityService")
public class PermissionSecurityService {

    public boolean hasPermission(String permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        String expectedAuthority = PermissionAuthorityService.permissionAuthority(permission);
        return authentication.getAuthorities().stream().anyMatch(authority -> expectedAuthority.equals(authority.getAuthority()));
    }
}
