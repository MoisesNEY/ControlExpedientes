package ni.edu.mney.service.dto;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record DatabaseBackupSettingsDTO(
        boolean enabled,
        DatabaseBackupFrequency frequency,
        Integer intervalHours,
        DayOfWeek dayOfWeek,
        LocalTime time,
        LocalDateTime lastBackupAt,
        LocalDateTime lastAutomaticExecutionAt,
        LocalDateTime nextExecutionAt,
        String lastBackupFilename) {}
