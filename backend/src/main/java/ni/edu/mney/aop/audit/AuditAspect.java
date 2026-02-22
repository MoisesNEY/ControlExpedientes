package ni.edu.mney.aop.audit;

import java.time.ZonedDateTime;
import java.util.Optional;
import ni.edu.mney.domain.AuditoriaAcciones;
import ni.edu.mney.domain.User;
import ni.edu.mney.repository.AuditoriaAccionesRepository;
import ni.edu.mney.repository.PacienteRepository;
import ni.edu.mney.repository.UserRepository;
import ni.edu.mney.security.SecurityUtils;
import ni.edu.mney.service.dto.DiagnosticoDTO;
import ni.edu.mney.service.dto.TratamientoDTO;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Aspecto para la auditoría automática de acciones críticas en el flujo
 * clínico.
 * Cumple con la normativa de trazabilidad de modificaciones en diagnósticos y
 * tratamientos.
 */
@Aspect
@Component
public class AuditAspect {

    private final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private final AuditoriaAccionesRepository auditoriaAccionesRepository;
    private final UserRepository userRepository;
    private final PacienteRepository pacienteRepository;

    public AuditAspect(
            AuditoriaAccionesRepository auditoriaAccionesRepository,
            UserRepository userRepository,
            PacienteRepository pacienteRepository) {
        this.auditoriaAccionesRepository = auditoriaAccionesRepository;
        this.userRepository = userRepository;
        this.pacienteRepository = pacienteRepository;
    }

    /**
     * Monitorea el guardado y actualización de Diagnósticos.
     */
    @AfterReturning(pointcut = "execution(* ni.edu.mney.service.DiagnosticoService.save(..)) || " +
            "execution(* ni.edu.mney.service.DiagnosticoService.update(..)) || " +
            "execution(* ni.edu.mney.service.DiagnosticoService.partialUpdate(..))", returning = "result")
    public void auditDiagnostico(Object result) {
        if (result instanceof DiagnosticoDTO dto) {
            saveAudit("Diagnostico", "MODIFICACION",
                    dto.getConsulta() != null && dto.getConsulta().getExpediente() != null
                            ? dto.getConsulta().getExpediente().getId()
                            : null,
                    "el diagnóstico (CIE: " + (dto.getCodigoCIE() != null ? dto.getCodigoCIE() : "N/A") + ")");
        } else if (result instanceof Optional<?> optional && optional.isPresent()
                && optional.get() instanceof DiagnosticoDTO dto) {
            saveAudit("Diagnostico", "MODIFICACION",
                    dto.getConsulta() != null && dto.getConsulta().getExpediente() != null
                            ? dto.getConsulta().getExpediente().getId()
                            : null,
                    "el diagnóstico (CIE: " + (dto.getCodigoCIE() != null ? dto.getCodigoCIE() : "N/A") + ")");
        }
    }

    /**
     * Monitorea el guardado y actualización de Tratamientos.
     */
    @AfterReturning(pointcut = "execution(* ni.edu.mney.service.TratamientoService.save(..)) || " +
            "execution(* ni.edu.mney.service.TratamientoService.update(..)) || " +
            "execution(* ni.edu.mney.service.TratamientoService.partialUpdate(..))", returning = "result")
    public void auditTratamiento(Object result) {
        if (result instanceof TratamientoDTO dto) {
            saveAudit("Tratamiento", "MODIFICACION",
                    dto.getConsulta() != null && dto.getConsulta().getExpediente() != null
                            ? dto.getConsulta().getExpediente().getId()
                            : null,
                    "el tratamiento");
        } else if (result instanceof Optional<?> optional && optional.isPresent()
                && optional.get() instanceof TratamientoDTO dto) {
            saveAudit("Tratamiento", "MODIFICACION",
                    dto.getConsulta() != null && dto.getConsulta().getExpediente() != null
                            ? dto.getConsulta().getExpediente().getId()
                            : null,
                    "el tratamiento");
        }
    }

    private void saveAudit(String entidad, String accion, Long expedienteId, String detalle) {
        String login = SecurityUtils.getCurrentUserLogin().orElse("system");
        User user = userRepository.findOneByLogin(login).orElse(null);

        String patientName = "Desconocido";
        if (expedienteId != null) {
            patientName = pacienteRepository.findByExpedienteId(expedienteId)
                    .map(p -> p.getNombres() + " " + p.getApellidos())
                    .orElse("Paciente no encontrado");
        }

        String doctorName = user != null ? (user.getFirstName() + " " + user.getLastName()).trim() : login;
        if (doctorName.isEmpty())
            doctorName = login;

        String descripcion = String.format("El profesional %s modificó %s del paciente %s.", doctorName, detalle,
                patientName);

        AuditoriaAcciones audit = new AuditoriaAcciones();
        audit.setEntidad(entidad);
        audit.setAccion(accion);
        audit.setFecha(ZonedDateTime.now());
        audit.setDescripcion(descripcion);
        audit.setUser(user);

        auditoriaAccionesRepository.save(audit);
        log.debug("Auditoría clínica generada: {}", descripcion);
    }
}
