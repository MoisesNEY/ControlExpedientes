package ni.edu.mney.web.rest;

import static ni.edu.mney.domain.ConsultaMedicaAsserts.*;
import static ni.edu.mney.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import ni.edu.mney.IntegrationTest;
import ni.edu.mney.domain.ConsultaMedica;
import ni.edu.mney.domain.ExpedienteClinico;
import ni.edu.mney.domain.User;
import ni.edu.mney.repository.ConsultaMedicaRepository;
import ni.edu.mney.repository.UserRepository;
import ni.edu.mney.service.ConsultaMedicaService;
import ni.edu.mney.service.dto.ConsultaMedicaDTO;
import ni.edu.mney.service.mapper.ConsultaMedicaMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ConsultaMedicaResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class ConsultaMedicaResourceIT {

    private static final LocalDate DEFAULT_FECHA_CONSULTA = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_FECHA_CONSULTA = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_FECHA_CONSULTA = LocalDate.ofEpochDay(-1L);

    private static final String DEFAULT_MOTIVO_CONSULTA = "AAAAAAAAAA";
    private static final String UPDATED_MOTIVO_CONSULTA = "BBBBBBBBBB";

    private static final String DEFAULT_NOTAS_MEDICAS = "AAAAAAAAAA";
    private static final String UPDATED_NOTAS_MEDICAS = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/consulta-medicas";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ConsultaMedicaRepository consultaMedicaRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private ConsultaMedicaRepository consultaMedicaRepositoryMock;

    @Autowired
    private ConsultaMedicaMapper consultaMedicaMapper;

    @Mock
    private ConsultaMedicaService consultaMedicaServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restConsultaMedicaMockMvc;

    private ConsultaMedica consultaMedica;

    private ConsultaMedica insertedConsultaMedica;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ConsultaMedica createEntity() {
        return new ConsultaMedica()
            .fechaConsulta(DEFAULT_FECHA_CONSULTA)
            .motivoConsulta(DEFAULT_MOTIVO_CONSULTA)
            .notasMedicas(DEFAULT_NOTAS_MEDICAS);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ConsultaMedica createUpdatedEntity() {
        return new ConsultaMedica()
            .fechaConsulta(UPDATED_FECHA_CONSULTA)
            .motivoConsulta(UPDATED_MOTIVO_CONSULTA)
            .notasMedicas(UPDATED_NOTAS_MEDICAS);
    }

    @BeforeEach
    void initTest() {
        consultaMedica = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedConsultaMedica != null) {
            consultaMedicaRepository.delete(insertedConsultaMedica);
            insertedConsultaMedica = null;
        }
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    void createConsultaMedica() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the ConsultaMedica
        ConsultaMedicaDTO consultaMedicaDTO = consultaMedicaMapper.toDto(consultaMedica);
        var returnedConsultaMedicaDTO = om.readValue(
            restConsultaMedicaMockMvc
                .perform(
                    post(ENTITY_API_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(consultaMedicaDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ConsultaMedicaDTO.class
        );

        // Validate the ConsultaMedica in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedConsultaMedica = consultaMedicaMapper.toEntity(returnedConsultaMedicaDTO);
        assertConsultaMedicaUpdatableFieldsEquals(returnedConsultaMedica, getPersistedConsultaMedica(returnedConsultaMedica));

        insertedConsultaMedica = returnedConsultaMedica;
    }

    @Test
    @Transactional
    void createConsultaMedicaWithExistingId() throws Exception {
        // Create the ConsultaMedica with an existing ID
        consultaMedica.setId(1L);
        ConsultaMedicaDTO consultaMedicaDTO = consultaMedicaMapper.toDto(consultaMedica);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restConsultaMedicaMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(consultaMedicaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ConsultaMedica in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkFechaConsultaIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        consultaMedica.setFechaConsulta(null);

        // Create the ConsultaMedica, which fails.
        ConsultaMedicaDTO consultaMedicaDTO = consultaMedicaMapper.toDto(consultaMedica);

        restConsultaMedicaMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(consultaMedicaDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkMotivoConsultaIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        consultaMedica.setMotivoConsulta(null);

        // Create the ConsultaMedica, which fails.
        ConsultaMedicaDTO consultaMedicaDTO = consultaMedicaMapper.toDto(consultaMedica);

        restConsultaMedicaMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(consultaMedicaDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllConsultaMedicas() throws Exception {
        // Initialize the database
        insertedConsultaMedica = consultaMedicaRepository.saveAndFlush(consultaMedica);

        // Get all the consultaMedicaList
        restConsultaMedicaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(consultaMedica.getId().intValue())))
            .andExpect(jsonPath("$.[*].fechaConsulta").value(hasItem(DEFAULT_FECHA_CONSULTA.toString())))
            .andExpect(jsonPath("$.[*].motivoConsulta").value(hasItem(DEFAULT_MOTIVO_CONSULTA)))
            .andExpect(jsonPath("$.[*].notasMedicas").value(hasItem(DEFAULT_NOTAS_MEDICAS)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllConsultaMedicasWithEagerRelationshipsIsEnabled() throws Exception {
        when(consultaMedicaServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restConsultaMedicaMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(consultaMedicaServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllConsultaMedicasWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(consultaMedicaServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restConsultaMedicaMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(consultaMedicaRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getConsultaMedica() throws Exception {
        // Initialize the database
        insertedConsultaMedica = consultaMedicaRepository.saveAndFlush(consultaMedica);

        // Get the consultaMedica
        restConsultaMedicaMockMvc
            .perform(get(ENTITY_API_URL_ID, consultaMedica.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(consultaMedica.getId().intValue()))
            .andExpect(jsonPath("$.fechaConsulta").value(DEFAULT_FECHA_CONSULTA.toString()))
            .andExpect(jsonPath("$.motivoConsulta").value(DEFAULT_MOTIVO_CONSULTA))
            .andExpect(jsonPath("$.notasMedicas").value(DEFAULT_NOTAS_MEDICAS));
    }

    @Test
    @Transactional
    void getConsultaMedicasByIdFiltering() throws Exception {
        // Initialize the database
        insertedConsultaMedica = consultaMedicaRepository.saveAndFlush(consultaMedica);

        Long id = consultaMedica.getId();

        defaultConsultaMedicaFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultConsultaMedicaFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultConsultaMedicaFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllConsultaMedicasByFechaConsultaIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedConsultaMedica = consultaMedicaRepository.saveAndFlush(consultaMedica);

        // Get all the consultaMedicaList where fechaConsulta equals to
        defaultConsultaMedicaFiltering("fechaConsulta.equals=" + DEFAULT_FECHA_CONSULTA, "fechaConsulta.equals=" + UPDATED_FECHA_CONSULTA);
    }

    @Test
    @Transactional
    void getAllConsultaMedicasByFechaConsultaIsInShouldWork() throws Exception {
        // Initialize the database
        insertedConsultaMedica = consultaMedicaRepository.saveAndFlush(consultaMedica);

        // Get all the consultaMedicaList where fechaConsulta in
        defaultConsultaMedicaFiltering(
            "fechaConsulta.in=" + DEFAULT_FECHA_CONSULTA + "," + UPDATED_FECHA_CONSULTA,
            "fechaConsulta.in=" + UPDATED_FECHA_CONSULTA
        );
    }

    @Test
    @Transactional
    void getAllConsultaMedicasByFechaConsultaIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedConsultaMedica = consultaMedicaRepository.saveAndFlush(consultaMedica);

        // Get all the consultaMedicaList where fechaConsulta is not null
        defaultConsultaMedicaFiltering("fechaConsulta.specified=true", "fechaConsulta.specified=false");
    }

    @Test
    @Transactional
    void getAllConsultaMedicasByFechaConsultaIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedConsultaMedica = consultaMedicaRepository.saveAndFlush(consultaMedica);

        // Get all the consultaMedicaList where fechaConsulta is greater than or equal to
        defaultConsultaMedicaFiltering(
            "fechaConsulta.greaterThanOrEqual=" + DEFAULT_FECHA_CONSULTA,
            "fechaConsulta.greaterThanOrEqual=" + UPDATED_FECHA_CONSULTA
        );
    }

    @Test
    @Transactional
    void getAllConsultaMedicasByFechaConsultaIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedConsultaMedica = consultaMedicaRepository.saveAndFlush(consultaMedica);

        // Get all the consultaMedicaList where fechaConsulta is less than or equal to
        defaultConsultaMedicaFiltering(
            "fechaConsulta.lessThanOrEqual=" + DEFAULT_FECHA_CONSULTA,
            "fechaConsulta.lessThanOrEqual=" + SMALLER_FECHA_CONSULTA
        );
    }

    @Test
    @Transactional
    void getAllConsultaMedicasByFechaConsultaIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedConsultaMedica = consultaMedicaRepository.saveAndFlush(consultaMedica);

        // Get all the consultaMedicaList where fechaConsulta is less than
        defaultConsultaMedicaFiltering(
            "fechaConsulta.lessThan=" + UPDATED_FECHA_CONSULTA,
            "fechaConsulta.lessThan=" + DEFAULT_FECHA_CONSULTA
        );
    }

    @Test
    @Transactional
    void getAllConsultaMedicasByFechaConsultaIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedConsultaMedica = consultaMedicaRepository.saveAndFlush(consultaMedica);

        // Get all the consultaMedicaList where fechaConsulta is greater than
        defaultConsultaMedicaFiltering(
            "fechaConsulta.greaterThan=" + SMALLER_FECHA_CONSULTA,
            "fechaConsulta.greaterThan=" + DEFAULT_FECHA_CONSULTA
        );
    }

    @Test
    @Transactional
    void getAllConsultaMedicasByMotivoConsultaIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedConsultaMedica = consultaMedicaRepository.saveAndFlush(consultaMedica);

        // Get all the consultaMedicaList where motivoConsulta equals to
        defaultConsultaMedicaFiltering(
            "motivoConsulta.equals=" + DEFAULT_MOTIVO_CONSULTA,
            "motivoConsulta.equals=" + UPDATED_MOTIVO_CONSULTA
        );
    }

    @Test
    @Transactional
    void getAllConsultaMedicasByMotivoConsultaIsInShouldWork() throws Exception {
        // Initialize the database
        insertedConsultaMedica = consultaMedicaRepository.saveAndFlush(consultaMedica);

        // Get all the consultaMedicaList where motivoConsulta in
        defaultConsultaMedicaFiltering(
            "motivoConsulta.in=" + DEFAULT_MOTIVO_CONSULTA + "," + UPDATED_MOTIVO_CONSULTA,
            "motivoConsulta.in=" + UPDATED_MOTIVO_CONSULTA
        );
    }

    @Test
    @Transactional
    void getAllConsultaMedicasByMotivoConsultaIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedConsultaMedica = consultaMedicaRepository.saveAndFlush(consultaMedica);

        // Get all the consultaMedicaList where motivoConsulta is not null
        defaultConsultaMedicaFiltering("motivoConsulta.specified=true", "motivoConsulta.specified=false");
    }

    @Test
    @Transactional
    void getAllConsultaMedicasByMotivoConsultaContainsSomething() throws Exception {
        // Initialize the database
        insertedConsultaMedica = consultaMedicaRepository.saveAndFlush(consultaMedica);

        // Get all the consultaMedicaList where motivoConsulta contains
        defaultConsultaMedicaFiltering(
            "motivoConsulta.contains=" + DEFAULT_MOTIVO_CONSULTA,
            "motivoConsulta.contains=" + UPDATED_MOTIVO_CONSULTA
        );
    }

    @Test
    @Transactional
    void getAllConsultaMedicasByMotivoConsultaNotContainsSomething() throws Exception {
        // Initialize the database
        insertedConsultaMedica = consultaMedicaRepository.saveAndFlush(consultaMedica);

        // Get all the consultaMedicaList where motivoConsulta does not contain
        defaultConsultaMedicaFiltering(
            "motivoConsulta.doesNotContain=" + UPDATED_MOTIVO_CONSULTA,
            "motivoConsulta.doesNotContain=" + DEFAULT_MOTIVO_CONSULTA
        );
    }

    @Test
    @Transactional
    void getAllConsultaMedicasByNotasMedicasIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedConsultaMedica = consultaMedicaRepository.saveAndFlush(consultaMedica);

        // Get all the consultaMedicaList where notasMedicas equals to
        defaultConsultaMedicaFiltering("notasMedicas.equals=" + DEFAULT_NOTAS_MEDICAS, "notasMedicas.equals=" + UPDATED_NOTAS_MEDICAS);
    }

    @Test
    @Transactional
    void getAllConsultaMedicasByNotasMedicasIsInShouldWork() throws Exception {
        // Initialize the database
        insertedConsultaMedica = consultaMedicaRepository.saveAndFlush(consultaMedica);

        // Get all the consultaMedicaList where notasMedicas in
        defaultConsultaMedicaFiltering(
            "notasMedicas.in=" + DEFAULT_NOTAS_MEDICAS + "," + UPDATED_NOTAS_MEDICAS,
            "notasMedicas.in=" + UPDATED_NOTAS_MEDICAS
        );
    }

    @Test
    @Transactional
    void getAllConsultaMedicasByNotasMedicasIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedConsultaMedica = consultaMedicaRepository.saveAndFlush(consultaMedica);

        // Get all the consultaMedicaList where notasMedicas is not null
        defaultConsultaMedicaFiltering("notasMedicas.specified=true", "notasMedicas.specified=false");
    }

    @Test
    @Transactional
    void getAllConsultaMedicasByNotasMedicasContainsSomething() throws Exception {
        // Initialize the database
        insertedConsultaMedica = consultaMedicaRepository.saveAndFlush(consultaMedica);

        // Get all the consultaMedicaList where notasMedicas contains
        defaultConsultaMedicaFiltering("notasMedicas.contains=" + DEFAULT_NOTAS_MEDICAS, "notasMedicas.contains=" + UPDATED_NOTAS_MEDICAS);
    }

    @Test
    @Transactional
    void getAllConsultaMedicasByNotasMedicasNotContainsSomething() throws Exception {
        // Initialize the database
        insertedConsultaMedica = consultaMedicaRepository.saveAndFlush(consultaMedica);

        // Get all the consultaMedicaList where notasMedicas does not contain
        defaultConsultaMedicaFiltering(
            "notasMedicas.doesNotContain=" + UPDATED_NOTAS_MEDICAS,
            "notasMedicas.doesNotContain=" + DEFAULT_NOTAS_MEDICAS
        );
    }

    @Test
    @Transactional
    void getAllConsultaMedicasByUserIsEqualToSomething() throws Exception {
        User user;
        if (TestUtil.findAll(em, User.class).isEmpty()) {
            consultaMedicaRepository.saveAndFlush(consultaMedica);
            user = UserResourceIT.createEntity();
        } else {
            user = TestUtil.findAll(em, User.class).get(0);
        }
        em.persist(user);
        em.flush();
        consultaMedica.setUser(user);
        consultaMedicaRepository.saveAndFlush(consultaMedica);
        String userId = user.getId();
        // Get all the consultaMedicaList where user equals to userId
        defaultConsultaMedicaShouldBeFound("userId.equals=" + userId);

        // Get all the consultaMedicaList where user equals to "invalid-id"
        defaultConsultaMedicaShouldNotBeFound("userId.equals=" + "invalid-id");
    }

    @Test
    @Transactional
    void getAllConsultaMedicasByExpedienteIsEqualToSomething() throws Exception {
        ExpedienteClinico expediente;
        if (TestUtil.findAll(em, ExpedienteClinico.class).isEmpty()) {
            consultaMedicaRepository.saveAndFlush(consultaMedica);
            expediente = ExpedienteClinicoResourceIT.createEntity();
        } else {
            expediente = TestUtil.findAll(em, ExpedienteClinico.class).get(0);
        }
        em.persist(expediente);
        em.flush();
        consultaMedica.setExpediente(expediente);
        consultaMedicaRepository.saveAndFlush(consultaMedica);
        Long expedienteId = expediente.getId();
        // Get all the consultaMedicaList where expediente equals to expedienteId
        defaultConsultaMedicaShouldBeFound("expedienteId.equals=" + expedienteId);

        // Get all the consultaMedicaList where expediente equals to (expedienteId + 1)
        defaultConsultaMedicaShouldNotBeFound("expedienteId.equals=" + (expedienteId + 1));
    }

    private void defaultConsultaMedicaFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultConsultaMedicaShouldBeFound(shouldBeFound);
        defaultConsultaMedicaShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultConsultaMedicaShouldBeFound(String filter) throws Exception {
        restConsultaMedicaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(consultaMedica.getId().intValue())))
            .andExpect(jsonPath("$.[*].fechaConsulta").value(hasItem(DEFAULT_FECHA_CONSULTA.toString())))
            .andExpect(jsonPath("$.[*].motivoConsulta").value(hasItem(DEFAULT_MOTIVO_CONSULTA)))
            .andExpect(jsonPath("$.[*].notasMedicas").value(hasItem(DEFAULT_NOTAS_MEDICAS)));

        // Check, that the count call also returns 1
        restConsultaMedicaMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultConsultaMedicaShouldNotBeFound(String filter) throws Exception {
        restConsultaMedicaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restConsultaMedicaMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingConsultaMedica() throws Exception {
        // Get the consultaMedica
        restConsultaMedicaMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingConsultaMedica() throws Exception {
        // Initialize the database
        insertedConsultaMedica = consultaMedicaRepository.saveAndFlush(consultaMedica);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the consultaMedica
        ConsultaMedica updatedConsultaMedica = consultaMedicaRepository.findById(consultaMedica.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedConsultaMedica are not directly saved in db
        em.detach(updatedConsultaMedica);
        updatedConsultaMedica
            .fechaConsulta(UPDATED_FECHA_CONSULTA)
            .motivoConsulta(UPDATED_MOTIVO_CONSULTA)
            .notasMedicas(UPDATED_NOTAS_MEDICAS);
        ConsultaMedicaDTO consultaMedicaDTO = consultaMedicaMapper.toDto(updatedConsultaMedica);

        restConsultaMedicaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, consultaMedicaDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(consultaMedicaDTO))
            )
            .andExpect(status().isOk());

        // Validate the ConsultaMedica in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedConsultaMedicaToMatchAllProperties(updatedConsultaMedica);
    }

    @Test
    @Transactional
    void putNonExistingConsultaMedica() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        consultaMedica.setId(longCount.incrementAndGet());

        // Create the ConsultaMedica
        ConsultaMedicaDTO consultaMedicaDTO = consultaMedicaMapper.toDto(consultaMedica);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restConsultaMedicaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, consultaMedicaDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(consultaMedicaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ConsultaMedica in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchConsultaMedica() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        consultaMedica.setId(longCount.incrementAndGet());

        // Create the ConsultaMedica
        ConsultaMedicaDTO consultaMedicaDTO = consultaMedicaMapper.toDto(consultaMedica);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restConsultaMedicaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(consultaMedicaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ConsultaMedica in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamConsultaMedica() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        consultaMedica.setId(longCount.incrementAndGet());

        // Create the ConsultaMedica
        ConsultaMedicaDTO consultaMedicaDTO = consultaMedicaMapper.toDto(consultaMedica);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restConsultaMedicaMockMvc
            .perform(
                put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(consultaMedicaDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the ConsultaMedica in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateConsultaMedicaWithPatch() throws Exception {
        // Initialize the database
        insertedConsultaMedica = consultaMedicaRepository.saveAndFlush(consultaMedica);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the consultaMedica using partial update
        ConsultaMedica partialUpdatedConsultaMedica = new ConsultaMedica();
        partialUpdatedConsultaMedica.setId(consultaMedica.getId());

        partialUpdatedConsultaMedica.motivoConsulta(UPDATED_MOTIVO_CONSULTA).notasMedicas(UPDATED_NOTAS_MEDICAS);

        restConsultaMedicaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedConsultaMedica.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedConsultaMedica))
            )
            .andExpect(status().isOk());

        // Validate the ConsultaMedica in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertConsultaMedicaUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedConsultaMedica, consultaMedica),
            getPersistedConsultaMedica(consultaMedica)
        );
    }

    @Test
    @Transactional
    void fullUpdateConsultaMedicaWithPatch() throws Exception {
        // Initialize the database
        insertedConsultaMedica = consultaMedicaRepository.saveAndFlush(consultaMedica);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the consultaMedica using partial update
        ConsultaMedica partialUpdatedConsultaMedica = new ConsultaMedica();
        partialUpdatedConsultaMedica.setId(consultaMedica.getId());

        partialUpdatedConsultaMedica
            .fechaConsulta(UPDATED_FECHA_CONSULTA)
            .motivoConsulta(UPDATED_MOTIVO_CONSULTA)
            .notasMedicas(UPDATED_NOTAS_MEDICAS);

        restConsultaMedicaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedConsultaMedica.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedConsultaMedica))
            )
            .andExpect(status().isOk());

        // Validate the ConsultaMedica in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertConsultaMedicaUpdatableFieldsEquals(partialUpdatedConsultaMedica, getPersistedConsultaMedica(partialUpdatedConsultaMedica));
    }

    @Test
    @Transactional
    void patchNonExistingConsultaMedica() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        consultaMedica.setId(longCount.incrementAndGet());

        // Create the ConsultaMedica
        ConsultaMedicaDTO consultaMedicaDTO = consultaMedicaMapper.toDto(consultaMedica);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restConsultaMedicaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, consultaMedicaDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(consultaMedicaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ConsultaMedica in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchConsultaMedica() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        consultaMedica.setId(longCount.incrementAndGet());

        // Create the ConsultaMedica
        ConsultaMedicaDTO consultaMedicaDTO = consultaMedicaMapper.toDto(consultaMedica);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restConsultaMedicaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(consultaMedicaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ConsultaMedica in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamConsultaMedica() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        consultaMedica.setId(longCount.incrementAndGet());

        // Create the ConsultaMedica
        ConsultaMedicaDTO consultaMedicaDTO = consultaMedicaMapper.toDto(consultaMedica);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restConsultaMedicaMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(consultaMedicaDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the ConsultaMedica in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteConsultaMedica() throws Exception {
        // Initialize the database
        insertedConsultaMedica = consultaMedicaRepository.saveAndFlush(consultaMedica);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the consultaMedica
        restConsultaMedicaMockMvc
            .perform(delete(ENTITY_API_URL_ID, consultaMedica.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return consultaMedicaRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected ConsultaMedica getPersistedConsultaMedica(ConsultaMedica consultaMedica) {
        return consultaMedicaRepository.findById(consultaMedica.getId()).orElseThrow();
    }

    protected void assertPersistedConsultaMedicaToMatchAllProperties(ConsultaMedica expectedConsultaMedica) {
        assertConsultaMedicaAllPropertiesEquals(expectedConsultaMedica, getPersistedConsultaMedica(expectedConsultaMedica));
    }

    protected void assertPersistedConsultaMedicaToMatchUpdatableProperties(ConsultaMedica expectedConsultaMedica) {
        assertConsultaMedicaAllUpdatablePropertiesEquals(expectedConsultaMedica, getPersistedConsultaMedica(expectedConsultaMedica));
    }
}
