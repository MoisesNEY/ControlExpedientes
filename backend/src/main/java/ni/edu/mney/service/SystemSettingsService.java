package ni.edu.mney.service;

import ni.edu.mney.domain.SystemSettings;
import ni.edu.mney.repository.SystemSettingsRepository;
import ni.edu.mney.service.dto.SystemSettingsDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SystemSettingsService {

    private final SystemSettingsRepository systemSettingsRepository;

    public SystemSettingsService(SystemSettingsRepository systemSettingsRepository) {
        this.systemSettingsRepository = systemSettingsRepository;
    }

    public SystemSettingsDTO getSettings() {
        return toDto(getOrCreateSettings());
    }

    public SystemSettingsDTO updateSettings(SystemSettingsDTO request) {
        SystemSettings settings = getOrCreateSettings();
        settings.setApplicationName(normalize(request.applicationName(), SystemSettings.DEFAULT_APPLICATION_NAME));
        settings.setBrandName(normalize(request.brandName(), SystemSettings.DEFAULT_BRAND_NAME));
        settings.setPrimaryColor(normalizeColor(request.primaryColor()));
        settings.setSidebarColor(normalizeSidebarColor(request.sidebarColor()));
        settings.setDefaultTheme(normalizeTheme(request.defaultTheme()));
        settings.setDefaultDeviceBackupDownloadsEnabled(request.defaultDeviceBackupDownloadsEnabled());
        settings.setShowBackupDownloadReminders(request.showBackupDownloadReminders());
        return toDto(systemSettingsRepository.save(settings));
    }

    private SystemSettings getOrCreateSettings() {
        return systemSettingsRepository.findById(SystemSettings.SINGLETON_ID).orElseGet(() -> {
            SystemSettings settings = new SystemSettings();
            settings.setId(SystemSettings.SINGLETON_ID);
            return systemSettingsRepository.save(settings);
        });
    }

    private SystemSettingsDTO toDto(SystemSettings settings) {
        return new SystemSettingsDTO(
            settings.getApplicationName(),
            settings.getBrandName(),
            settings.getPrimaryColor(),
            settings.getSidebarColor(),
            settings.getDefaultTheme(),
            settings.isDefaultDeviceBackupDownloadsEnabled(),
            settings.isShowBackupDownloadReminders()
        );
    }

    private String normalize(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String normalizeColor(String value) {
        return value == null || value.isBlank() ? SystemSettings.DEFAULT_PRIMARY_COLOR : value.trim();
    }

    private String normalizeSidebarColor(String value) {
        return value == null || value.isBlank() ? SystemSettings.DEFAULT_SIDEBAR_COLOR : value.trim();
    }

    private String normalizeTheme(String value) {
        return value == null || value.isBlank() ? SystemSettings.DEFAULT_THEME : value.trim();
    }
}
