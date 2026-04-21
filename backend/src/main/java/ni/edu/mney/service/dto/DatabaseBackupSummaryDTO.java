package ni.edu.mney.service.dto;

import java.util.List;

public record DatabaseBackupSummaryDTO(
        DatabaseBackupSettingsDTO settings,
        List<DatabaseBackupHistoryItemDTO> backups) {}
