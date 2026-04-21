package ni.edu.mney.service.dto;

import java.time.LocalDateTime;

public record DatabaseBackupHistoryItemDTO(
        String filename,
        long sizeBytes,
        LocalDateTime createdAt,
        String trigger,
        boolean automatic) {}
