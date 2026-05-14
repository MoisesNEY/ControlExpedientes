package ni.edu.mney.service;

import ni.edu.mney.domain.UserPreferences;
import ni.edu.mney.repository.UserPreferencesRepository;
import ni.edu.mney.security.SecurityUtils;
import ni.edu.mney.service.dto.UserPreferencesDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserPreferencesService {

    private final UserPreferencesRepository userPreferencesRepository;

    public UserPreferencesService(UserPreferencesRepository userPreferencesRepository) {
        this.userPreferencesRepository = userPreferencesRepository;
    }

    @Transactional(readOnly = true)
    public UserPreferencesDTO getCurrentUserPreferences() {
        String login = currentLogin();
        return userPreferencesRepository.findById(login).map(this::toDto).orElseGet(() -> new UserPreferencesDTO(null, null, null, null));
    }

    public UserPreferencesDTO updateCurrentUserPreferences(UserPreferencesDTO request) {
        String login = currentLogin();
        UserPreferences preferences = userPreferencesRepository.findById(login).orElseGet(() -> {
            UserPreferences created = new UserPreferences();
            created.setUserLogin(login);
            return created;
        });

        preferences.setPrimaryColor(normalizeNullable(request.primaryColor()));
        preferences.setSidebarColor(normalizeNullable(request.sidebarColor()));
        preferences.setTheme(normalizeNullable(request.theme()));
        preferences.setDeviceBackupDownloadsEnabled(request.deviceBackupDownloadsEnabled());

        return toDto(userPreferencesRepository.save(preferences));
    }

    private String currentLogin() {
        return SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new IllegalStateException("No hay una sesión activa."));
    }

    private UserPreferencesDTO toDto(UserPreferences preferences) {
        return new UserPreferencesDTO(
            preferences.getPrimaryColor(),
            preferences.getSidebarColor(),
            preferences.getTheme(),
            preferences.getDeviceBackupDownloadsEnabled()
        );
    }

    private String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
