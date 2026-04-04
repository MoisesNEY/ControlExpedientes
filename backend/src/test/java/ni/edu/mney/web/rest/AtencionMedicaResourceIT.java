package ni.edu.mney.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import ni.edu.mney.IntegrationTest;
import ni.edu.mney.domain.*;
import ni.edu.mney.domain.enumeration.Sexo;
import ni.edu.mney.repository.*;
import ni.edu.mney.security.AuthoritiesConstants;
import ni.edu.mney.service.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link AtencionMedicaResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.MEDICO)
class AtencionMedicaResourceIT {

    @Autowired
    private MockMvc restAtencionMedicaMockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private EntityManager em;

    @Autowired
    private ConsultaMedicaRepository consultaMedicaRepository;

    @Autowired
    private SignosVitalesRepository signosVitalesRepository;

    @Autowired
    private DiagnosticoRepository diagnosticoRepository;

    @Autowired
    private RecetaRepository recetaRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Autowired
    private ExpedienteClinicoRepository expedienteClinicoRepository;

    @Autowired
    private AuditoriaAccionesRepository auditoriaAccionesRepository;

    private ExpedienteClinico expediente;
    private Medicamento medicamento;

    @BeforeEach
    public void initTest() {
        // Setup Patient and Expediente
        Paciente paciente = new Paciente();
        paciente.setCodigo("PAC-0001");
        paciente.setNombres("Juan");
        paciente.setApellidos("Perez");
        paciente.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        paciente.setSexo(Sexo.MASCULINO);
        paciente.setTelefono("12345678");
        paciente.setActivo(true);
        em.persist(paciente);

        expediente = new ExpedienteClinico();
        expediente.setNumeroExpediente("EXP-2024-0001");
        expediente.setPaciente(paciente);
        em.persist(expediente);

        medicamento = new Medicamento();
        medicamento.setNombre("Paracetamol");
        medicamento.setStock(100);
        em.persist(medicamento);

        em.flush();
    }

    @Test
    @Transactional
    void testFinalizarConsultaHappyPath() throws Exception {
        int databaseSizeBefore = consultaMedicaRepository.findAll().size();
        int auditSizeBefore = auditoriaAccionesRepository.findAll().size();

        // Create DTO
        AtencionMedicaDTO vm = new AtencionMedicaDTO();

        // 1. Consulta
        ConsultaMedicaDTO consultaDTO = new ConsultaMedicaDTO();
        consultaDTO.setFechaConsulta(LocalDate.now());
        consultaDTO.setMotivoConsulta("Dolor de cabeza severo");
        consultaDTO.setNotasMedicas("Paciente estable");

        ExpedienteClinicoDTO expedienteDTO = new ExpedienteClinicoDTO();
        expedienteDTO.setId(expediente.getId());
        consultaDTO.setExpediente(expedienteDTO);
        vm.setConsulta(consultaDTO);

        // 2. Signos Vitales
        SignosVitalesDTO svDTO = new SignosVitalesDTO();
        svDTO.setPresionArterial("120/80");
        svDTO.setFrecuenciaCardiaca(75);
        svDTO.setTemperatura(36.5);
        svDTO.setPeso(70.0);
        vm.setSignosVitales(svDTO);

        // 3. Diagnóstico
        DiagnosticoDTO diagDTO = new DiagnosticoDTO();
        diagDTO.setCodigoCIE("R51");
        diagDTO.setDescripcion("Cefalea");
        vm.setDiagnostico(diagDTO);

        // 4. Receta
        RecetaDTO recetaDTO = new RecetaDTO();
        recetaDTO.setCantidad(2);
        recetaDTO.setDosis("500mg");
        recetaDTO.setFrecuencia("Cada 8 horas");
        recetaDTO.setDuracion("3 dias");

        MedicamentoDTO medDTO = new MedicamentoDTO();
        medDTO.setId(medicamento.getId());
        recetaDTO.setMedicamento(medDTO);
        vm.setRecetas(Collections.singletonList(recetaDTO));

        // Execute
        restAtencionMedicaMockMvc.perform(post("/api/atencion-medica/finalizar-consulta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(vm)))
                .andExpect(status().isOk());

        // Validate
        List<ConsultaMedica> consultas = consultaMedicaRepository.findAll();
        assertThat(consultas).hasSize(databaseSizeBefore + 1);
        ConsultaMedica testConsulta = consultas.get(consultas.size() - 1);
        assertThat(testConsulta.getMotivoConsulta()).isEqualTo("Dolor de cabeza severo");

        // Validate Signos Vitales
        assertThat(signosVitalesRepository.findAll())
                .anyMatch(sv -> sv.getConsulta().getId().equals(testConsulta.getId()));

        // Validate Diagnóstico
        assertThat(diagnosticoRepository.findAll())
                .anyMatch(d -> d.getConsulta().getId().equals(testConsulta.getId()) && d.getCodigoCIE().equals("R51"));

        // Validate Receta and Stock
        assertThat(recetaRepository.findAll())
                .anyMatch(r -> r.getConsulta().getId().equals(testConsulta.getId()) && r.getCantidad().equals(2));

        Medicamento updatedMed = medicamentoRepository.findById(medicamento.getId()).get();
        assertThat(updatedMed.getStock()).isEqualTo(98); // 100 - 2

        // Validate Audit (clinical audit aspect from Task 6.1)
        List<AuditoriaAcciones> audits = auditoriaAccionesRepository.findAll();
        assertThat(audits.size()).isGreaterThan(auditSizeBefore);
        assertThat(audits)
                .anyMatch(a -> a.getDescripcion().contains("Juan Perez") && a.getEntidad().equals("Diagnostico"));
    }
}
