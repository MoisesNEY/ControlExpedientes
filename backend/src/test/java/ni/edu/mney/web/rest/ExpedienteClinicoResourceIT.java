package ni.edu.mney.web.rest;

import static ni.edu.mney.domain.ExpedienteClinicoAsserts.*;
import static ni.edu.mney.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import ni.edu.mney.IntegrationTest;
import ni.edu.mney.domain.ExpedienteClinico;
import ni.edu.mney.repository.ExpedienteClinicoRepository;
import ni.edu.mney.service.dto.ExpedienteClinicoDTO;
import ni.edu.mney.service.mapper.ExpedienteClinicoMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ExpedienteClinicoResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ExpedienteClinicoResourceIT {

    private static final String DEFAULT_NUMERO_EXPEDIENTE = "AAAAAAAAAA";
    private static final String UPDATED_NUMERO_EXPEDIENTE = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_FECHA_APERTURA = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_FECHA_APERTURA = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_FECHA_APERTURA = LocalDate.ofEpochDay(-1L);

    private static final String DEFAULT_OBSERVACIONES = "AAAAAAAAAA";
    private static final String UPDATED_OBSERVACIONES = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/expediente-clinicos";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ExpedienteClinicoRepository expedienteClinicoRepository;

    @Autowired
    private ExpedienteClinicoMapper expedienteClinicoMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restExpedienteClinicoMockMvc;

    private ExpedienteClinico expedienteClinico;

    private ExpedienteClinico insertedExpedienteClinico;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ExpedienteClinico createEntity() {
        return new ExpedienteClinico()
            .numeroExpediente(DEFAULT_NUMERO_EXPEDIENTE)
            .fechaApertura(DEFAULT_FECHA_APERTURA)
            .observaciones(DEFAULT_OBSERVACIONES);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ExpedienteClinico createUpdatedEntity() {
        return new ExpedienteClinico()
            .numeroExpediente(UPDATED_NUMERO_EXPEDIENTE)
            .fechaApertura(UPDATED_FECHA_APERTURA)
            .observaciones(UPDATED_OBSERVACIONES);
    }

    @BeforeEach
    void initTest() {
        expedienteClinico = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedExpedienteClinico != null) {
            expedienteClinicoRepository.delete(insertedExpedienteClinico);
            insertedExpedienteClinico = null;
        }
    }

    @Test
    @Transactional
    void createExpedienteClinico() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the ExpedienteClinico
        ExpedienteClinicoDTO expedienteClinicoDTO = expedienteClinicoMapper.toDto(expedienteClinico);
        var returnedExpedienteClinicoDTO = om.readValue(
            restExpedienteClinicoMockMvc
                .perform(
                    post(ENTITY_API_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(expedienteClinicoDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ExpedienteClinicoDTO.class
        );

        // Validate the ExpedienteClinico in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedExpedienteClinico = expedienteClinicoMapper.toEntity(returnedExpedienteClinicoDTO);
        assertExpedienteClinicoUpdatableFieldsEquals(returnedExpedienteClinico, getPersistedExpedienteClinico(returnedExpedienteClinico));

        insertedExpedienteClinico = returnedExpedienteClinico;
    }

    @Test
    @Transactional
    void createExpedienteClinicoWithExistingId() throws Exception {
        // Create the ExpedienteClinico with an existing ID
        expedienteClinico.setId(1L);
        ExpedienteClinicoDTO expedienteClinicoDTO = expedienteClinicoMapper.toDto(expedienteClinico);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restExpedienteClinicoMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(expedienteClinicoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ExpedienteClinico in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNumeroExpedienteIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        expedienteClinico.setNumeroExpediente(null);

        // Create the ExpedienteClinico, which fails.
        ExpedienteClinicoDTO expedienteClinicoDTO = expedienteClinicoMapper.toDto(expedienteClinico);

        restExpedienteClinicoMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(expedienteClinicoDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkFechaAperturaIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        expedienteClinico.setFechaApertura(null);

        // Create the ExpedienteClinico, which fails.
        ExpedienteClinicoDTO expedienteClinicoDTO = expedienteClinicoMapper.toDto(expedienteClinico);

        restExpedienteClinicoMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(expedienteClinicoDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllExpedienteClinicos() throws Exception {
        // Initialize the database
        insertedExpedienteClinico = expedienteClinicoRepository.saveAndFlush(expedienteClinico);

        // Get all the expedienteClinicoList
        restExpedienteClinicoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(expedienteClinico.getId().intValue())))
            .andExpect(jsonPath("$.[*].numeroExpediente").value(hasItem(DEFAULT_NUMERO_EXPEDIENTE)))
            .andExpect(jsonPath("$.[*].fechaApertura").value(hasItem(DEFAULT_FECHA_APERTURA.toString())))
            .andExpect(jsonPath("$.[*].observaciones").value(hasItem(DEFAULT_OBSERVACIONES)));
    }

    @Test
    @Transactional
    void getExpedienteClinico() throws Exception {
        // Initialize the database
        insertedExpedienteClinico = expedienteClinicoRepository.saveAndFlush(expedienteClinico);

        // Get the expedienteClinico
        restExpedienteClinicoMockMvc
            .perform(get(ENTITY_API_URL_ID, expedienteClinico.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(expedienteClinico.getId().intValue()))
            .andExpect(jsonPath("$.numeroExpediente").value(DEFAULT_NUMERO_EXPEDIENTE))
            .andExpect(jsonPath("$.fechaApertura").value(DEFAULT_FECHA_APERTURA.toString()))
            .andExpect(jsonPath("$.observaciones").value(DEFAULT_OBSERVACIONES));
    }

    @Test
    @Transactional
    void getExpedienteClinicosByIdFiltering() throws Exception {
        // Initialize the database
        insertedExpedienteClinico = expedienteClinicoRepository.saveAndFlush(expedienteClinico);

        Long id = expedienteClinico.getId();

        defaultExpedienteClinicoFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultExpedienteClinicoFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultExpedienteClinicoFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllExpedienteClinicosByNumeroExpedienteIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedExpedienteClinico = expedienteClinicoRepository.saveAndFlush(expedienteClinico);

        // Get all the expedienteClinicoList where numeroExpediente equals to
        defaultExpedienteClinicoFiltering(
            "numeroExpediente.equals=" + DEFAULT_NUMERO_EXPEDIENTE,
            "numeroExpediente.equals=" + UPDATED_NUMERO_EXPEDIENTE
        );
    }

    @Test
    @Transactional
    void getAllExpedienteClinicosByNumeroExpedienteIsInShouldWork() throws Exception {
        // Initialize the database
        insertedExpedienteClinico = expedienteClinicoRepository.saveAndFlush(expedienteClinico);

        // Get all the expedienteClinicoList where numeroExpediente in
        defaultExpedienteClinicoFiltering(
            "numeroExpediente.in=" + DEFAULT_NUMERO_EXPEDIENTE + "," + UPDATED_NUMERO_EXPEDIENTE,
            "numeroExpediente.in=" + UPDATED_NUMERO_EXPEDIENTE
        );
    }

    @Test
    @Transactional
    void getAllExpedienteClinicosByNumeroExpedienteIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedExpedienteClinico = expedienteClinicoRepository.saveAndFlush(expedienteClinico);

        // Get all the expedienteClinicoList where numeroExpediente is not null
        defaultExpedienteClinicoFiltering("numeroExpediente.specified=true", "numeroExpediente.specified=false");
    }

    @Test
    @Transactional
    void getAllExpedienteClinicosByNumeroExpedienteContainsSomething() throws Exception {
        // Initialize the database
        insertedExpedienteClinico = expedienteClinicoRepository.saveAndFlush(expedienteClinico);

        // Get all the expedienteClinicoList where numeroExpediente contains
        defaultExpedienteClinicoFiltering(
            "numeroExpediente.contains=" + DEFAULT_NUMERO_EXPEDIENTE,
            "numeroExpediente.contains=" + UPDATED_NUMERO_EXPEDIENTE
        );
    }

    @Test
    @Transactional
    void getAllExpedienteClinicosByNumeroExpedienteNotContainsSomething() throws Exception {
        // Initialize the database
        insertedExpedienteClinico = expedienteClinicoRepository.saveAndFlush(expedienteClinico);

        // Get all the expedienteClinicoList where numeroExpediente does not contain
        defaultExpedienteClinicoFiltering(
            "numeroExpediente.doesNotContain=" + UPDATED_NUMERO_EXPEDIENTE,
            "numeroExpediente.doesNotContain=" + DEFAULT_NUMERO_EXPEDIENTE
        );
    }

    @Test
    @Transactional
    void getAllExpedienteClinicosByFechaAperturaIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedExpedienteClinico = expedienteClinicoRepository.saveAndFlush(expedienteClinico);

        // Get all the expedienteClinicoList where fechaApertura equals to
        defaultExpedienteClinicoFiltering(
            "fechaApertura.equals=" + DEFAULT_FECHA_APERTURA,
            "fechaApertura.equals=" + UPDATED_FECHA_APERTURA
        );
    }

    @Test
    @Transactional
    void getAllExpedienteClinicosByFechaAperturaIsInShouldWork() throws Exception {
        // Initialize the database
        insertedExpedienteClinico = expedienteClinicoRepository.saveAndFlush(expedienteClinico);

        // Get all the expedienteClinicoList where fechaApertura in
        defaultExpedienteClinicoFiltering(
            "fechaApertura.in=" + DEFAULT_FECHA_APERTURA + "," + UPDATED_FECHA_APERTURA,
            "fechaApertura.in=" + UPDATED_FECHA_APERTURA
        );
    }

    @Test
    @Transactional
    void getAllExpedienteClinicosByFechaAperturaIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedExpedienteClinico = expedienteClinicoRepository.saveAndFlush(expedienteClinico);

        // Get all the expedienteClinicoList where fechaApertura is not null
        defaultExpedienteClinicoFiltering("fechaApertura.specified=true", "fechaApertura.specified=false");
    }

    @Test
    @Transactional
    void getAllExpedienteClinicosByFechaAperturaIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedExpedienteClinico = expedienteClinicoRepository.saveAndFlush(expedienteClinico);

        // Get all the expedienteClinicoList where fechaApertura is greater than or equal to
        defaultExpedienteClinicoFiltering(
            "fechaApertura.greaterThanOrEqual=" + DEFAULT_FECHA_APERTURA,
            "fechaApertura.greaterThanOrEqual=" + UPDATED_FECHA_APERTURA
        );
    }

    @Test
    @Transactional
    void getAllExpedienteClinicosByFechaAperturaIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedExpedienteClinico = expedienteClinicoRepository.saveAndFlush(expedienteClinico);

        // Get all the expedienteClinicoList where fechaApertura is less than or equal to
        defaultExpedienteClinicoFiltering(
            "fechaApertura.lessThanOrEqual=" + DEFAULT_FECHA_APERTURA,
            "fechaApertura.lessThanOrEqual=" + SMALLER_FECHA_APERTURA
        );
    }

    @Test
    @Transactional
    void getAllExpedienteClinicosByFechaAperturaIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedExpedienteClinico = expedienteClinicoRepository.saveAndFlush(expedienteClinico);

        // Get all the expedienteClinicoList where fechaApertura is less than
        defaultExpedienteClinicoFiltering(
            "fechaApertura.lessThan=" + UPDATED_FECHA_APERTURA,
            "fechaApertura.lessThan=" + DEFAULT_FECHA_APERTURA
        );
    }

    @Test
    @Transactional
    void getAllExpedienteClinicosByFechaAperturaIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedExpedienteClinico = expedienteClinicoRepository.saveAndFlush(expedienteClinico);

        // Get all the expedienteClinicoList where fechaApertura is greater than
        defaultExpedienteClinicoFiltering(
            "fechaApertura.greaterThan=" + SMALLER_FECHA_APERTURA,
            "fechaApertura.greaterThan=" + DEFAULT_FECHA_APERTURA
        );
    }

    @Test
    @Transactional
    void getAllExpedienteClinicosByObservacionesIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedExpedienteClinico = expedienteClinicoRepository.saveAndFlush(expedienteClinico);

        // Get all the expedienteClinicoList where observaciones equals to
        defaultExpedienteClinicoFiltering("observaciones.equals=" + DEFAULT_OBSERVACIONES, "observaciones.equals=" + UPDATED_OBSERVACIONES);
    }

    @Test
    @Transactional
    void getAllExpedienteClinicosByObservacionesIsInShouldWork() throws Exception {
        // Initialize the database
        insertedExpedienteClinico = expedienteClinicoRepository.saveAndFlush(expedienteClinico);

        // Get all the expedienteClinicoList where observaciones in
        defaultExpedienteClinicoFiltering(
            "observaciones.in=" + DEFAULT_OBSERVACIONES + "," + UPDATED_OBSERVACIONES,
            "observaciones.in=" + UPDATED_OBSERVACIONES
        );
    }

    @Test
    @Transactional
    void getAllExpedienteClinicosByObservacionesIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedExpedienteClinico = expedienteClinicoRepository.saveAndFlush(expedienteClinico);

        // Get all the expedienteClinicoList where observaciones is not null
        defaultExpedienteClinicoFiltering("observaciones.specified=true", "observaciones.specified=false");
    }

    @Test
    @Transactional
    void getAllExpedienteClinicosByObservacionesContainsSomething() throws Exception {
        // Initialize the database
        insertedExpedienteClinico = expedienteClinicoRepository.saveAndFlush(expedienteClinico);

        // Get all the expedienteClinicoList where observaciones contains
        defaultExpedienteClinicoFiltering(
            "observaciones.contains=" + DEFAULT_OBSERVACIONES,
            "observaciones.contains=" + UPDATED_OBSERVACIONES
        );
    }

    @Test
    @Transactional
    void getAllExpedienteClinicosByObservacionesNotContainsSomething() throws Exception {
        // Initialize the database
        insertedExpedienteClinico = expedienteClinicoRepository.saveAndFlush(expedienteClinico);

        // Get all the expedienteClinicoList where observaciones does not contain
        defaultExpedienteClinicoFiltering(
            "observaciones.doesNotContain=" + UPDATED_OBSERVACIONES,
            "observaciones.doesNotContain=" + DEFAULT_OBSERVACIONES
        );
    }

    private void defaultExpedienteClinicoFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultExpedienteClinicoShouldBeFound(shouldBeFound);
        defaultExpedienteClinicoShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultExpedienteClinicoShouldBeFound(String filter) throws Exception {
        restExpedienteClinicoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(expedienteClinico.getId().intValue())))
            .andExpect(jsonPath("$.[*].numeroExpediente").value(hasItem(DEFAULT_NUMERO_EXPEDIENTE)))
            .andExpect(jsonPath("$.[*].fechaApertura").value(hasItem(DEFAULT_FECHA_APERTURA.toString())))
            .andExpect(jsonPath("$.[*].observaciones").value(hasItem(DEFAULT_OBSERVACIONES)));

        // Check, that the count call also returns 1
        restExpedienteClinicoMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultExpedienteClinicoShouldNotBeFound(String filter) throws Exception {
        restExpedienteClinicoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restExpedienteClinicoMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingExpedienteClinico() throws Exception {
        // Get the expedienteClinico
        restExpedienteClinicoMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingExpedienteClinico() throws Exception {
        // Initialize the database
        insertedExpedienteClinico = expedienteClinicoRepository.saveAndFlush(expedienteClinico);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the expedienteClinico
        ExpedienteClinico updatedExpedienteClinico = expedienteClinicoRepository.findById(expedienteClinico.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedExpedienteClinico are not directly saved in db
        em.detach(updatedExpedienteClinico);
        updatedExpedienteClinico
            .numeroExpediente(UPDATED_NUMERO_EXPEDIENTE)
            .fechaApertura(UPDATED_FECHA_APERTURA)
            .observaciones(UPDATED_OBSERVACIONES);
        ExpedienteClinicoDTO expedienteClinicoDTO = expedienteClinicoMapper.toDto(updatedExpedienteClinico);

        restExpedienteClinicoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, expedienteClinicoDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(expedienteClinicoDTO))
            )
            .andExpect(status().isOk());

        // Validate the ExpedienteClinico in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedExpedienteClinicoToMatchAllProperties(updatedExpedienteClinico);
    }

    @Test
    @Transactional
    void putNonExistingExpedienteClinico() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        expedienteClinico.setId(longCount.incrementAndGet());

        // Create the ExpedienteClinico
        ExpedienteClinicoDTO expedienteClinicoDTO = expedienteClinicoMapper.toDto(expedienteClinico);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restExpedienteClinicoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, expedienteClinicoDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(expedienteClinicoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ExpedienteClinico in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchExpedienteClinico() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        expedienteClinico.setId(longCount.incrementAndGet());

        // Create the ExpedienteClinico
        ExpedienteClinicoDTO expedienteClinicoDTO = expedienteClinicoMapper.toDto(expedienteClinico);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restExpedienteClinicoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(expedienteClinicoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ExpedienteClinico in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamExpedienteClinico() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        expedienteClinico.setId(longCount.incrementAndGet());

        // Create the ExpedienteClinico
        ExpedienteClinicoDTO expedienteClinicoDTO = expedienteClinicoMapper.toDto(expedienteClinico);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restExpedienteClinicoMockMvc
            .perform(
                put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(expedienteClinicoDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the ExpedienteClinico in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateExpedienteClinicoWithPatch() throws Exception {
        // Initialize the database
        insertedExpedienteClinico = expedienteClinicoRepository.saveAndFlush(expedienteClinico);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the expedienteClinico using partial update
        ExpedienteClinico partialUpdatedExpedienteClinico = new ExpedienteClinico();
        partialUpdatedExpedienteClinico.setId(expedienteClinico.getId());

        partialUpdatedExpedienteClinico.numeroExpediente(UPDATED_NUMERO_EXPEDIENTE).observaciones(UPDATED_OBSERVACIONES);

        restExpedienteClinicoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedExpedienteClinico.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedExpedienteClinico))
            )
            .andExpect(status().isOk());

        // Validate the ExpedienteClinico in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertExpedienteClinicoUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedExpedienteClinico, expedienteClinico),
            getPersistedExpedienteClinico(expedienteClinico)
        );
    }

    @Test
    @Transactional
    void fullUpdateExpedienteClinicoWithPatch() throws Exception {
        // Initialize the database
        insertedExpedienteClinico = expedienteClinicoRepository.saveAndFlush(expedienteClinico);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the expedienteClinico using partial update
        ExpedienteClinico partialUpdatedExpedienteClinico = new ExpedienteClinico();
        partialUpdatedExpedienteClinico.setId(expedienteClinico.getId());

        partialUpdatedExpedienteClinico
            .numeroExpediente(UPDATED_NUMERO_EXPEDIENTE)
            .fechaApertura(UPDATED_FECHA_APERTURA)
            .observaciones(UPDATED_OBSERVACIONES);

        restExpedienteClinicoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedExpedienteClinico.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedExpedienteClinico))
            )
            .andExpect(status().isOk());

        // Validate the ExpedienteClinico in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertExpedienteClinicoUpdatableFieldsEquals(
            partialUpdatedExpedienteClinico,
            getPersistedExpedienteClinico(partialUpdatedExpedienteClinico)
        );
    }

    @Test
    @Transactional
    void patchNonExistingExpedienteClinico() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        expedienteClinico.setId(longCount.incrementAndGet());

        // Create the ExpedienteClinico
        ExpedienteClinicoDTO expedienteClinicoDTO = expedienteClinicoMapper.toDto(expedienteClinico);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restExpedienteClinicoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, expedienteClinicoDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(expedienteClinicoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ExpedienteClinico in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchExpedienteClinico() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        expedienteClinico.setId(longCount.incrementAndGet());

        // Create the ExpedienteClinico
        ExpedienteClinicoDTO expedienteClinicoDTO = expedienteClinicoMapper.toDto(expedienteClinico);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restExpedienteClinicoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(expedienteClinicoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ExpedienteClinico in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamExpedienteClinico() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        expedienteClinico.setId(longCount.incrementAndGet());

        // Create the ExpedienteClinico
        ExpedienteClinicoDTO expedienteClinicoDTO = expedienteClinicoMapper.toDto(expedienteClinico);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restExpedienteClinicoMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(expedienteClinicoDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the ExpedienteClinico in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteExpedienteClinico() throws Exception {
        // Initialize the database
        insertedExpedienteClinico = expedienteClinicoRepository.saveAndFlush(expedienteClinico);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the expedienteClinico
        restExpedienteClinicoMockMvc
            .perform(delete(ENTITY_API_URL_ID, expedienteClinico.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return expedienteClinicoRepository.count();
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

    protected ExpedienteClinico getPersistedExpedienteClinico(ExpedienteClinico expedienteClinico) {
        return expedienteClinicoRepository.findById(expedienteClinico.getId()).orElseThrow();
    }

    protected void assertPersistedExpedienteClinicoToMatchAllProperties(ExpedienteClinico expectedExpedienteClinico) {
        assertExpedienteClinicoAllPropertiesEquals(expectedExpedienteClinico, getPersistedExpedienteClinico(expectedExpedienteClinico));
    }

    protected void assertPersistedExpedienteClinicoToMatchUpdatableProperties(ExpedienteClinico expectedExpedienteClinico) {
        assertExpedienteClinicoAllUpdatablePropertiesEquals(
            expectedExpedienteClinico,
            getPersistedExpedienteClinico(expectedExpedienteClinico)
        );
    }
}
