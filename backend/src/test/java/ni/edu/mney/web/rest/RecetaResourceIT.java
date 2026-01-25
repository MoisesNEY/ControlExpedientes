package ni.edu.mney.web.rest;

import static ni.edu.mney.domain.RecetaAsserts.*;
import static ni.edu.mney.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import ni.edu.mney.IntegrationTest;
import ni.edu.mney.domain.ConsultaMedica;
import ni.edu.mney.domain.Medicamento;
import ni.edu.mney.domain.Receta;
import ni.edu.mney.repository.RecetaRepository;
import ni.edu.mney.service.dto.RecetaDTO;
import ni.edu.mney.service.mapper.RecetaMapper;
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
 * Integration tests for the {@link RecetaResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class RecetaResourceIT {

    private static final String DEFAULT_DOSIS = "AAAAAAAAAA";
    private static final String UPDATED_DOSIS = "BBBBBBBBBB";

    private static final String DEFAULT_FRECUENCIA = "AAAAAAAAAA";
    private static final String UPDATED_FRECUENCIA = "BBBBBBBBBB";

    private static final String DEFAULT_DURACION = "AAAAAAAAAA";
    private static final String UPDATED_DURACION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/recetas";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private RecetaRepository recetaRepository;

    @Autowired
    private RecetaMapper recetaMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restRecetaMockMvc;

    private Receta receta;

    private Receta insertedReceta;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Receta createEntity() {
        return new Receta().dosis(DEFAULT_DOSIS).frecuencia(DEFAULT_FRECUENCIA).duracion(DEFAULT_DURACION);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Receta createUpdatedEntity() {
        return new Receta().dosis(UPDATED_DOSIS).frecuencia(UPDATED_FRECUENCIA).duracion(UPDATED_DURACION);
    }

    @BeforeEach
    void initTest() {
        receta = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedReceta != null) {
            recetaRepository.delete(insertedReceta);
            insertedReceta = null;
        }
    }

    @Test
    @Transactional
    void createReceta() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Receta
        RecetaDTO recetaDTO = recetaMapper.toDto(receta);
        var returnedRecetaDTO = om.readValue(
            restRecetaMockMvc
                .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(recetaDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            RecetaDTO.class
        );

        // Validate the Receta in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedReceta = recetaMapper.toEntity(returnedRecetaDTO);
        assertRecetaUpdatableFieldsEquals(returnedReceta, getPersistedReceta(returnedReceta));

        insertedReceta = returnedReceta;
    }

    @Test
    @Transactional
    void createRecetaWithExistingId() throws Exception {
        // Create the Receta with an existing ID
        receta.setId(1L);
        RecetaDTO recetaDTO = recetaMapper.toDto(receta);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restRecetaMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(recetaDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Receta in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkDosisIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        receta.setDosis(null);

        // Create the Receta, which fails.
        RecetaDTO recetaDTO = recetaMapper.toDto(receta);

        restRecetaMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(recetaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkFrecuenciaIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        receta.setFrecuencia(null);

        // Create the Receta, which fails.
        RecetaDTO recetaDTO = recetaMapper.toDto(receta);

        restRecetaMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(recetaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkDuracionIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        receta.setDuracion(null);

        // Create the Receta, which fails.
        RecetaDTO recetaDTO = recetaMapper.toDto(receta);

        restRecetaMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(recetaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllRecetas() throws Exception {
        // Initialize the database
        insertedReceta = recetaRepository.saveAndFlush(receta);

        // Get all the recetaList
        restRecetaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(receta.getId().intValue())))
            .andExpect(jsonPath("$.[*].dosis").value(hasItem(DEFAULT_DOSIS)))
            .andExpect(jsonPath("$.[*].frecuencia").value(hasItem(DEFAULT_FRECUENCIA)))
            .andExpect(jsonPath("$.[*].duracion").value(hasItem(DEFAULT_DURACION)));
    }

    @Test
    @Transactional
    void getReceta() throws Exception {
        // Initialize the database
        insertedReceta = recetaRepository.saveAndFlush(receta);

        // Get the receta
        restRecetaMockMvc
            .perform(get(ENTITY_API_URL_ID, receta.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(receta.getId().intValue()))
            .andExpect(jsonPath("$.dosis").value(DEFAULT_DOSIS))
            .andExpect(jsonPath("$.frecuencia").value(DEFAULT_FRECUENCIA))
            .andExpect(jsonPath("$.duracion").value(DEFAULT_DURACION));
    }

    @Test
    @Transactional
    void getRecetasByIdFiltering() throws Exception {
        // Initialize the database
        insertedReceta = recetaRepository.saveAndFlush(receta);

        Long id = receta.getId();

        defaultRecetaFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultRecetaFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultRecetaFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllRecetasByDosisIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedReceta = recetaRepository.saveAndFlush(receta);

        // Get all the recetaList where dosis equals to
        defaultRecetaFiltering("dosis.equals=" + DEFAULT_DOSIS, "dosis.equals=" + UPDATED_DOSIS);
    }

    @Test
    @Transactional
    void getAllRecetasByDosisIsInShouldWork() throws Exception {
        // Initialize the database
        insertedReceta = recetaRepository.saveAndFlush(receta);

        // Get all the recetaList where dosis in
        defaultRecetaFiltering("dosis.in=" + DEFAULT_DOSIS + "," + UPDATED_DOSIS, "dosis.in=" + UPDATED_DOSIS);
    }

    @Test
    @Transactional
    void getAllRecetasByDosisIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedReceta = recetaRepository.saveAndFlush(receta);

        // Get all the recetaList where dosis is not null
        defaultRecetaFiltering("dosis.specified=true", "dosis.specified=false");
    }

    @Test
    @Transactional
    void getAllRecetasByDosisContainsSomething() throws Exception {
        // Initialize the database
        insertedReceta = recetaRepository.saveAndFlush(receta);

        // Get all the recetaList where dosis contains
        defaultRecetaFiltering("dosis.contains=" + DEFAULT_DOSIS, "dosis.contains=" + UPDATED_DOSIS);
    }

    @Test
    @Transactional
    void getAllRecetasByDosisNotContainsSomething() throws Exception {
        // Initialize the database
        insertedReceta = recetaRepository.saveAndFlush(receta);

        // Get all the recetaList where dosis does not contain
        defaultRecetaFiltering("dosis.doesNotContain=" + UPDATED_DOSIS, "dosis.doesNotContain=" + DEFAULT_DOSIS);
    }

    @Test
    @Transactional
    void getAllRecetasByFrecuenciaIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedReceta = recetaRepository.saveAndFlush(receta);

        // Get all the recetaList where frecuencia equals to
        defaultRecetaFiltering("frecuencia.equals=" + DEFAULT_FRECUENCIA, "frecuencia.equals=" + UPDATED_FRECUENCIA);
    }

    @Test
    @Transactional
    void getAllRecetasByFrecuenciaIsInShouldWork() throws Exception {
        // Initialize the database
        insertedReceta = recetaRepository.saveAndFlush(receta);

        // Get all the recetaList where frecuencia in
        defaultRecetaFiltering("frecuencia.in=" + DEFAULT_FRECUENCIA + "," + UPDATED_FRECUENCIA, "frecuencia.in=" + UPDATED_FRECUENCIA);
    }

    @Test
    @Transactional
    void getAllRecetasByFrecuenciaIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedReceta = recetaRepository.saveAndFlush(receta);

        // Get all the recetaList where frecuencia is not null
        defaultRecetaFiltering("frecuencia.specified=true", "frecuencia.specified=false");
    }

    @Test
    @Transactional
    void getAllRecetasByFrecuenciaContainsSomething() throws Exception {
        // Initialize the database
        insertedReceta = recetaRepository.saveAndFlush(receta);

        // Get all the recetaList where frecuencia contains
        defaultRecetaFiltering("frecuencia.contains=" + DEFAULT_FRECUENCIA, "frecuencia.contains=" + UPDATED_FRECUENCIA);
    }

    @Test
    @Transactional
    void getAllRecetasByFrecuenciaNotContainsSomething() throws Exception {
        // Initialize the database
        insertedReceta = recetaRepository.saveAndFlush(receta);

        // Get all the recetaList where frecuencia does not contain
        defaultRecetaFiltering("frecuencia.doesNotContain=" + UPDATED_FRECUENCIA, "frecuencia.doesNotContain=" + DEFAULT_FRECUENCIA);
    }

    @Test
    @Transactional
    void getAllRecetasByDuracionIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedReceta = recetaRepository.saveAndFlush(receta);

        // Get all the recetaList where duracion equals to
        defaultRecetaFiltering("duracion.equals=" + DEFAULT_DURACION, "duracion.equals=" + UPDATED_DURACION);
    }

    @Test
    @Transactional
    void getAllRecetasByDuracionIsInShouldWork() throws Exception {
        // Initialize the database
        insertedReceta = recetaRepository.saveAndFlush(receta);

        // Get all the recetaList where duracion in
        defaultRecetaFiltering("duracion.in=" + DEFAULT_DURACION + "," + UPDATED_DURACION, "duracion.in=" + UPDATED_DURACION);
    }

    @Test
    @Transactional
    void getAllRecetasByDuracionIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedReceta = recetaRepository.saveAndFlush(receta);

        // Get all the recetaList where duracion is not null
        defaultRecetaFiltering("duracion.specified=true", "duracion.specified=false");
    }

    @Test
    @Transactional
    void getAllRecetasByDuracionContainsSomething() throws Exception {
        // Initialize the database
        insertedReceta = recetaRepository.saveAndFlush(receta);

        // Get all the recetaList where duracion contains
        defaultRecetaFiltering("duracion.contains=" + DEFAULT_DURACION, "duracion.contains=" + UPDATED_DURACION);
    }

    @Test
    @Transactional
    void getAllRecetasByDuracionNotContainsSomething() throws Exception {
        // Initialize the database
        insertedReceta = recetaRepository.saveAndFlush(receta);

        // Get all the recetaList where duracion does not contain
        defaultRecetaFiltering("duracion.doesNotContain=" + UPDATED_DURACION, "duracion.doesNotContain=" + DEFAULT_DURACION);
    }

    @Test
    @Transactional
    void getAllRecetasByMedicamentoIsEqualToSomething() throws Exception {
        Medicamento medicamento;
        if (TestUtil.findAll(em, Medicamento.class).isEmpty()) {
            recetaRepository.saveAndFlush(receta);
            medicamento = MedicamentoResourceIT.createEntity();
        } else {
            medicamento = TestUtil.findAll(em, Medicamento.class).get(0);
        }
        em.persist(medicamento);
        em.flush();
        receta.setMedicamento(medicamento);
        recetaRepository.saveAndFlush(receta);
        Long medicamentoId = medicamento.getId();
        // Get all the recetaList where medicamento equals to medicamentoId
        defaultRecetaShouldBeFound("medicamentoId.equals=" + medicamentoId);

        // Get all the recetaList where medicamento equals to (medicamentoId + 1)
        defaultRecetaShouldNotBeFound("medicamentoId.equals=" + (medicamentoId + 1));
    }

    @Test
    @Transactional
    void getAllRecetasByConsultaIsEqualToSomething() throws Exception {
        ConsultaMedica consulta;
        if (TestUtil.findAll(em, ConsultaMedica.class).isEmpty()) {
            recetaRepository.saveAndFlush(receta);
            consulta = ConsultaMedicaResourceIT.createEntity();
        } else {
            consulta = TestUtil.findAll(em, ConsultaMedica.class).get(0);
        }
        em.persist(consulta);
        em.flush();
        receta.setConsulta(consulta);
        recetaRepository.saveAndFlush(receta);
        Long consultaId = consulta.getId();
        // Get all the recetaList where consulta equals to consultaId
        defaultRecetaShouldBeFound("consultaId.equals=" + consultaId);

        // Get all the recetaList where consulta equals to (consultaId + 1)
        defaultRecetaShouldNotBeFound("consultaId.equals=" + (consultaId + 1));
    }

    private void defaultRecetaFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultRecetaShouldBeFound(shouldBeFound);
        defaultRecetaShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultRecetaShouldBeFound(String filter) throws Exception {
        restRecetaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(receta.getId().intValue())))
            .andExpect(jsonPath("$.[*].dosis").value(hasItem(DEFAULT_DOSIS)))
            .andExpect(jsonPath("$.[*].frecuencia").value(hasItem(DEFAULT_FRECUENCIA)))
            .andExpect(jsonPath("$.[*].duracion").value(hasItem(DEFAULT_DURACION)));

        // Check, that the count call also returns 1
        restRecetaMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultRecetaShouldNotBeFound(String filter) throws Exception {
        restRecetaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restRecetaMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingReceta() throws Exception {
        // Get the receta
        restRecetaMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingReceta() throws Exception {
        // Initialize the database
        insertedReceta = recetaRepository.saveAndFlush(receta);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the receta
        Receta updatedReceta = recetaRepository.findById(receta.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedReceta are not directly saved in db
        em.detach(updatedReceta);
        updatedReceta.dosis(UPDATED_DOSIS).frecuencia(UPDATED_FRECUENCIA).duracion(UPDATED_DURACION);
        RecetaDTO recetaDTO = recetaMapper.toDto(updatedReceta);

        restRecetaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, recetaDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(recetaDTO))
            )
            .andExpect(status().isOk());

        // Validate the Receta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedRecetaToMatchAllProperties(updatedReceta);
    }

    @Test
    @Transactional
    void putNonExistingReceta() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        receta.setId(longCount.incrementAndGet());

        // Create the Receta
        RecetaDTO recetaDTO = recetaMapper.toDto(receta);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRecetaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, recetaDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(recetaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Receta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchReceta() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        receta.setId(longCount.incrementAndGet());

        // Create the Receta
        RecetaDTO recetaDTO = recetaMapper.toDto(receta);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRecetaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(recetaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Receta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamReceta() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        receta.setId(longCount.incrementAndGet());

        // Create the Receta
        RecetaDTO recetaDTO = recetaMapper.toDto(receta);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRecetaMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(recetaDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Receta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateRecetaWithPatch() throws Exception {
        // Initialize the database
        insertedReceta = recetaRepository.saveAndFlush(receta);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the receta using partial update
        Receta partialUpdatedReceta = new Receta();
        partialUpdatedReceta.setId(receta.getId());

        partialUpdatedReceta.frecuencia(UPDATED_FRECUENCIA).duracion(UPDATED_DURACION);

        restRecetaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedReceta.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedReceta))
            )
            .andExpect(status().isOk());

        // Validate the Receta in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertRecetaUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedReceta, receta), getPersistedReceta(receta));
    }

    @Test
    @Transactional
    void fullUpdateRecetaWithPatch() throws Exception {
        // Initialize the database
        insertedReceta = recetaRepository.saveAndFlush(receta);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the receta using partial update
        Receta partialUpdatedReceta = new Receta();
        partialUpdatedReceta.setId(receta.getId());

        partialUpdatedReceta.dosis(UPDATED_DOSIS).frecuencia(UPDATED_FRECUENCIA).duracion(UPDATED_DURACION);

        restRecetaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedReceta.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedReceta))
            )
            .andExpect(status().isOk());

        // Validate the Receta in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertRecetaUpdatableFieldsEquals(partialUpdatedReceta, getPersistedReceta(partialUpdatedReceta));
    }

    @Test
    @Transactional
    void patchNonExistingReceta() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        receta.setId(longCount.incrementAndGet());

        // Create the Receta
        RecetaDTO recetaDTO = recetaMapper.toDto(receta);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRecetaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, recetaDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(recetaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Receta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchReceta() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        receta.setId(longCount.incrementAndGet());

        // Create the Receta
        RecetaDTO recetaDTO = recetaMapper.toDto(receta);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRecetaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(recetaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Receta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamReceta() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        receta.setId(longCount.incrementAndGet());

        // Create the Receta
        RecetaDTO recetaDTO = recetaMapper.toDto(receta);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRecetaMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(recetaDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Receta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteReceta() throws Exception {
        // Initialize the database
        insertedReceta = recetaRepository.saveAndFlush(receta);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the receta
        restRecetaMockMvc
            .perform(delete(ENTITY_API_URL_ID, receta.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return recetaRepository.count();
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

    protected Receta getPersistedReceta(Receta receta) {
        return recetaRepository.findById(receta.getId()).orElseThrow();
    }

    protected void assertPersistedRecetaToMatchAllProperties(Receta expectedReceta) {
        assertRecetaAllPropertiesEquals(expectedReceta, getPersistedReceta(expectedReceta));
    }

    protected void assertPersistedRecetaToMatchUpdatableProperties(Receta expectedReceta) {
        assertRecetaAllUpdatablePropertiesEquals(expectedReceta, getPersistedReceta(expectedReceta));
    }
}
