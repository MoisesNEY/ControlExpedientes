package ni.edu.mney.web.rest;

import static ni.edu.mney.domain.AuditoriaAccionesAsserts.*;
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
import ni.edu.mney.domain.AuditoriaAcciones;
import ni.edu.mney.domain.User;
import ni.edu.mney.repository.AuditoriaAccionesRepository;
import ni.edu.mney.repository.UserRepository;
import ni.edu.mney.service.AuditoriaAccionesService;
import ni.edu.mney.service.dto.AuditoriaAccionesDTO;
import ni.edu.mney.service.mapper.AuditoriaAccionesMapper;
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
 * Integration tests for the {@link AuditoriaAccionesResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class AuditoriaAccionesResourceIT {

    private static final String DEFAULT_ENTIDAD = "AAAAAAAAAA";
    private static final String UPDATED_ENTIDAD = "BBBBBBBBBB";

    private static final String DEFAULT_ACCION = "AAAAAAAAAA";
    private static final String UPDATED_ACCION = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_FECHA = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_FECHA = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final ZonedDateTime SMALLER_FECHA = ZonedDateTime.ofInstant(Instant.ofEpochMilli(-1L), ZoneOffset.UTC);

    private static final String DEFAULT_DESCRIPCION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPCION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/auditoria-acciones";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private AuditoriaAccionesRepository auditoriaAccionesRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private AuditoriaAccionesRepository auditoriaAccionesRepositoryMock;

    @Autowired
    private AuditoriaAccionesMapper auditoriaAccionesMapper;

    @Mock
    private AuditoriaAccionesService auditoriaAccionesServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAuditoriaAccionesMockMvc;

    private AuditoriaAcciones auditoriaAcciones;

    private AuditoriaAcciones insertedAuditoriaAcciones;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AuditoriaAcciones createEntity() {
        return new AuditoriaAcciones()
            .entidad(DEFAULT_ENTIDAD)
            .accion(DEFAULT_ACCION)
            .fecha(DEFAULT_FECHA)
            .descripcion(DEFAULT_DESCRIPCION);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AuditoriaAcciones createUpdatedEntity() {
        return new AuditoriaAcciones()
            .entidad(UPDATED_ENTIDAD)
            .accion(UPDATED_ACCION)
            .fecha(UPDATED_FECHA)
            .descripcion(UPDATED_DESCRIPCION);
    }

    @BeforeEach
    void initTest() {
        auditoriaAcciones = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedAuditoriaAcciones != null) {
            auditoriaAccionesRepository.delete(insertedAuditoriaAcciones);
            insertedAuditoriaAcciones = null;
        }
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    void createAuditoriaAcciones() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the AuditoriaAcciones
        AuditoriaAccionesDTO auditoriaAccionesDTO = auditoriaAccionesMapper.toDto(auditoriaAcciones);
        var returnedAuditoriaAccionesDTO = om.readValue(
            restAuditoriaAccionesMockMvc
                .perform(
                    post(ENTITY_API_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(auditoriaAccionesDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            AuditoriaAccionesDTO.class
        );

        // Validate the AuditoriaAcciones in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedAuditoriaAcciones = auditoriaAccionesMapper.toEntity(returnedAuditoriaAccionesDTO);
        assertAuditoriaAccionesUpdatableFieldsEquals(returnedAuditoriaAcciones, getPersistedAuditoriaAcciones(returnedAuditoriaAcciones));

        insertedAuditoriaAcciones = returnedAuditoriaAcciones;
    }

    @Test
    @Transactional
    void createAuditoriaAccionesWithExistingId() throws Exception {
        // Create the AuditoriaAcciones with an existing ID
        auditoriaAcciones.setId(1L);
        AuditoriaAccionesDTO auditoriaAccionesDTO = auditoriaAccionesMapper.toDto(auditoriaAcciones);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAuditoriaAccionesMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(auditoriaAccionesDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AuditoriaAcciones in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkEntidadIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        auditoriaAcciones.setEntidad(null);

        // Create the AuditoriaAcciones, which fails.
        AuditoriaAccionesDTO auditoriaAccionesDTO = auditoriaAccionesMapper.toDto(auditoriaAcciones);

        restAuditoriaAccionesMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(auditoriaAccionesDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkAccionIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        auditoriaAcciones.setAccion(null);

        // Create the AuditoriaAcciones, which fails.
        AuditoriaAccionesDTO auditoriaAccionesDTO = auditoriaAccionesMapper.toDto(auditoriaAcciones);

        restAuditoriaAccionesMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(auditoriaAccionesDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkFechaIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        auditoriaAcciones.setFecha(null);

        // Create the AuditoriaAcciones, which fails.
        AuditoriaAccionesDTO auditoriaAccionesDTO = auditoriaAccionesMapper.toDto(auditoriaAcciones);

        restAuditoriaAccionesMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(auditoriaAccionesDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllAuditoriaAcciones() throws Exception {
        // Initialize the database
        insertedAuditoriaAcciones = auditoriaAccionesRepository.saveAndFlush(auditoriaAcciones);

        // Get all the auditoriaAccionesList
        restAuditoriaAccionesMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(auditoriaAcciones.getId().intValue())))
            .andExpect(jsonPath("$.[*].entidad").value(hasItem(DEFAULT_ENTIDAD)))
            .andExpect(jsonPath("$.[*].accion").value(hasItem(DEFAULT_ACCION)))
            .andExpect(jsonPath("$.[*].fecha").value(hasItem(sameInstant(DEFAULT_FECHA))))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAuditoriaAccionesWithEagerRelationshipsIsEnabled() throws Exception {
        when(auditoriaAccionesServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAuditoriaAccionesMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(auditoriaAccionesServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAuditoriaAccionesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(auditoriaAccionesServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAuditoriaAccionesMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(auditoriaAccionesRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getAuditoriaAcciones() throws Exception {
        // Initialize the database
        insertedAuditoriaAcciones = auditoriaAccionesRepository.saveAndFlush(auditoriaAcciones);

        // Get the auditoriaAcciones
        restAuditoriaAccionesMockMvc
            .perform(get(ENTITY_API_URL_ID, auditoriaAcciones.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(auditoriaAcciones.getId().intValue()))
            .andExpect(jsonPath("$.entidad").value(DEFAULT_ENTIDAD))
            .andExpect(jsonPath("$.accion").value(DEFAULT_ACCION))
            .andExpect(jsonPath("$.fecha").value(sameInstant(DEFAULT_FECHA)))
            .andExpect(jsonPath("$.descripcion").value(DEFAULT_DESCRIPCION));
    }

    @Test
    @Transactional
    void getAuditoriaAccionesByIdFiltering() throws Exception {
        // Initialize the database
        insertedAuditoriaAcciones = auditoriaAccionesRepository.saveAndFlush(auditoriaAcciones);

        Long id = auditoriaAcciones.getId();

        defaultAuditoriaAccionesFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultAuditoriaAccionesFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultAuditoriaAccionesFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllAuditoriaAccionesByEntidadIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAuditoriaAcciones = auditoriaAccionesRepository.saveAndFlush(auditoriaAcciones);

        // Get all the auditoriaAccionesList where entidad equals to
        defaultAuditoriaAccionesFiltering("entidad.equals=" + DEFAULT_ENTIDAD, "entidad.equals=" + UPDATED_ENTIDAD);
    }

    @Test
    @Transactional
    void getAllAuditoriaAccionesByEntidadIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAuditoriaAcciones = auditoriaAccionesRepository.saveAndFlush(auditoriaAcciones);

        // Get all the auditoriaAccionesList where entidad in
        defaultAuditoriaAccionesFiltering("entidad.in=" + DEFAULT_ENTIDAD + "," + UPDATED_ENTIDAD, "entidad.in=" + UPDATED_ENTIDAD);
    }

    @Test
    @Transactional
    void getAllAuditoriaAccionesByEntidadIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAuditoriaAcciones = auditoriaAccionesRepository.saveAndFlush(auditoriaAcciones);

        // Get all the auditoriaAccionesList where entidad is not null
        defaultAuditoriaAccionesFiltering("entidad.specified=true", "entidad.specified=false");
    }

    @Test
    @Transactional
    void getAllAuditoriaAccionesByEntidadContainsSomething() throws Exception {
        // Initialize the database
        insertedAuditoriaAcciones = auditoriaAccionesRepository.saveAndFlush(auditoriaAcciones);

        // Get all the auditoriaAccionesList where entidad contains
        defaultAuditoriaAccionesFiltering("entidad.contains=" + DEFAULT_ENTIDAD, "entidad.contains=" + UPDATED_ENTIDAD);
    }

    @Test
    @Transactional
    void getAllAuditoriaAccionesByEntidadNotContainsSomething() throws Exception {
        // Initialize the database
        insertedAuditoriaAcciones = auditoriaAccionesRepository.saveAndFlush(auditoriaAcciones);

        // Get all the auditoriaAccionesList where entidad does not contain
        defaultAuditoriaAccionesFiltering("entidad.doesNotContain=" + UPDATED_ENTIDAD, "entidad.doesNotContain=" + DEFAULT_ENTIDAD);
    }

    @Test
    @Transactional
    void getAllAuditoriaAccionesByAccionIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAuditoriaAcciones = auditoriaAccionesRepository.saveAndFlush(auditoriaAcciones);

        // Get all the auditoriaAccionesList where accion equals to
        defaultAuditoriaAccionesFiltering("accion.equals=" + DEFAULT_ACCION, "accion.equals=" + UPDATED_ACCION);
    }

    @Test
    @Transactional
    void getAllAuditoriaAccionesByAccionIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAuditoriaAcciones = auditoriaAccionesRepository.saveAndFlush(auditoriaAcciones);

        // Get all the auditoriaAccionesList where accion in
        defaultAuditoriaAccionesFiltering("accion.in=" + DEFAULT_ACCION + "," + UPDATED_ACCION, "accion.in=" + UPDATED_ACCION);
    }

    @Test
    @Transactional
    void getAllAuditoriaAccionesByAccionIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAuditoriaAcciones = auditoriaAccionesRepository.saveAndFlush(auditoriaAcciones);

        // Get all the auditoriaAccionesList where accion is not null
        defaultAuditoriaAccionesFiltering("accion.specified=true", "accion.specified=false");
    }

    @Test
    @Transactional
    void getAllAuditoriaAccionesByAccionContainsSomething() throws Exception {
        // Initialize the database
        insertedAuditoriaAcciones = auditoriaAccionesRepository.saveAndFlush(auditoriaAcciones);

        // Get all the auditoriaAccionesList where accion contains
        defaultAuditoriaAccionesFiltering("accion.contains=" + DEFAULT_ACCION, "accion.contains=" + UPDATED_ACCION);
    }

    @Test
    @Transactional
    void getAllAuditoriaAccionesByAccionNotContainsSomething() throws Exception {
        // Initialize the database
        insertedAuditoriaAcciones = auditoriaAccionesRepository.saveAndFlush(auditoriaAcciones);

        // Get all the auditoriaAccionesList where accion does not contain
        defaultAuditoriaAccionesFiltering("accion.doesNotContain=" + UPDATED_ACCION, "accion.doesNotContain=" + DEFAULT_ACCION);
    }

    @Test
    @Transactional
    void getAllAuditoriaAccionesByFechaIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAuditoriaAcciones = auditoriaAccionesRepository.saveAndFlush(auditoriaAcciones);

        // Get all the auditoriaAccionesList where fecha equals to
        defaultAuditoriaAccionesFiltering("fecha.equals=" + DEFAULT_FECHA, "fecha.equals=" + UPDATED_FECHA);
    }

    @Test
    @Transactional
    void getAllAuditoriaAccionesByFechaIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAuditoriaAcciones = auditoriaAccionesRepository.saveAndFlush(auditoriaAcciones);

        // Get all the auditoriaAccionesList where fecha in
        defaultAuditoriaAccionesFiltering("fecha.in=" + DEFAULT_FECHA + "," + UPDATED_FECHA, "fecha.in=" + UPDATED_FECHA);
    }

    @Test
    @Transactional
    void getAllAuditoriaAccionesByFechaIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAuditoriaAcciones = auditoriaAccionesRepository.saveAndFlush(auditoriaAcciones);

        // Get all the auditoriaAccionesList where fecha is not null
        defaultAuditoriaAccionesFiltering("fecha.specified=true", "fecha.specified=false");
    }

    @Test
    @Transactional
    void getAllAuditoriaAccionesByFechaIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedAuditoriaAcciones = auditoriaAccionesRepository.saveAndFlush(auditoriaAcciones);

        // Get all the auditoriaAccionesList where fecha is greater than or equal to
        defaultAuditoriaAccionesFiltering("fecha.greaterThanOrEqual=" + DEFAULT_FECHA, "fecha.greaterThanOrEqual=" + UPDATED_FECHA);
    }

    @Test
    @Transactional
    void getAllAuditoriaAccionesByFechaIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedAuditoriaAcciones = auditoriaAccionesRepository.saveAndFlush(auditoriaAcciones);

        // Get all the auditoriaAccionesList where fecha is less than or equal to
        defaultAuditoriaAccionesFiltering("fecha.lessThanOrEqual=" + DEFAULT_FECHA, "fecha.lessThanOrEqual=" + SMALLER_FECHA);
    }

    @Test
    @Transactional
    void getAllAuditoriaAccionesByFechaIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedAuditoriaAcciones = auditoriaAccionesRepository.saveAndFlush(auditoriaAcciones);

        // Get all the auditoriaAccionesList where fecha is less than
        defaultAuditoriaAccionesFiltering("fecha.lessThan=" + UPDATED_FECHA, "fecha.lessThan=" + DEFAULT_FECHA);
    }

    @Test
    @Transactional
    void getAllAuditoriaAccionesByFechaIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedAuditoriaAcciones = auditoriaAccionesRepository.saveAndFlush(auditoriaAcciones);

        // Get all the auditoriaAccionesList where fecha is greater than
        defaultAuditoriaAccionesFiltering("fecha.greaterThan=" + SMALLER_FECHA, "fecha.greaterThan=" + DEFAULT_FECHA);
    }

    @Test
    @Transactional
    void getAllAuditoriaAccionesByDescripcionIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAuditoriaAcciones = auditoriaAccionesRepository.saveAndFlush(auditoriaAcciones);

        // Get all the auditoriaAccionesList where descripcion equals to
        defaultAuditoriaAccionesFiltering("descripcion.equals=" + DEFAULT_DESCRIPCION, "descripcion.equals=" + UPDATED_DESCRIPCION);
    }

    @Test
    @Transactional
    void getAllAuditoriaAccionesByDescripcionIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAuditoriaAcciones = auditoriaAccionesRepository.saveAndFlush(auditoriaAcciones);

        // Get all the auditoriaAccionesList where descripcion in
        defaultAuditoriaAccionesFiltering(
            "descripcion.in=" + DEFAULT_DESCRIPCION + "," + UPDATED_DESCRIPCION,
            "descripcion.in=" + UPDATED_DESCRIPCION
        );
    }

    @Test
    @Transactional
    void getAllAuditoriaAccionesByDescripcionIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAuditoriaAcciones = auditoriaAccionesRepository.saveAndFlush(auditoriaAcciones);

        // Get all the auditoriaAccionesList where descripcion is not null
        defaultAuditoriaAccionesFiltering("descripcion.specified=true", "descripcion.specified=false");
    }

    @Test
    @Transactional
    void getAllAuditoriaAccionesByDescripcionContainsSomething() throws Exception {
        // Initialize the database
        insertedAuditoriaAcciones = auditoriaAccionesRepository.saveAndFlush(auditoriaAcciones);

        // Get all the auditoriaAccionesList where descripcion contains
        defaultAuditoriaAccionesFiltering("descripcion.contains=" + DEFAULT_DESCRIPCION, "descripcion.contains=" + UPDATED_DESCRIPCION);
    }

    @Test
    @Transactional
    void getAllAuditoriaAccionesByDescripcionNotContainsSomething() throws Exception {
        // Initialize the database
        insertedAuditoriaAcciones = auditoriaAccionesRepository.saveAndFlush(auditoriaAcciones);

        // Get all the auditoriaAccionesList where descripcion does not contain
        defaultAuditoriaAccionesFiltering(
            "descripcion.doesNotContain=" + UPDATED_DESCRIPCION,
            "descripcion.doesNotContain=" + DEFAULT_DESCRIPCION
        );
    }

    @Test
    @Transactional
    void getAllAuditoriaAccionesByUserIsEqualToSomething() throws Exception {
        User user;
        if (TestUtil.findAll(em, User.class).isEmpty()) {
            auditoriaAccionesRepository.saveAndFlush(auditoriaAcciones);
            user = UserResourceIT.createEntity();
        } else {
            user = TestUtil.findAll(em, User.class).get(0);
        }
        em.persist(user);
        em.flush();
        auditoriaAcciones.setUser(user);
        auditoriaAccionesRepository.saveAndFlush(auditoriaAcciones);
        String userId = user.getId();
        // Get all the auditoriaAccionesList where user equals to userId
        defaultAuditoriaAccionesShouldBeFound("userId.equals=" + userId);

        // Get all the auditoriaAccionesList where user equals to "invalid-id"
        defaultAuditoriaAccionesShouldNotBeFound("userId.equals=" + "invalid-id");
    }

    private void defaultAuditoriaAccionesFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultAuditoriaAccionesShouldBeFound(shouldBeFound);
        defaultAuditoriaAccionesShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultAuditoriaAccionesShouldBeFound(String filter) throws Exception {
        restAuditoriaAccionesMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(auditoriaAcciones.getId().intValue())))
            .andExpect(jsonPath("$.[*].entidad").value(hasItem(DEFAULT_ENTIDAD)))
            .andExpect(jsonPath("$.[*].accion").value(hasItem(DEFAULT_ACCION)))
            .andExpect(jsonPath("$.[*].fecha").value(hasItem(sameInstant(DEFAULT_FECHA))))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)));

        // Check, that the count call also returns 1
        restAuditoriaAccionesMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultAuditoriaAccionesShouldNotBeFound(String filter) throws Exception {
        restAuditoriaAccionesMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restAuditoriaAccionesMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingAuditoriaAcciones() throws Exception {
        // Get the auditoriaAcciones
        restAuditoriaAccionesMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingAuditoriaAcciones() throws Exception {
        // Initialize the database
        insertedAuditoriaAcciones = auditoriaAccionesRepository.saveAndFlush(auditoriaAcciones);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the auditoriaAcciones
        AuditoriaAcciones updatedAuditoriaAcciones = auditoriaAccionesRepository.findById(auditoriaAcciones.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedAuditoriaAcciones are not directly saved in db
        em.detach(updatedAuditoriaAcciones);
        updatedAuditoriaAcciones.entidad(UPDATED_ENTIDAD).accion(UPDATED_ACCION).fecha(UPDATED_FECHA).descripcion(UPDATED_DESCRIPCION);
        AuditoriaAccionesDTO auditoriaAccionesDTO = auditoriaAccionesMapper.toDto(updatedAuditoriaAcciones);

        restAuditoriaAccionesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, auditoriaAccionesDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(auditoriaAccionesDTO))
            )
            .andExpect(status().isOk());

        // Validate the AuditoriaAcciones in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedAuditoriaAccionesToMatchAllProperties(updatedAuditoriaAcciones);
    }

    @Test
    @Transactional
    void putNonExistingAuditoriaAcciones() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        auditoriaAcciones.setId(longCount.incrementAndGet());

        // Create the AuditoriaAcciones
        AuditoriaAccionesDTO auditoriaAccionesDTO = auditoriaAccionesMapper.toDto(auditoriaAcciones);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAuditoriaAccionesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, auditoriaAccionesDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(auditoriaAccionesDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AuditoriaAcciones in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchAuditoriaAcciones() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        auditoriaAcciones.setId(longCount.incrementAndGet());

        // Create the AuditoriaAcciones
        AuditoriaAccionesDTO auditoriaAccionesDTO = auditoriaAccionesMapper.toDto(auditoriaAcciones);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAuditoriaAccionesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(auditoriaAccionesDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AuditoriaAcciones in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAuditoriaAcciones() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        auditoriaAcciones.setId(longCount.incrementAndGet());

        // Create the AuditoriaAcciones
        AuditoriaAccionesDTO auditoriaAccionesDTO = auditoriaAccionesMapper.toDto(auditoriaAcciones);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAuditoriaAccionesMockMvc
            .perform(
                put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(auditoriaAccionesDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the AuditoriaAcciones in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateAuditoriaAccionesWithPatch() throws Exception {
        // Initialize the database
        insertedAuditoriaAcciones = auditoriaAccionesRepository.saveAndFlush(auditoriaAcciones);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the auditoriaAcciones using partial update
        AuditoriaAcciones partialUpdatedAuditoriaAcciones = new AuditoriaAcciones();
        partialUpdatedAuditoriaAcciones.setId(auditoriaAcciones.getId());

        partialUpdatedAuditoriaAcciones.accion(UPDATED_ACCION).fecha(UPDATED_FECHA).descripcion(UPDATED_DESCRIPCION);

        restAuditoriaAccionesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAuditoriaAcciones.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAuditoriaAcciones))
            )
            .andExpect(status().isOk());

        // Validate the AuditoriaAcciones in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAuditoriaAccionesUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedAuditoriaAcciones, auditoriaAcciones),
            getPersistedAuditoriaAcciones(auditoriaAcciones)
        );
    }

    @Test
    @Transactional
    void fullUpdateAuditoriaAccionesWithPatch() throws Exception {
        // Initialize the database
        insertedAuditoriaAcciones = auditoriaAccionesRepository.saveAndFlush(auditoriaAcciones);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the auditoriaAcciones using partial update
        AuditoriaAcciones partialUpdatedAuditoriaAcciones = new AuditoriaAcciones();
        partialUpdatedAuditoriaAcciones.setId(auditoriaAcciones.getId());

        partialUpdatedAuditoriaAcciones
            .entidad(UPDATED_ENTIDAD)
            .accion(UPDATED_ACCION)
            .fecha(UPDATED_FECHA)
            .descripcion(UPDATED_DESCRIPCION);

        restAuditoriaAccionesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAuditoriaAcciones.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAuditoriaAcciones))
            )
            .andExpect(status().isOk());

        // Validate the AuditoriaAcciones in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAuditoriaAccionesUpdatableFieldsEquals(
            partialUpdatedAuditoriaAcciones,
            getPersistedAuditoriaAcciones(partialUpdatedAuditoriaAcciones)
        );
    }

    @Test
    @Transactional
    void patchNonExistingAuditoriaAcciones() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        auditoriaAcciones.setId(longCount.incrementAndGet());

        // Create the AuditoriaAcciones
        AuditoriaAccionesDTO auditoriaAccionesDTO = auditoriaAccionesMapper.toDto(auditoriaAcciones);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAuditoriaAccionesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, auditoriaAccionesDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(auditoriaAccionesDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AuditoriaAcciones in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAuditoriaAcciones() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        auditoriaAcciones.setId(longCount.incrementAndGet());

        // Create the AuditoriaAcciones
        AuditoriaAccionesDTO auditoriaAccionesDTO = auditoriaAccionesMapper.toDto(auditoriaAcciones);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAuditoriaAccionesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(auditoriaAccionesDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AuditoriaAcciones in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAuditoriaAcciones() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        auditoriaAcciones.setId(longCount.incrementAndGet());

        // Create the AuditoriaAcciones
        AuditoriaAccionesDTO auditoriaAccionesDTO = auditoriaAccionesMapper.toDto(auditoriaAcciones);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAuditoriaAccionesMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(auditoriaAccionesDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the AuditoriaAcciones in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteAuditoriaAcciones() throws Exception {
        // Initialize the database
        insertedAuditoriaAcciones = auditoriaAccionesRepository.saveAndFlush(auditoriaAcciones);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the auditoriaAcciones
        restAuditoriaAccionesMockMvc
            .perform(delete(ENTITY_API_URL_ID, auditoriaAcciones.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return auditoriaAccionesRepository.count();
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

    protected AuditoriaAcciones getPersistedAuditoriaAcciones(AuditoriaAcciones auditoriaAcciones) {
        return auditoriaAccionesRepository.findById(auditoriaAcciones.getId()).orElseThrow();
    }

    protected void assertPersistedAuditoriaAccionesToMatchAllProperties(AuditoriaAcciones expectedAuditoriaAcciones) {
        assertAuditoriaAccionesAllPropertiesEquals(expectedAuditoriaAcciones, getPersistedAuditoriaAcciones(expectedAuditoriaAcciones));
    }

    protected void assertPersistedAuditoriaAccionesToMatchUpdatableProperties(AuditoriaAcciones expectedAuditoriaAcciones) {
        assertAuditoriaAccionesAllUpdatablePropertiesEquals(
            expectedAuditoriaAcciones,
            getPersistedAuditoriaAcciones(expectedAuditoriaAcciones)
        );
    }
}
