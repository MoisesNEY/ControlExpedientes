package ni.edu.mney.service;

import jakarta.transaction.Transactional;
import ni.edu.mney.domain.*;
import ni.edu.mney.domain.enumeration.EstadoCita;
import ni.edu.mney.repository.*;
import ni.edu.mney.service.dto.FinalizarConsultaRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio transaccional que orquesta el cierre de una consulta médica.
 * En una única transacción:
 * 1. Crea la entidad ConsultaMedica
 * 2. Vincula el Diagnóstico principal (ya existente en BD)
 * 3. Crea las Recetas con sus medicamentos
 * 4. Actualiza la CitaMedica a estado ATENDIDA
 */
@Service
public class FinalizarConsultaService {

    private static final Logger LOG = LoggerFactory.getLogger(FinalizarConsultaService.class);

    private final CitaMedicaRepository citaMedicaRepository;
    private final ConsultaMedicaRepository consultaMedicaRepository;
    private final DiagnosticoRepository diagnosticoRepository;
    private final RecetaRepository recetaRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final ExpedienteClinicoRepository expedienteClinicoRepository;

    public FinalizarConsultaService(
            CitaMedicaRepository citaMedicaRepository,
            ConsultaMedicaRepository consultaMedicaRepository,
            DiagnosticoRepository diagnosticoRepository,
            RecetaRepository recetaRepository,
            MedicamentoRepository medicamentoRepository,
            ExpedienteClinicoRepository expedienteClinicoRepository) {
        this.citaMedicaRepository = citaMedicaRepository;
        this.consultaMedicaRepository = consultaMedicaRepository;
        this.diagnosticoRepository = diagnosticoRepository;
        this.recetaRepository = recetaRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.expedienteClinicoRepository = expedienteClinicoRepository;
    }

    /**
     * Finaliza una consulta médica de forma transaccional.
     *
     * @param citaId  ID de la CitaMedica a cerrar
     * @param request DTO con los datos de la consulta, diagnóstico y recetas
     */
    @Transactional
    public void finalizarConsulta(Long citaId, FinalizarConsultaRequestDTO request) {
        LOG.debug("Finalizando consulta para cita ID: {}", citaId);

        // 1. Obtener y validar la cita
        CitaMedica cita = citaMedicaRepository.findById(citaId)
                .orElseThrow(() -> new IllegalArgumentException("CitaMedica no encontrada con ID: " + citaId));

        // 2. Buscar el expediente del paciente para enlazar la consulta
        Long pacienteId = cita.getPaciente() != null ? cita.getPaciente().getId() : null;
        ExpedienteClinico expediente = null;
        if (pacienteId != null) {
            expediente = expedienteClinicoRepository.findByPacienteId(pacienteId).orElse(null);
        }

        // 3. Buscar si Triage ya creó una ConsultaMedica hoy para este expediente
        ConsultaMedica consulta = null;
        if (expediente != null) {
            consulta = consultaMedicaRepository
                    .findFirstByExpedienteIdAndFechaConsultaOrderByIdDesc(expediente.getId(), LocalDate.now())
                    .orElse(null);
        }

        if (consulta == null) {
            consulta = new ConsultaMedica();
            consulta.setFechaConsulta(LocalDate.now());
            consulta.setExpediente(expediente);
        }

        consulta.setMotivoConsulta(request.getMotivoConsulta() != null
                ? request.getMotivoConsulta()
                : (cita.getObservaciones() != null ? cita.getObservaciones() : "Consulta médica"));
        consulta.setNotasMedicas(request.getNotasMedicas());
        consulta.setUser(cita.getUser()); // Médico que atiende
        consulta = consultaMedicaRepository.save(consulta);
        LOG.debug("ConsultaMedica guardada/actualizada con ID: {}", consulta.getId());

        // 4. Vincular el Diagnóstico Principal (actualizar su FK consulta)
        if (request.getDiagnosticoPrincipalId() != null) {
            Diagnostico diag = diagnosticoRepository.findById(request.getDiagnosticoPrincipalId())
                    .orElse(null);
            if (diag != null) {
                diag.setConsulta(consulta);
                diagnosticoRepository.save(diag);
                LOG.debug("Diagnóstico ID {} vinculado a consulta ID {}", diag.getId(), consulta.getId());
            }
        }

        // 5. Crear las Recetas
        if (request.getRecetas() != null && !request.getRecetas().isEmpty()) {
            List<Receta> recetas = new ArrayList<>();
            for (FinalizarConsultaRequestDTO.RecetaRequestDTO rxReq : request.getRecetas()) {
                if (rxReq.getMedicamentoId() == null)
                    continue;
                Medicamento med = medicamentoRepository.findById(rxReq.getMedicamentoId()).orElse(null);
                if (med == null)
                    continue;

                Receta receta = new Receta();
                receta.setDosis(rxReq.getDosis());
                receta.setFrecuencia(rxReq.getFrecuencia());
                receta.setDuracion(rxReq.getDuracion());
                receta.setCantidad(rxReq.getCantidad() != null ? rxReq.getCantidad() : 1);
                receta.setMedicamento(med);
                receta.setConsulta(consulta);
                recetas.add(receta);
            }
            recetaRepository.saveAll(recetas);
            LOG.debug("{} recetas guardadas para consulta ID {}", recetas.size(), consulta.getId());
        }

        // 6. Actualizar el estado de la Cita a ATENDIDA
        cita.setEstado(EstadoCita.ATENDIDA);
        citaMedicaRepository.save(cita);
        LOG.debug("Cita ID {} marcada como ATENDIDA", citaId);
    }
}
