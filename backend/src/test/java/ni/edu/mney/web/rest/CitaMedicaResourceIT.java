package ni.edu.mney.web.rest;

import static ni.edu.mney.domain.CitaMedicaAsserts.*;
import static ni.edu.mney.web.rest.TestUtil.createUpdateProxyForBean;
import static ni.edu.mney.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import ni.edu.mney.IntegrationTest;
import ni.edu.mney.domain.CitaMedica;
import ni.edu.mney.domain.Paciente;
import ni.edu.mney.domain.User;
import ni.edu.mney.domain.enumeration.EstadoCita;
import ni.edu.mney.repository.CitaMedicaRepository;
import ni.edu.mney.repository.UserRepository;
import ni.edu.mney.service.CitaMedicaService;
import ni.edu.mney.service.dto.CitaMedicaDTO;
import ni.edu.mney.service.mapper.CitaMedicaMapper;
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
 * Integration tests for the {@link CitaMedicaResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class CitaMedicaResourceIT {

    private static final ZonedDateTime DEFAULT_FECHA_HORA = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_FECHA_HORA = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final ZonedDateTime SMALLER_FECHA_HORA = ZonedDateTime.ofInstant(Instant.ofEpochMilli(-1L), ZoneOffset.UTC);

    private static final EstadoCita DEFAULT_ESTADO = EstadoCita.PROGRAMADA;
    private static final EstadoCita UPDATED_ESTADO = EstadoCita.ATENDIDA;

    private static final String DEFAULT_OBSERVACIONES = "AAAAAAAAAA";
    private static final String UPDATED_OBSERVACIONES = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/cita-medicas";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private CitaMedicaRepository citaMedicaRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private CitaMedicaRepository citaMedicaRepositoryMock;

    @Autowired
    private CitaMedicaMapper citaMedicaMapper;

    @Mock
    private CitaMedicaService citaMedicaServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCitaMedicaMockMvc;

    private CitaMedica citaMedica;

    private CitaMedica insertedCitaMedica;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CitaMedica createEntity() {
        return new CitaMedica().fechaHora(DEFAULT_FECHA_HORA).estado(DEFAULT_ESTADO).observaciones(DEFAULT_OBSERVACIONES);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CitaMedica createUpdatedEntity() {
        return new CitaMedica().fechaHora(UPDATED_FECHA_HORA).estado(UPDATED_ESTADO).observaciones(UPDATED_OBSERVACIONES);
    }

    @BeforeEach
    void initTest() {
        citaMedica = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedCitaMedica != null) {
            citaMedicaRepository.delete(insertedCitaMedica);
            insertedCitaMedica = null;
        }
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    void createCitaMedica() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the CitaMedica
        CitaMedicaDTO citaMedicaDTO = citaMedicaMapper.toDto(citaMedica);
        var returnedCitaMedicaDTO = om.readValue(
            restCitaMedicaMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(citaMedicaDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            CitaMedicaDTO.class
        );

        // Validate the CitaMedica in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedCitaMedica = citaMedicaMapper.toEntity(returnedCitaMedicaDTO);
        assertCitaMedicaUpdatableFieldsEquals(returnedCitaMedica, getPersistedCitaMedica(returnedCitaMedica));

        insertedCitaMedica = returnedCitaMedica;
    }

    @Test
    @Transactional
    void createCitaMedicaWithExistingId() throws Exception {
        // Create the CitaMedica with an existing ID
        citaMedica.setId(1L);
        CitaMedicaDTO citaMedicaDTO = citaMedicaMapper.toDto(citaMedica);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCitaMedicaMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(citaMedicaDTO)))
            .andExpect(status().isBadRequest());

        // Validate the CitaMedica in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkFechaHoraIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        citaMedica.setFechaHora(null);

        // Create the CitaMedica, which fails.
        CitaMedicaDTO citaMedicaDTO = citaMedicaMapper.toDto(citaMedica);

        restCitaMedicaMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(citaMedicaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEstadoIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        citaMedica.setEstado(null);

        // Create the CitaMedica, which fails.
        CitaMedicaDTO citaMedicaDTO = citaMedicaMapper.toDto(citaMedica);

        restCitaMedicaMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(citaMedicaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllCitaMedicas() throws Exception {
        // Initialize the database
        insertedCitaMedica = citaMedicaRepository.saveAndFlush(citaMedica);

        // Get all the citaMedicaList
        restCitaMedicaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(citaMedica.getId().intValue())))
            .andExpect(jsonPath("$.[*].fechaHora").value(hasItem(sameInstant(DEFAULT_FECHA_HORA))))
            .andExpect(jsonPath("$.[*].estado").value(hasItem(DEFAULT_ESTADO.toString())))
            .andExpect(jsonPath("$.[*].observaciones").value(hasItem(DEFAULT_OBSERVACIONES)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllCitaMedicasWithEagerRelationshipsIsEnabled() throws Exception {
        when(citaMedicaServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restCitaMedicaMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(citaMedicaServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllCitaMedicasWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(citaMedicaServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restCitaMedicaMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(citaMedicaRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getCitaMedica() throws Exception {
        // Initialize the database
        insertedCitaMedica = citaMedicaRepository.saveAndFlush(citaMedica);

        // Get the citaMedica
        restCitaMedicaMockMvc
            .perform(get(ENTITY_API_URL_ID, citaMedica.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(citaMedica.getId().intValue()))
            .andExpect(jsonPath("$.fechaHora").value(sameInstant(DEFAULT_FECHA_HORA)))
            .andExpect(jsonPath("$.estado").value(DEFAULT_ESTADO.toString()))
            .andExpect(jsonPath("$.observaciones").value(DEFAULT_OBSERVACIONES));
    }

    @Test
    @Transactional
    void getCitaMedicasByIdFiltering() throws Exception {
        // Initialize the database
        insertedCitaMedica = citaMedicaRepository.saveAndFlush(citaMedica);

        Long id = citaMedica.getId();

        defaultCitaMedicaFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultCitaMedicaFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultCitaMedicaFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllCitaMedicasByFechaHoraIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCitaMedica = citaMedicaRepository.saveAndFlush(citaMedica);

        // Get all the citaMedicaList where fechaHora equals to
        defaultCitaMedicaFiltering("fechaHora.equals=" + DEFAULT_FECHA_HORA, "fechaHora.equals=" + UPDATED_FECHA_HORA);
    }

    @Test
    @Transactional
    void getAllCitaMedicasByFechaHoraIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCitaMedica = citaMedicaRepository.saveAndFlush(citaMedica);

        // Get all the citaMedicaList where fechaHora in
        defaultCitaMedicaFiltering("fechaHora.in=" + DEFAULT_FECHA_HORA + "," + UPDATED_FECHA_HORA, "fechaHora.in=" + UPDATED_FECHA_HORA);
    }

    @Test
    @Transactional
    void getAllCitaMedicasByFechaHoraIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCitaMedica = citaMedicaRepository.saveAndFlush(citaMedica);

        // Get all the citaMedicaList where fechaHora is not null
        defaultCitaMedicaFiltering("fechaHora.specified=true", "fechaHora.specified=false");
    }

    @Test
    @Transactional
    void getAllCitaMedicasByFechaHoraIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedCitaMedica = citaMedicaRepository.saveAndFlush(citaMedica);

        // Get all the citaMedicaList where fechaHora is greater than or equal to
        defaultCitaMedicaFiltering(
            "fechaHora.greaterThanOrEqual=" + DEFAULT_FECHA_HORA,
            "fechaHora.greaterThanOrEqual=" + UPDATED_FECHA_HORA
        );
    }

    @Test
    @Transactional
    void getAllCitaMedicasByFechaHoraIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedCitaMedica = citaMedicaRepository.saveAndFlush(citaMedica);

        // Get all the citaMedicaList where fechaHora is less than or equal to
        defaultCitaMedicaFiltering("fechaHora.lessThanOrEqual=" + DEFAULT_FECHA_HORA, "fechaHora.lessThanOrEqual=" + SMALLER_FECHA_HORA);
    }

    @Test
    @Transactional
    void getAllCitaMedicasByFechaHoraIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedCitaMedica = citaMedicaRepository.saveAndFlush(citaMedica);

        // Get all the citaMedicaList where fechaHora is less than
        defaultCitaMedicaFiltering("fechaHora.lessThan=" + UPDATED_FECHA_HORA, "fechaHora.lessThan=" + DEFAULT_FECHA_HORA);
    }

    @Test
    @Transactional
    void getAllCitaMedicasByFechaHoraIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedCitaMedica = citaMedicaRepository.saveAndFlush(citaMedica);

        // Get all the citaMedicaList where fechaHora is greater than
        defaultCitaMedicaFiltering("fechaHora.greaterThan=" + SMALLER_FECHA_HORA, "fechaHora.greaterThan=" + DEFAULT_FECHA_HORA);
    }

    @Test
    @Transactional
    void getAllCitaMedicasByEstadoIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCitaMedica = citaMedicaRepository.saveAndFlush(citaMedica);

        // Get all the citaMedicaList where estado equals to
        defaultCitaMedicaFiltering("estado.equals=" + DEFAULT_ESTADO, "estado.equals=" + UPDATED_ESTADO);
    }

    @Test
    @Transactional
    void getAllCitaMedicasByEstadoIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCitaMedica = citaMedicaRepository.saveAndFlush(citaMedica);

        // Get all the citaMedicaList where estado in
        defaultCitaMedicaFiltering("estado.in=" + DEFAULT_ESTADO + "," + UPDATED_ESTADO, "estado.in=" + UPDATED_ESTADO);
    }

    @Test
    @Transactional
    void getAllCitaMedicasByEstadoIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCitaMedica = citaMedicaRepository.saveAndFlush(citaMedica);

        // Get all the citaMedicaList where estado is not null
        defaultCitaMedicaFiltering("estado.specified=true", "estado.specified=false");
    }

    @Test
    @Transactional
    void getAllCitaMedicasByObservacionesIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCitaMedica = citaMedicaRepository.saveAndFlush(citaMedica);

        // Get all the citaMedicaList where observaciones equals to
        defaultCitaMedicaFiltering("observaciones.equals=" + DEFAULT_OBSERVACIONES, "observaciones.equals=" + UPDATED_OBSERVACIONES);
    }

    @Test
    @Transactional
    void getAllCitaMedicasByObservacionesIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCitaMedica = citaMedicaRepository.saveAndFlush(citaMedica);

        // Get all the citaMedicaList where observaciones in
        defaultCitaMedicaFiltering(
            "observaciones.in=" + DEFAULT_OBSERVACIONES + "," + UPDATED_OBSERVACIONES,
            "observaciones.in=" + UPDATED_OBSERVACIONES
        );
    }

    @Test
    @Transactional
    void getAllCitaMedicasByObservacionesIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCitaMedica = citaMedicaRepository.saveAndFlush(citaMedica);

        // Get all the citaMedicaList where observaciones is not null
        defaultCitaMedicaFiltering("observaciones.specified=true", "observaciones.specified=false");
    }

    @Test
    @Transactional
    void getAllCitaMedicasByObservacionesContainsSomething() throws Exception {
        // Initialize the database
        insertedCitaMedica = citaMedicaRepository.saveAndFlush(citaMedica);

        // Get all the citaMedicaList where observaciones contains
        defaultCitaMedicaFiltering("observaciones.contains=" + DEFAULT_OBSERVACIONES, "observaciones.contains=" + UPDATED_OBSERVACIONES);
    }

    @Test
    @Transactional
    void getAllCitaMedicasByObservacionesNotContainsSomething() throws Exception {
        // Initialize the database
        insertedCitaMedica = citaMedicaRepository.saveAndFlush(citaMedica);

        // Get all the citaMedicaList where observaciones does not contain
        defaultCitaMedicaFiltering(
            "observaciones.doesNotContain=" + UPDATED_OBSERVACIONES,
            "observaciones.doesNotContain=" + DEFAULT_OBSERVACIONES
        );
    }

    @Test
    @Transactional
    void getAllCitaMedicasByUserIsEqualToSomething() throws Exception {
        User user;
        if (TestUtil.findAll(em, User.class).isEmpty()) {
            citaMedicaRepository.saveAndFlush(citaMedica);
            user = UserResourceIT.createEntity();
        } else {
            user = TestUtil.findAll(em, User.class).get(0);
        }
        em.persist(user);
        em.flush();
        citaMedica.setUser(user);
        citaMedicaRepository.saveAndFlush(citaMedica);
        String userId = user.getId();
        // Get all the citaMedicaList where user equals to userId
        defaultCitaMedicaShouldBeFound("userId.equals=" + userId);

        // Get all the citaMedicaList where user equals to "invalid-id"
        defaultCitaMedicaShouldNotBeFound("userId.equals=" + "invalid-id");
    }

    @Test
    @Transactional
    void getAllCitaMedicasByPacienteIsEqualToSomething() throws Exception {
        Paciente paciente;
        if (TestUtil.findAll(em, Paciente.class).isEmpty()) {
            citaMedicaRepository.saveAndFlush(citaMedica);
            paciente = PacienteResourceIT.createEntity();
        } else {
            paciente = TestUtil.findAll(em, Paciente.class).get(0);
        }
        em.persist(paciente);
        em.flush();
        citaMedica.setPaciente(paciente);
        citaMedicaRepository.saveAndFlush(citaMedica);
        Long pacienteId = paciente.getId();
        // Get all the citaMedicaList where paciente equals to pacienteId
        defaultCitaMedicaShouldBeFound("pacienteId.equals=" + pacienteId);

        // Get all the citaMedicaList where paciente equals to (pacienteId + 1)
        defaultCitaMedicaShouldNotBeFound("pacienteId.equals=" + (pacienteId + 1));
    }

    private void defaultCitaMedicaFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultCitaMedicaShouldBeFound(shouldBeFound);
        defaultCitaMedicaShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultCitaMedicaShouldBeFound(String filter) throws Exception {
        restCitaMedicaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(citaMedica.getId().intValue())))
            .andExpect(jsonPath("$.[*].fechaHora").value(hasItem(sameInstant(DEFAULT_FECHA_HORA))))
            .andExpect(jsonPath("$.[*].estado").value(hasItem(DEFAULT_ESTADO.toString())))
            .andExpect(jsonPath("$.[*].observaciones").value(hasItem(DEFAULT_OBSERVACIONES)));

        // Check, that the count call also returns 1
        restCitaMedicaMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultCitaMedicaShouldNotBeFound(String filter) throws Exception {
        restCitaMedicaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCitaMedicaMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingCitaMedica() throws Exception {
        // Get the citaMedica
        restCitaMedicaMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingCitaMedica() throws Exception {
        // Initialize the database
        insertedCitaMedica = citaMedicaRepository.saveAndFlush(citaMedica);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the citaMedica
        CitaMedica updatedCitaMedica = citaMedicaRepository.findById(citaMedica.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedCitaMedica are not directly saved in db
        em.detach(updatedCitaMedica);
        updatedCitaMedica.fechaHora(UPDATED_FECHA_HORA).estado(UPDATED_ESTADO).observaciones(UPDATED_OBSERVACIONES);
        CitaMedicaDTO citaMedicaDTO = citaMedicaMapper.toDto(updatedCitaMedica);

        restCitaMedicaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, citaMedicaDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(citaMedicaDTO))
            )
            .andExpect(status().isOk());

        // Validate the CitaMedica in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedCitaMedicaToMatchAllProperties(updatedCitaMedica);
    }

    @Test
    @Transactional
    void putNonExistingCitaMedica() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        citaMedica.setId(longCount.incrementAndGet());

        // Create the CitaMedica
        CitaMedicaDTO citaMedicaDTO = citaMedicaMapper.toDto(citaMedica);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCitaMedicaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, citaMedicaDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(citaMedicaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CitaMedica in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCitaMedica() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        citaMedica.setId(longCount.incrementAndGet());

        // Create the CitaMedica
        CitaMedicaDTO citaMedicaDTO = citaMedicaMapper.toDto(citaMedica);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCitaMedicaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(citaMedicaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CitaMedica in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCitaMedica() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        citaMedica.setId(longCount.incrementAndGet());

        // Create the CitaMedica
        CitaMedicaDTO citaMedicaDTO = citaMedicaMapper.toDto(citaMedica);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCitaMedicaMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(citaMedicaDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the CitaMedica in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCitaMedicaWithPatch() throws Exception {
        // Initialize the database
        insertedCitaMedica = citaMedicaRepository.saveAndFlush(citaMedica);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the citaMedica using partial update
        CitaMedica partialUpdatedCitaMedica = new CitaMedica();
        partialUpdatedCitaMedica.setId(citaMedica.getId());

        partialUpdatedCitaMedica.fechaHora(UPDATED_FECHA_HORA).observaciones(UPDATED_OBSERVACIONES);

        restCitaMedicaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCitaMedica.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCitaMedica))
            )
            .andExpect(status().isOk());

        // Validate the CitaMedica in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCitaMedicaUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedCitaMedica, citaMedica),
            getPersistedCitaMedica(citaMedica)
        );
    }

    @Test
    @Transactional
    void fullUpdateCitaMedicaWithPatch() throws Exception {
        // Initialize the database
        insertedCitaMedica = citaMedicaRepository.saveAndFlush(citaMedica);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the citaMedica using partial update
        CitaMedica partialUpdatedCitaMedica = new CitaMedica();
        partialUpdatedCitaMedica.setId(citaMedica.getId());

        partialUpdatedCitaMedica.fechaHora(UPDATED_FECHA_HORA).estado(UPDATED_ESTADO).observaciones(UPDATED_OBSERVACIONES);

        restCitaMedicaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCitaMedica.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCitaMedica))
            )
            .andExpect(status().isOk());

        // Validate the CitaMedica in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCitaMedicaUpdatableFieldsEquals(partialUpdatedCitaMedica, getPersistedCitaMedica(partialUpdatedCitaMedica));
    }

    @Test
    @Transactional
    void patchNonExistingCitaMedica() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        citaMedica.setId(longCount.incrementAndGet());

        // Create the CitaMedica
        CitaMedicaDTO citaMedicaDTO = citaMedicaMapper.toDto(citaMedica);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCitaMedicaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, citaMedicaDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(citaMedicaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CitaMedica in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCitaMedica() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        citaMedica.setId(longCount.incrementAndGet());

        // Create the CitaMedica
        CitaMedicaDTO citaMedicaDTO = citaMedicaMapper.toDto(citaMedica);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCitaMedicaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(citaMedicaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CitaMedica in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCitaMedica() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        citaMedica.setId(longCount.incrementAndGet());

        // Create the CitaMedica
        CitaMedicaDTO citaMedicaDTO = citaMedicaMapper.toDto(citaMedica);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCitaMedicaMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(citaMedicaDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the CitaMedica in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCitaMedica() throws Exception {
        // Initialize the database
        insertedCitaMedica = citaMedicaRepository.saveAndFlush(citaMedica);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the citaMedica
        restCitaMedicaMockMvc
            .perform(delete(ENTITY_API_URL_ID, citaMedica.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return citaMedicaRepository.count();
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

    protected CitaMedica getPersistedCitaMedica(CitaMedica citaMedica) {
        return citaMedicaRepository.findById(citaMedica.getId()).orElseThrow();
    }

    protected void assertPersistedCitaMedicaToMatchAllProperties(CitaMedica expectedCitaMedica) {
        assertCitaMedicaAllPropertiesEquals(expectedCitaMedica, getPersistedCitaMedica(expectedCitaMedica));
    }

    protected void assertPersistedCitaMedicaToMatchUpdatableProperties(CitaMedica expectedCitaMedica) {
        assertCitaMedicaAllUpdatablePropertiesEquals(expectedCitaMedica, getPersistedCitaMedica(expectedCitaMedica));
    }
}
