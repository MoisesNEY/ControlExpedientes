package ni.edu.mney.web.rest;

import static ni.edu.mney.domain.HistorialClinicoAsserts.*;
import static ni.edu.mney.web.rest.TestUtil.createUpdateProxyForBean;
import static ni.edu.mney.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import ni.edu.mney.IntegrationTest;
import ni.edu.mney.domain.ExpedienteClinico;
import ni.edu.mney.domain.HistorialClinico;
import ni.edu.mney.repository.HistorialClinicoRepository;
import ni.edu.mney.service.dto.HistorialClinicoDTO;
import ni.edu.mney.service.mapper.HistorialClinicoMapper;
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
 * Integration tests for the {@link HistorialClinicoResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class HistorialClinicoResourceIT {

    private static final ZonedDateTime DEFAULT_FECHA_REGISTRO = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_FECHA_REGISTRO = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final ZonedDateTime SMALLER_FECHA_REGISTRO = ZonedDateTime.ofInstant(Instant.ofEpochMilli(-1L), ZoneOffset.UTC);

    private static final String DEFAULT_DESCRIPCION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPCION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/historial-clinicos";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private HistorialClinicoRepository historialClinicoRepository;

    @Autowired
    private HistorialClinicoMapper historialClinicoMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restHistorialClinicoMockMvc;

    private HistorialClinico historialClinico;

    private HistorialClinico insertedHistorialClinico;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static HistorialClinico createEntity() {
        return new HistorialClinico().fechaRegistro(DEFAULT_FECHA_REGISTRO).descripcion(DEFAULT_DESCRIPCION);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static HistorialClinico createUpdatedEntity() {
        return new HistorialClinico().fechaRegistro(UPDATED_FECHA_REGISTRO).descripcion(UPDATED_DESCRIPCION);
    }

    @BeforeEach
    void initTest() {
        historialClinico = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedHistorialClinico != null) {
            historialClinicoRepository.delete(insertedHistorialClinico);
            insertedHistorialClinico = null;
        }
    }

    @Test
    @Transactional
    void createHistorialClinico() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the HistorialClinico
        HistorialClinicoDTO historialClinicoDTO = historialClinicoMapper.toDto(historialClinico);
        var returnedHistorialClinicoDTO = om.readValue(
            restHistorialClinicoMockMvc
                .perform(
                    post(ENTITY_API_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(historialClinicoDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            HistorialClinicoDTO.class
        );

        // Validate the HistorialClinico in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedHistorialClinico = historialClinicoMapper.toEntity(returnedHistorialClinicoDTO);
        assertHistorialClinicoUpdatableFieldsEquals(returnedHistorialClinico, getPersistedHistorialClinico(returnedHistorialClinico));

        insertedHistorialClinico = returnedHistorialClinico;
    }

    @Test
    @Transactional
    void createHistorialClinicoWithExistingId() throws Exception {
        // Create the HistorialClinico with an existing ID
        historialClinico.setId(1L);
        HistorialClinicoDTO historialClinicoDTO = historialClinicoMapper.toDto(historialClinico);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restHistorialClinicoMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(historialClinicoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the HistorialClinico in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkFechaRegistroIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        historialClinico.setFechaRegistro(null);

        // Create the HistorialClinico, which fails.
        HistorialClinicoDTO historialClinicoDTO = historialClinicoMapper.toDto(historialClinico);

        restHistorialClinicoMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(historialClinicoDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkDescripcionIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        historialClinico.setDescripcion(null);

        // Create the HistorialClinico, which fails.
        HistorialClinicoDTO historialClinicoDTO = historialClinicoMapper.toDto(historialClinico);

        restHistorialClinicoMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(historialClinicoDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllHistorialClinicos() throws Exception {
        // Initialize the database
        insertedHistorialClinico = historialClinicoRepository.saveAndFlush(historialClinico);

        // Get all the historialClinicoList
        restHistorialClinicoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(historialClinico.getId().intValue())))
            .andExpect(jsonPath("$.[*].fechaRegistro").value(hasItem(sameInstant(DEFAULT_FECHA_REGISTRO))))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)));
    }

    @Test
    @Transactional
    void getHistorialClinico() throws Exception {
        // Initialize the database
        insertedHistorialClinico = historialClinicoRepository.saveAndFlush(historialClinico);

        // Get the historialClinico
        restHistorialClinicoMockMvc
            .perform(get(ENTITY_API_URL_ID, historialClinico.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(historialClinico.getId().intValue()))
            .andExpect(jsonPath("$.fechaRegistro").value(sameInstant(DEFAULT_FECHA_REGISTRO)))
            .andExpect(jsonPath("$.descripcion").value(DEFAULT_DESCRIPCION));
    }

    @Test
    @Transactional
    void getHistorialClinicosByIdFiltering() throws Exception {
        // Initialize the database
        insertedHistorialClinico = historialClinicoRepository.saveAndFlush(historialClinico);

        Long id = historialClinico.getId();

        defaultHistorialClinicoFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultHistorialClinicoFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultHistorialClinicoFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllHistorialClinicosByFechaRegistroIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedHistorialClinico = historialClinicoRepository.saveAndFlush(historialClinico);

        // Get all the historialClinicoList where fechaRegistro equals to
        defaultHistorialClinicoFiltering(
            "fechaRegistro.equals=" + DEFAULT_FECHA_REGISTRO,
            "fechaRegistro.equals=" + UPDATED_FECHA_REGISTRO
        );
    }

    @Test
    @Transactional
    void getAllHistorialClinicosByFechaRegistroIsInShouldWork() throws Exception {
        // Initialize the database
        insertedHistorialClinico = historialClinicoRepository.saveAndFlush(historialClinico);

        // Get all the historialClinicoList where fechaRegistro in
        defaultHistorialClinicoFiltering(
            "fechaRegistro.in=" + DEFAULT_FECHA_REGISTRO + "," + UPDATED_FECHA_REGISTRO,
            "fechaRegistro.in=" + UPDATED_FECHA_REGISTRO
        );
    }

    @Test
    @Transactional
    void getAllHistorialClinicosByFechaRegistroIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedHistorialClinico = historialClinicoRepository.saveAndFlush(historialClinico);

        // Get all the historialClinicoList where fechaRegistro is not null
        defaultHistorialClinicoFiltering("fechaRegistro.specified=true", "fechaRegistro.specified=false");
    }

    @Test
    @Transactional
    void getAllHistorialClinicosByFechaRegistroIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedHistorialClinico = historialClinicoRepository.saveAndFlush(historialClinico);

        // Get all the historialClinicoList where fechaRegistro is greater than or equal to
        defaultHistorialClinicoFiltering(
            "fechaRegistro.greaterThanOrEqual=" + DEFAULT_FECHA_REGISTRO,
            "fechaRegistro.greaterThanOrEqual=" + UPDATED_FECHA_REGISTRO
        );
    }

    @Test
    @Transactional
    void getAllHistorialClinicosByFechaRegistroIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedHistorialClinico = historialClinicoRepository.saveAndFlush(historialClinico);

        // Get all the historialClinicoList where fechaRegistro is less than or equal to
        defaultHistorialClinicoFiltering(
            "fechaRegistro.lessThanOrEqual=" + DEFAULT_FECHA_REGISTRO,
            "fechaRegistro.lessThanOrEqual=" + SMALLER_FECHA_REGISTRO
        );
    }

    @Test
    @Transactional
    void getAllHistorialClinicosByFechaRegistroIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedHistorialClinico = historialClinicoRepository.saveAndFlush(historialClinico);

        // Get all the historialClinicoList where fechaRegistro is less than
        defaultHistorialClinicoFiltering(
            "fechaRegistro.lessThan=" + UPDATED_FECHA_REGISTRO,
            "fechaRegistro.lessThan=" + DEFAULT_FECHA_REGISTRO
        );
    }

    @Test
    @Transactional
    void getAllHistorialClinicosByFechaRegistroIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedHistorialClinico = historialClinicoRepository.saveAndFlush(historialClinico);

        // Get all the historialClinicoList where fechaRegistro is greater than
        defaultHistorialClinicoFiltering(
            "fechaRegistro.greaterThan=" + SMALLER_FECHA_REGISTRO,
            "fechaRegistro.greaterThan=" + DEFAULT_FECHA_REGISTRO
        );
    }

    @Test
    @Transactional
    void getAllHistorialClinicosByDescripcionIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedHistorialClinico = historialClinicoRepository.saveAndFlush(historialClinico);

        // Get all the historialClinicoList where descripcion equals to
        defaultHistorialClinicoFiltering("descripcion.equals=" + DEFAULT_DESCRIPCION, "descripcion.equals=" + UPDATED_DESCRIPCION);
    }

    @Test
    @Transactional
    void getAllHistorialClinicosByDescripcionIsInShouldWork() throws Exception {
        // Initialize the database
        insertedHistorialClinico = historialClinicoRepository.saveAndFlush(historialClinico);

        // Get all the historialClinicoList where descripcion in
        defaultHistorialClinicoFiltering(
            "descripcion.in=" + DEFAULT_DESCRIPCION + "," + UPDATED_DESCRIPCION,
            "descripcion.in=" + UPDATED_DESCRIPCION
        );
    }

    @Test
    @Transactional
    void getAllHistorialClinicosByDescripcionIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedHistorialClinico = historialClinicoRepository.saveAndFlush(historialClinico);

        // Get all the historialClinicoList where descripcion is not null
        defaultHistorialClinicoFiltering("descripcion.specified=true", "descripcion.specified=false");
    }

    @Test
    @Transactional
    void getAllHistorialClinicosByDescripcionContainsSomething() throws Exception {
        // Initialize the database
        insertedHistorialClinico = historialClinicoRepository.saveAndFlush(historialClinico);

        // Get all the historialClinicoList where descripcion contains
        defaultHistorialClinicoFiltering("descripcion.contains=" + DEFAULT_DESCRIPCION, "descripcion.contains=" + UPDATED_DESCRIPCION);
    }

    @Test
    @Transactional
    void getAllHistorialClinicosByDescripcionNotContainsSomething() throws Exception {
        // Initialize the database
        insertedHistorialClinico = historialClinicoRepository.saveAndFlush(historialClinico);

        // Get all the historialClinicoList where descripcion does not contain
        defaultHistorialClinicoFiltering(
            "descripcion.doesNotContain=" + UPDATED_DESCRIPCION,
            "descripcion.doesNotContain=" + DEFAULT_DESCRIPCION
        );
    }

    @Test
    @Transactional
    void getAllHistorialClinicosByExpedienteIsEqualToSomething() throws Exception {
        ExpedienteClinico expediente;
        if (TestUtil.findAll(em, ExpedienteClinico.class).isEmpty()) {
            historialClinicoRepository.saveAndFlush(historialClinico);
            expediente = ExpedienteClinicoResourceIT.createEntity();
        } else {
            expediente = TestUtil.findAll(em, ExpedienteClinico.class).get(0);
        }
        em.persist(expediente);
        em.flush();
        historialClinico.setExpediente(expediente);
        historialClinicoRepository.saveAndFlush(historialClinico);
        Long expedienteId = expediente.getId();
        // Get all the historialClinicoList where expediente equals to expedienteId
        defaultHistorialClinicoShouldBeFound("expedienteId.equals=" + expedienteId);

        // Get all the historialClinicoList where expediente equals to (expedienteId + 1)
        defaultHistorialClinicoShouldNotBeFound("expedienteId.equals=" + (expedienteId + 1));
    }

    private void defaultHistorialClinicoFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultHistorialClinicoShouldBeFound(shouldBeFound);
        defaultHistorialClinicoShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultHistorialClinicoShouldBeFound(String filter) throws Exception {
        restHistorialClinicoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(historialClinico.getId().intValue())))
            .andExpect(jsonPath("$.[*].fechaRegistro").value(hasItem(sameInstant(DEFAULT_FECHA_REGISTRO))))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)));

        // Check, that the count call also returns 1
        restHistorialClinicoMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultHistorialClinicoShouldNotBeFound(String filter) throws Exception {
        restHistorialClinicoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restHistorialClinicoMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingHistorialClinico() throws Exception {
        // Get the historialClinico
        restHistorialClinicoMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingHistorialClinico() throws Exception {
        // Initialize the database
        insertedHistorialClinico = historialClinicoRepository.saveAndFlush(historialClinico);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the historialClinico
        HistorialClinico updatedHistorialClinico = historialClinicoRepository.findById(historialClinico.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedHistorialClinico are not directly saved in db
        em.detach(updatedHistorialClinico);
        updatedHistorialClinico.fechaRegistro(UPDATED_FECHA_REGISTRO).descripcion(UPDATED_DESCRIPCION);
        HistorialClinicoDTO historialClinicoDTO = historialClinicoMapper.toDto(updatedHistorialClinico);

        restHistorialClinicoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, historialClinicoDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(historialClinicoDTO))
            )
            .andExpect(status().isOk());

        // Validate the HistorialClinico in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedHistorialClinicoToMatchAllProperties(updatedHistorialClinico);
    }

    @Test
    @Transactional
    void putNonExistingHistorialClinico() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        historialClinico.setId(longCount.incrementAndGet());

        // Create the HistorialClinico
        HistorialClinicoDTO historialClinicoDTO = historialClinicoMapper.toDto(historialClinico);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restHistorialClinicoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, historialClinicoDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(historialClinicoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the HistorialClinico in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchHistorialClinico() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        historialClinico.setId(longCount.incrementAndGet());

        // Create the HistorialClinico
        HistorialClinicoDTO historialClinicoDTO = historialClinicoMapper.toDto(historialClinico);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restHistorialClinicoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(historialClinicoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the HistorialClinico in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamHistorialClinico() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        historialClinico.setId(longCount.incrementAndGet());

        // Create the HistorialClinico
        HistorialClinicoDTO historialClinicoDTO = historialClinicoMapper.toDto(historialClinico);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restHistorialClinicoMockMvc
            .perform(
                put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(historialClinicoDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the HistorialClinico in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateHistorialClinicoWithPatch() throws Exception {
        // Initialize the database
        insertedHistorialClinico = historialClinicoRepository.saveAndFlush(historialClinico);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the historialClinico using partial update
        HistorialClinico partialUpdatedHistorialClinico = new HistorialClinico();
        partialUpdatedHistorialClinico.setId(historialClinico.getId());

        partialUpdatedHistorialClinico.descripcion(UPDATED_DESCRIPCION);

        restHistorialClinicoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedHistorialClinico.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedHistorialClinico))
            )
            .andExpect(status().isOk());

        // Validate the HistorialClinico in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertHistorialClinicoUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedHistorialClinico, historialClinico),
            getPersistedHistorialClinico(historialClinico)
        );
    }

    @Test
    @Transactional
    void fullUpdateHistorialClinicoWithPatch() throws Exception {
        // Initialize the database
        insertedHistorialClinico = historialClinicoRepository.saveAndFlush(historialClinico);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the historialClinico using partial update
        HistorialClinico partialUpdatedHistorialClinico = new HistorialClinico();
        partialUpdatedHistorialClinico.setId(historialClinico.getId());

        partialUpdatedHistorialClinico.fechaRegistro(UPDATED_FECHA_REGISTRO).descripcion(UPDATED_DESCRIPCION);

        restHistorialClinicoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedHistorialClinico.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedHistorialClinico))
            )
            .andExpect(status().isOk());

        // Validate the HistorialClinico in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertHistorialClinicoUpdatableFieldsEquals(
            partialUpdatedHistorialClinico,
            getPersistedHistorialClinico(partialUpdatedHistorialClinico)
        );
    }

    @Test
    @Transactional
    void patchNonExistingHistorialClinico() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        historialClinico.setId(longCount.incrementAndGet());

        // Create the HistorialClinico
        HistorialClinicoDTO historialClinicoDTO = historialClinicoMapper.toDto(historialClinico);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restHistorialClinicoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, historialClinicoDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(historialClinicoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the HistorialClinico in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchHistorialClinico() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        historialClinico.setId(longCount.incrementAndGet());

        // Create the HistorialClinico
        HistorialClinicoDTO historialClinicoDTO = historialClinicoMapper.toDto(historialClinico);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restHistorialClinicoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(historialClinicoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the HistorialClinico in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamHistorialClinico() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        historialClinico.setId(longCount.incrementAndGet());

        // Create the HistorialClinico
        HistorialClinicoDTO historialClinicoDTO = historialClinicoMapper.toDto(historialClinico);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restHistorialClinicoMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(historialClinicoDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the HistorialClinico in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteHistorialClinico() throws Exception {
        // Initialize the database
        insertedHistorialClinico = historialClinicoRepository.saveAndFlush(historialClinico);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the historialClinico
        restHistorialClinicoMockMvc
            .perform(delete(ENTITY_API_URL_ID, historialClinico.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return historialClinicoRepository.count();
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

    protected HistorialClinico getPersistedHistorialClinico(HistorialClinico historialClinico) {
        return historialClinicoRepository.findById(historialClinico.getId()).orElseThrow();
    }

    protected void assertPersistedHistorialClinicoToMatchAllProperties(HistorialClinico expectedHistorialClinico) {
        assertHistorialClinicoAllPropertiesEquals(expectedHistorialClinico, getPersistedHistorialClinico(expectedHistorialClinico));
    }

    protected void assertPersistedHistorialClinicoToMatchUpdatableProperties(HistorialClinico expectedHistorialClinico) {
        assertHistorialClinicoAllUpdatablePropertiesEquals(
            expectedHistorialClinico,
            getPersistedHistorialClinico(expectedHistorialClinico)
        );
    }
}
