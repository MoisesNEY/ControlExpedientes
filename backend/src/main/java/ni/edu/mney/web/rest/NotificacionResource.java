package ni.edu.mney.web.rest;

import java.util.List;
import ni.edu.mney.service.NotificacionService;
import ni.edu.mney.service.dto.NotificacionDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionResource {

    private final NotificacionService notificacionService;

    public NotificacionResource(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificacionDTO>> getMine() {
        return ResponseEntity.ok(notificacionService.findCurrentUserNotifications());
    }

    @PatchMapping("/me/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markMineRead() {
        notificacionService.markCurrentUserNotificationsRead();
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markOneRead(@PathVariable("id") Long id) {
        notificacionService.markCurrentUserNotificationRead(id);
        return ResponseEntity.noContent().build();
    }
}
