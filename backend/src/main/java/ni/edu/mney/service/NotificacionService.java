package ni.edu.mney.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import ni.edu.mney.domain.AppNotification;
import ni.edu.mney.domain.User;
import ni.edu.mney.repository.AppNotificationRepository;
import ni.edu.mney.repository.UserRepository;
import ni.edu.mney.security.SecurityUtils;
import ni.edu.mney.service.dto.NotificacionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de notificaciones push vía WebSocket (STOMP).
 * Envía mensajes a tópicos específicos para que los clientes suscritos
 * reciban actualizaciones en tiempo real.
 */
@Service
public class NotificacionService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificacionService.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final AppNotificationRepository appNotificationRepository;
    private final UserRepository userRepository;

    public NotificacionService(
        SimpMessagingTemplate messagingTemplate,
        AppNotificationRepository appNotificationRepository,
        UserRepository userRepository
    ) {
        this.messagingTemplate = messagingTemplate;
        this.appNotificationRepository = appNotificationRepository;
        this.userRepository = userRepository;
    }

    /**
     * Notifica a todos los médicos suscritos que un paciente está listo
     * para ser atendido (cambió a ESPERANDO_MEDICO).
     *
     * @param notificacion DTO con los datos del paciente y la cita
     */
    public void notificarPacienteListo(NotificacionDTO notificacion) {
        LOG.debug("Enviando notificación WebSocket: paciente listo -> {}", notificacion);
        messagingTemplate.convertAndSend("/topic/espera", notificacion);
    }

    /**
     * Notifica a un médico específico que tiene un nuevo paciente en espera.
     *
     * @param medicoLogin  el login del médico destino
     * @param notificacion DTO con los datos
     */
    public void notificarMedicoEspecifico(String medicoLogin, NotificacionDTO notificacion) {
        LOG.debug("Enviando notificación WebSocket a médico {}: {}", medicoLogin, notificacion);
        notificarUsuario(medicoLogin, notificacion);
    }

    public void notificarSistemaAdministrativo(NotificacionDTO notificacion) {
        LOG.debug("Enviando notificación de sistema administrativo: {}", notificacion);
        messagingTemplate.convertAndSend("/topic/admin/system", notificacion);
        notificarRoles(List.of("ROLE_ADMIN"), notificacion, false);
    }

    @Transactional(readOnly = true)
    public List<NotificacionDTO> findCurrentUserNotifications() {
        String login = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new IllegalStateException("No hay usuario autenticado."));
        return appNotificationRepository
            .findByUserLoginAndReadAtIsNullOrderByTimestampDesc(login, PageRequest.of(0, 50))
            .stream()
            .map(this::toDto)
            .toList();
    }

    @Transactional
    public void markCurrentUserNotificationsRead() {
        String login = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new IllegalStateException("No hay usuario autenticado."));
        appNotificationRepository.markAllReadByUserLogin(login);
    }

    @Transactional
    public void markCurrentUserNotificationRead(Long id) {
        String login = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new IllegalStateException("No hay usuario autenticado."));
        appNotificationRepository.markReadByIdAndUserLogin(id, login);
    }

    @Transactional
    public void notificarUsuario(String login, NotificacionDTO notificacion) {
        if (login == null || login.isBlank()) {
            return;
        }
        if (isCurrentUser(login)) {
            return;
        }
        userRepository.findOneByLogin(login).ifPresent(user -> persistAndPush(user, notificacion));
    }

    @Transactional
    public void notificarRoles(List<String> roles, NotificacionDTO notificacion, boolean excluirUsuarioActual) {
        Set<String> logins = new LinkedHashSet<>();
        List<User> users = new ArrayList<>();
        for (String role : roles) {
            for (User user : userRepository.findActivatedByAuthority(role)) {
                if (user.getLogin() == null || logins.contains(user.getLogin())) {
                    continue;
                }
                if (excluirUsuarioActual && isCurrentUser(user.getLogin())) {
                    continue;
                }
                logins.add(user.getLogin());
                users.add(user);
            }
        }
        users.forEach(user -> persistAndPush(user, notificacion));
    }

    private void persistAndPush(User user, NotificacionDTO notificacion) {
        AppNotification saved = appNotificationRepository.save(toEntity(user, notificacion));
        NotificacionDTO payload = toDto(saved);
        messagingTemplate.convertAndSend("/topic/user/" + user.getLogin(), payload);
    }

    private boolean isCurrentUser(String login) {
        Optional<String> current = SecurityUtils.getCurrentUserLogin();
        return current.isPresent() && Objects.equals(current.get(), login);
    }

    private AppNotification toEntity(User user, NotificacionDTO dto) {
        AppNotification entity = new AppNotification();
        entity.setUser(user);
        entity.setTipo(dto.getTipo());
        entity.setMensaje(dto.getMensaje());
        entity.setCitaId(dto.getCitaId());
        entity.setPacienteNombre(dto.getPacienteNombre());
        entity.setMedicoLogin(dto.getMedicoLogin());
        entity.setRutaAccion(dto.getRutaAccion());
        entity.setArchivoDescarga(dto.getArchivoDescarga());
        entity.setAccionLabel(dto.getAccionLabel());
        entity.setTimestamp(dto.getTimestamp());
        return entity;
    }

    private NotificacionDTO toDto(AppNotification entity) {
        NotificacionDTO dto = new NotificacionDTO();
        dto.setId(entity.getId());
        dto.setTipo(entity.getTipo());
        dto.setMensaje(entity.getMensaje());
        dto.setCitaId(entity.getCitaId());
        dto.setPacienteNombre(entity.getPacienteNombre());
        dto.setMedicoLogin(entity.getMedicoLogin());
        dto.setRutaAccion(entity.getRutaAccion());
        dto.setArchivoDescarga(entity.getArchivoDescarga());
        dto.setAccionLabel(entity.getAccionLabel());
        dto.setTimestamp(entity.getTimestamp());
        dto.setLeida(entity.getReadAt() != null && entity.getReadAt().isBefore(Instant.now().plusSeconds(1)));
        return dto;
    }
}
