package ni.edu.mney.web.rest;

import static ni.edu.mney.domain.TratamientoAsserts.*;
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
import ni.edu.mney.domain.Tratamiento;
import ni.edu.mney.repository.TratamientoRepository;
import ni.edu.mney.service.dto.TratamientoDTO;
import ni.edu.mney.service.mapper.TratamientoMapper;
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
 * Integration tests for the {@link TratamientoResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class TratamientoResourceIT {

    private static final String DEFAULT_INDICACIONES = "AAAAAAAAAA";
    private static final String UPDATED_INDICACIONES = "BBBBBBBBBB";

    private static final Integer DEFAULT_DURACION_DIAS = 1;
    private static final Integer UPDATED_DURACION_DIAS = 2;
    private static final Integer SMALLER_DURACION_DIAS = 1 - 1;

    private static final String ENTITY_API_URL = "/api/tratamientos";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TratamientoRepository tratamientoRepository;

    @Autowired
    private TratamientoMapper tratamientoMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTratamientoMockMvc;

    private Tratamiento tratamiento;

    private Tratamiento insertedTratamiento;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Tratamiento createEntity() {
        return new Tratamiento().indicaciones(DEFAULT_INDICACIONES).duracionDias(DEFAULT_DURACION_DIAS);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Tratamiento createUpdatedEntity() {
        return new Tratamiento().indicaciones(UPDATED_INDICACIONES).duracionDias(UPDATED_DURACION_DIAS);
    }

    @BeforeEach
    void initTest() {
        tratamiento = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedTratamiento != null) {
            tratamientoRepository.delete(insertedTratamiento);
            insertedTratamiento = null;
        }
    }

    @Test
    @Transactional
    void createTratamiento() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Tratamiento
        TratamientoDTO tratamientoDTO = tratamientoMapper.toDto(tratamiento);
        var returnedTratamientoDTO = om.readValue(
            restTratamientoMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(tratamientoDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            TratamientoDTO.class
        );

        // Validate the Tratamiento in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedTratamiento = tratamientoMapper.toEntity(returnedTratamientoDTO);
        assertTratamientoUpdatableFieldsEquals(returnedTratamiento, getPersistedTratamiento(returnedTratamiento));

        insertedTratamiento = returnedTratamiento;
    }

    @Test
    @Transactional
    void createTratamientoWithExistingId() throws Exception {
        // Create the Tratamiento with an existing ID
        tratamiento.setId(1L);
        TratamientoDTO tratamientoDTO = tratamientoMapper.toDto(tratamiento);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTratamientoMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(tratamientoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Tratamiento in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkIndicacionesIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        tratamiento.setIndicaciones(null);

        // Create the Tratamiento, which fails.
        TratamientoDTO tratamientoDTO = tratamientoMapper.toDto(tratamiento);

        restTratamientoMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(tratamientoDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllTratamientos() throws Exception {
        // Initialize the database
        insertedTratamiento = tratamientoRepository.saveAndFlush(tratamiento);

        // Get all the tratamientoList
        restTratamientoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(tratamiento.getId().intValue())))
            .andExpect(jsonPath("$.[*].indicaciones").value(hasItem(DEFAULT_INDICACIONES)))
            .andExpect(jsonPath("$.[*].duracionDias").value(hasItem(DEFAULT_DURACION_DIAS)));
    }

    @Test
    @Transactional
    void getTratamiento() throws Exception {
        // Initialize the database
        insertedTratamiento = tratamientoRepository.saveAndFlush(tratamiento);

        // Get the tratamiento
        restTratamientoMockMvc
            .perform(get(ENTITY_API_URL_ID, tratamiento.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(tratamiento.getId().intValue()))
            .andExpect(jsonPath("$.indicaciones").value(DEFAULT_INDICACIONES))
            .andExpect(jsonPath("$.duracionDias").value(DEFAULT_DURACION_DIAS));
    }

    @Test
    @Transactional
    void getTratamientosByIdFiltering() throws Exception {
        // Initialize the database
        insertedTratamiento = tratamientoRepository.saveAndFlush(tratamiento);

        Long id = tratamiento.getId();

        defaultTratamientoFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultTratamientoFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultTratamientoFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllTratamientosByIndicacionesIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTratamiento = tratamientoRepository.saveAndFlush(tratamiento);

        // Get all the tratamientoList where indicaciones equals to
        defaultTratamientoFiltering("indicaciones.equals=" + DEFAULT_INDICACIONES, "indicaciones.equals=" + UPDATED_INDICACIONES);
    }

    @Test
    @Transactional
    void getAllTratamientosByIndicacionesIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTratamiento = tratamientoRepository.saveAndFlush(tratamiento);

        // Get all the tratamientoList where indicaciones in
        defaultTratamientoFiltering(
            "indicaciones.in=" + DEFAULT_INDICACIONES + "," + UPDATED_INDICACIONES,
            "indicaciones.in=" + UPDATED_INDICACIONES
        );
    }

    @Test
    @Transactional
    void getAllTratamientosByIndicacionesIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTratamiento = tratamientoRepository.saveAndFlush(tratamiento);

        // Get all the tratamientoList where indicaciones is not null
        defaultTratamientoFiltering("indicaciones.specified=true", "indicaciones.specified=false");
    }

    @Test
    @Transactional
    void getAllTratamientosByIndicacionesContainsSomething() throws Exception {
        // Initialize the database
        insertedTratamiento = tratamientoRepository.saveAndFlush(tratamiento);

        // Get all the tratamientoList where indicaciones contains
        defaultTratamientoFiltering("indicaciones.contains=" + DEFAULT_INDICACIONES, "indicaciones.contains=" + UPDATED_INDICACIONES);
    }

    @Test
    @Transactional
    void getAllTratamientosByIndicacionesNotContainsSomething() throws Exception {
        // Initialize the database
        insertedTratamiento = tratamientoRepository.saveAndFlush(tratamiento);

        // Get all the tratamientoList where indicaciones does not contain
        defaultTratamientoFiltering(
            "indicaciones.doesNotContain=" + UPDATED_INDICACIONES,
            "indicaciones.doesNotContain=" + DEFAULT_INDICACIONES
        );
    }

    @Test
    @Transactional
    void getAllTratamientosByDuracionDiasIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTratamiento = tratamientoRepository.saveAndFlush(tratamiento);

        // Get all the tratamientoList where duracionDias equals to
        defaultTratamientoFiltering("duracionDias.equals=" + DEFAULT_DURACION_DIAS, "duracionDias.equals=" + UPDATED_DURACION_DIAS);
    }

    @Test
    @Transactional
    void getAllTratamientosByDuracionDiasIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTratamiento = tratamientoRepository.saveAndFlush(tratamiento);

        // Get all the tratamientoList where duracionDias in
        defaultTratamientoFiltering(
            "duracionDias.in=" + DEFAULT_DURACION_DIAS + "," + UPDATED_DURACION_DIAS,
            "duracionDias.in=" + UPDATED_DURACION_DIAS
        );
    }

    @Test
    @Transactional
    void getAllTratamientosByDuracionDiasIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTratamiento = tratamientoRepository.saveAndFlush(tratamiento);

        // Get all the tratamientoList where duracionDias is not null
        defaultTratamientoFiltering("duracionDias.specified=true", "duracionDias.specified=false");
    }

    @Test
    @Transactional
    void getAllTratamientosByDuracionDiasIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedTratamiento = tratamientoRepository.saveAndFlush(tratamiento);

        // Get all the tratamientoList where duracionDias is greater than or equal to
        defaultTratamientoFiltering(
            "duracionDias.greaterThanOrEqual=" + DEFAULT_DURACION_DIAS,
            "duracionDias.greaterThanOrEqual=" + UPDATED_DURACION_DIAS
        );
    }

    @Test
    @Transactional
    void getAllTratamientosByDuracionDiasIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedTratamiento = tratamientoRepository.saveAndFlush(tratamiento);

        // Get all the tratamientoList where duracionDias is less than or equal to
        defaultTratamientoFiltering(
            "duracionDias.lessThanOrEqual=" + DEFAULT_DURACION_DIAS,
            "duracionDias.lessThanOrEqual=" + SMALLER_DURACION_DIAS
        );
    }

    @Test
    @Transactional
    void getAllTratamientosByDuracionDiasIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedTratamiento = tratamientoRepository.saveAndFlush(tratamiento);

        // Get all the tratamientoList where duracionDias is less than
        defaultTratamientoFiltering("duracionDias.lessThan=" + UPDATED_DURACION_DIAS, "duracionDias.lessThan=" + DEFAULT_DURACION_DIAS);
    }

    @Test
    @Transactional
    void getAllTratamientosByDuracionDiasIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedTratamiento = tratamientoRepository.saveAndFlush(tratamiento);

        // Get all the tratamientoList where duracionDias is greater than
        defaultTratamientoFiltering(
            "duracionDias.greaterThan=" + SMALLER_DURACION_DIAS,
            "duracionDias.greaterThan=" + DEFAULT_DURACION_DIAS
        );
    }

    @Test
    @Transactional
    void getAllTratamientosByConsultaIsEqualToSomething() throws Exception {
        ConsultaMedica consulta;
        if (TestUtil.findAll(em, ConsultaMedica.class).isEmpty()) {
            tratamientoRepository.saveAndFlush(tratamiento);
            consulta = ConsultaMedicaResourceIT.createEntity();
        } else {
            consulta = TestUtil.findAll(em, ConsultaMedica.class).get(0);
        }
        em.persist(consulta);
        em.flush();
        tratamiento.setConsulta(consulta);
        tratamientoRepository.saveAndFlush(tratamiento);
        Long consultaId = consulta.getId();
        // Get all the tratamientoList where consulta equals to consultaId
        defaultTratamientoShouldBeFound("consultaId.equals=" + consultaId);

        // Get all the tratamientoList where consulta equals to (consultaId + 1)
        defaultTratamientoShouldNotBeFound("consultaId.equals=" + (consultaId + 1));
    }

    private void defaultTratamientoFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultTratamientoShouldBeFound(shouldBeFound);
        defaultTratamientoShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultTratamientoShouldBeFound(String filter) throws Exception {
        restTratamientoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(tratamiento.getId().intValue())))
            .andExpect(jsonPath("$.[*].indicaciones").value(hasItem(DEFAULT_INDICACIONES)))
            .andExpect(jsonPath("$.[*].duracionDias").value(hasItem(DEFAULT_DURACION_DIAS)));

        // Check, that the count call also returns 1
        restTratamientoMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultTratamientoShouldNotBeFound(String filter) throws Exception {
        restTratamientoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restTratamientoMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingTratamiento() throws Exception {
        // Get the tratamiento
        restTratamientoMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingTratamiento() throws Exception {
        // Initialize the database
        insertedTratamiento = tratamientoRepository.saveAndFlush(tratamiento);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the tratamiento
        Tratamiento updatedTratamiento = tratamientoRepository.findById(tratamiento.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedTratamiento are not directly saved in db
        em.detach(updatedTratamiento);
        updatedTratamiento.indicaciones(UPDATED_INDICACIONES).duracionDias(UPDATED_DURACION_DIAS);
        TratamientoDTO tratamientoDTO = tratamientoMapper.toDto(updatedTratamiento);

        restTratamientoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, tratamientoDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(tratamientoDTO))
            )
            .andExpect(status().isOk());

        // Validate the Tratamiento in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedTratamientoToMatchAllProperties(updatedTratamiento);
    }

    @Test
    @Transactional
    void putNonExistingTratamiento() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tratamiento.setId(longCount.incrementAndGet());

        // Create the Tratamiento
        TratamientoDTO tratamientoDTO = tratamientoMapper.toDto(tratamiento);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTratamientoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, tratamientoDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(tratamientoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Tratamiento in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchTratamiento() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tratamiento.setId(longCount.incrementAndGet());

        // Create the Tratamiento
        TratamientoDTO tratamientoDTO = tratamientoMapper.toDto(tratamiento);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTratamientoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(tratamientoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Tratamiento in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTratamiento() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tratamiento.setId(longCount.incrementAndGet());

        // Create the Tratamiento
        TratamientoDTO tratamientoDTO = tratamientoMapper.toDto(tratamiento);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTratamientoMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(tratamientoDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Tratamiento in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateTratamientoWithPatch() throws Exception {
        // Initialize the database
        insertedTratamiento = tratamientoRepository.saveAndFlush(tratamiento);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the tratamiento using partial update
        Tratamiento partialUpdatedTratamiento = new Tratamiento();
        partialUpdatedTratamiento.setId(tratamiento.getId());

        partialUpdatedTratamiento.indicaciones(UPDATED_INDICACIONES).duracionDias(UPDATED_DURACION_DIAS);

        restTratamientoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTratamiento.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTratamiento))
            )
            .andExpect(status().isOk());

        // Validate the Tratamiento in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTratamientoUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedTratamiento, tratamiento),
            getPersistedTratamiento(tratamiento)
        );
    }

    @Test
    @Transactional
    void fullUpdateTratamientoWithPatch() throws Exception {
        // Initialize the database
        insertedTratamiento = tratamientoRepository.saveAndFlush(tratamiento);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the tratamiento using partial update
        Tratamiento partialUpdatedTratamiento = new Tratamiento();
        partialUpdatedTratamiento.setId(tratamiento.getId());

        partialUpdatedTratamiento.indicaciones(UPDATED_INDICACIONES).duracionDias(UPDATED_DURACION_DIAS);

        restTratamientoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTratamiento.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTratamiento))
            )
            .andExpect(status().isOk());

        // Validate the Tratamiento in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTratamientoUpdatableFieldsEquals(partialUpdatedTratamiento, getPersistedTratamiento(partialUpdatedTratamiento));
    }

    @Test
    @Transactional
    void patchNonExistingTratamiento() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tratamiento.setId(longCount.incrementAndGet());

        // Create the Tratamiento
        TratamientoDTO tratamientoDTO = tratamientoMapper.toDto(tratamiento);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTratamientoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, tratamientoDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(tratamientoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Tratamiento in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTratamiento() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tratamiento.setId(longCount.incrementAndGet());

        // Create the Tratamiento
        TratamientoDTO tratamientoDTO = tratamientoMapper.toDto(tratamiento);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTratamientoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(tratamientoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Tratamiento in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTratamiento() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tratamiento.setId(longCount.incrementAndGet());

        // Create the Tratamiento
        TratamientoDTO tratamientoDTO = tratamientoMapper.toDto(tratamiento);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTratamientoMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(tratamientoDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Tratamiento in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteTratamiento() throws Exception {
        // Initialize the database
        insertedTratamiento = tratamientoRepository.saveAndFlush(tratamiento);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the tratamiento
        restTratamientoMockMvc
            .perform(delete(ENTITY_API_URL_ID, tratamiento.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return tratamientoRepository.count();
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

    protected Tratamiento getPersistedTratamiento(Tratamiento tratamiento) {
        return tratamientoRepository.findById(tratamiento.getId()).orElseThrow();
    }

    protected void assertPersistedTratamientoToMatchAllProperties(Tratamiento expectedTratamiento) {
        assertTratamientoAllPropertiesEquals(expectedTratamiento, getPersistedTratamiento(expectedTratamiento));
    }

    protected void assertPersistedTratamientoToMatchUpdatableProperties(Tratamiento expectedTratamiento) {
        assertTratamientoAllUpdatablePropertiesEquals(expectedTratamiento, getPersistedTratamiento(expectedTratamiento));
    }
}
