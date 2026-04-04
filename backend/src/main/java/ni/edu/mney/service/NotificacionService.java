package ni.edu.mney.service;

import ni.edu.mney.service.dto.NotificacionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Servicio de notificaciones push vía WebSocket (STOMP).
 * Envía mensajes a tópicos específicos para que los clientes suscritos
 * reciban actualizaciones en tiempo real.
 */
@Service
public class NotificacionService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificacionService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public NotificacionService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
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
        messagingTemplate.convertAndSend("/topic/medico/" + medicoLogin, notificacion);
    }
}
