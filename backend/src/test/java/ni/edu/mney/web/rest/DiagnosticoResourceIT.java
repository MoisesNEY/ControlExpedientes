package ni.edu.mney.web.rest;

import static ni.edu.mney.domain.DiagnosticoAsserts.*;
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
import ni.edu.mney.domain.Diagnostico;
import ni.edu.mney.repository.DiagnosticoRepository;
import ni.edu.mney.service.dto.DiagnosticoDTO;
import ni.edu.mney.service.mapper.DiagnosticoMapper;
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
 * Integration tests for the {@link DiagnosticoResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class DiagnosticoResourceIT {

    private static final String DEFAULT_DESCRIPCION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPCION = "BBBBBBBBBB";

    private static final String DEFAULT_CODIGO_CIE = "AAAAAAAAAA";
    private static final String UPDATED_CODIGO_CIE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/diagnosticos";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private DiagnosticoRepository diagnosticoRepository;

    @Autowired
    private DiagnosticoMapper diagnosticoMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restDiagnosticoMockMvc;

    private Diagnostico diagnostico;

    private Diagnostico insertedDiagnostico;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Diagnostico createEntity() {
        return new Diagnostico().descripcion(DEFAULT_DESCRIPCION).codigoCIE(DEFAULT_CODIGO_CIE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Diagnostico createUpdatedEntity() {
        return new Diagnostico().descripcion(UPDATED_DESCRIPCION).codigoCIE(UPDATED_CODIGO_CIE);
    }

    @BeforeEach
    void initTest() {
        diagnostico = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedDiagnostico != null) {
            diagnosticoRepository.delete(insertedDiagnostico);
            insertedDiagnostico = null;
        }
    }

    @Test
    @Transactional
    void createDiagnostico() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Diagnostico
        DiagnosticoDTO diagnosticoDTO = diagnosticoMapper.toDto(diagnostico);
        var returnedDiagnosticoDTO = om.readValue(
            restDiagnosticoMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(diagnosticoDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            DiagnosticoDTO.class
        );

        // Validate the Diagnostico in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedDiagnostico = diagnosticoMapper.toEntity(returnedDiagnosticoDTO);
        assertDiagnosticoUpdatableFieldsEquals(returnedDiagnostico, getPersistedDiagnostico(returnedDiagnostico));

        insertedDiagnostico = returnedDiagnostico;
    }

    @Test
    @Transactional
    void createDiagnosticoWithExistingId() throws Exception {
        // Create the Diagnostico with an existing ID
        diagnostico.setId(1L);
        DiagnosticoDTO diagnosticoDTO = diagnosticoMapper.toDto(diagnostico);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restDiagnosticoMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(diagnosticoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Diagnostico in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkDescripcionIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        diagnostico.setDescripcion(null);

        // Create the Diagnostico, which fails.
        DiagnosticoDTO diagnosticoDTO = diagnosticoMapper.toDto(diagnostico);

        restDiagnosticoMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(diagnosticoDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllDiagnosticos() throws Exception {
        // Initialize the database
        insertedDiagnostico = diagnosticoRepository.saveAndFlush(diagnostico);

        // Get all the diagnosticoList
        restDiagnosticoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(diagnostico.getId().intValue())))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)))
            .andExpect(jsonPath("$.[*].codigoCIE").value(hasItem(DEFAULT_CODIGO_CIE)));
    }

    @Test
    @Transactional
    void getDiagnostico() throws Exception {
        // Initialize the database
        insertedDiagnostico = diagnosticoRepository.saveAndFlush(diagnostico);

        // Get the diagnostico
        restDiagnosticoMockMvc
            .perform(get(ENTITY_API_URL_ID, diagnostico.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(diagnostico.getId().intValue()))
            .andExpect(jsonPath("$.descripcion").value(DEFAULT_DESCRIPCION))
            .andExpect(jsonPath("$.codigoCIE").value(DEFAULT_CODIGO_CIE));
    }

    @Test
    @Transactional
    void getDiagnosticosByIdFiltering() throws Exception {
        // Initialize the database
        insertedDiagnostico = diagnosticoRepository.saveAndFlush(diagnostico);

        Long id = diagnostico.getId();

        defaultDiagnosticoFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultDiagnosticoFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultDiagnosticoFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllDiagnosticosByDescripcionIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedDiagnostico = diagnosticoRepository.saveAndFlush(diagnostico);

        // Get all the diagnosticoList where descripcion equals to
        defaultDiagnosticoFiltering("descripcion.equals=" + DEFAULT_DESCRIPCION, "descripcion.equals=" + UPDATED_DESCRIPCION);
    }

    @Test
    @Transactional
    void getAllDiagnosticosByDescripcionIsInShouldWork() throws Exception {
        // Initialize the database
        insertedDiagnostico = diagnosticoRepository.saveAndFlush(diagnostico);

        // Get all the diagnosticoList where descripcion in
        defaultDiagnosticoFiltering(
            "descripcion.in=" + DEFAULT_DESCRIPCION + "," + UPDATED_DESCRIPCION,
            "descripcion.in=" + UPDATED_DESCRIPCION
        );
    }

    @Test
    @Transactional
    void getAllDiagnosticosByDescripcionIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedDiagnostico = diagnosticoRepository.saveAndFlush(diagnostico);

        // Get all the diagnosticoList where descripcion is not null
        defaultDiagnosticoFiltering("descripcion.specified=true", "descripcion.specified=false");
    }

    @Test
    @Transactional
    void getAllDiagnosticosByDescripcionContainsSomething() throws Exception {
        // Initialize the database
        insertedDiagnostico = diagnosticoRepository.saveAndFlush(diagnostico);

        // Get all the diagnosticoList where descripcion contains
        defaultDiagnosticoFiltering("descripcion.contains=" + DEFAULT_DESCRIPCION, "descripcion.contains=" + UPDATED_DESCRIPCION);
    }

    @Test
    @Transactional
    void getAllDiagnosticosByDescripcionNotContainsSomething() throws Exception {
        // Initialize the database
        insertedDiagnostico = diagnosticoRepository.saveAndFlush(diagnostico);

        // Get all the diagnosticoList where descripcion does not contain
        defaultDiagnosticoFiltering(
            "descripcion.doesNotContain=" + UPDATED_DESCRIPCION,
            "descripcion.doesNotContain=" + DEFAULT_DESCRIPCION
        );
    }

    @Test
    @Transactional
    void getAllDiagnosticosByCodigoCIEIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedDiagnostico = diagnosticoRepository.saveAndFlush(diagnostico);

        // Get all the diagnosticoList where codigoCIE equals to
        defaultDiagnosticoFiltering("codigoCIE.equals=" + DEFAULT_CODIGO_CIE, "codigoCIE.equals=" + UPDATED_CODIGO_CIE);
    }

    @Test
    @Transactional
    void getAllDiagnosticosByCodigoCIEIsInShouldWork() throws Exception {
        // Initialize the database
        insertedDiagnostico = diagnosticoRepository.saveAndFlush(diagnostico);

        // Get all the diagnosticoList where codigoCIE in
        defaultDiagnosticoFiltering("codigoCIE.in=" + DEFAULT_CODIGO_CIE + "," + UPDATED_CODIGO_CIE, "codigoCIE.in=" + UPDATED_CODIGO_CIE);
    }

    @Test
    @Transactional
    void getAllDiagnosticosByCodigoCIEIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedDiagnostico = diagnosticoRepository.saveAndFlush(diagnostico);

        // Get all the diagnosticoList where codigoCIE is not null
        defaultDiagnosticoFiltering("codigoCIE.specified=true", "codigoCIE.specified=false");
    }

    @Test
    @Transactional
    void getAllDiagnosticosByCodigoCIEContainsSomething() throws Exception {
        // Initialize the database
        insertedDiagnostico = diagnosticoRepository.saveAndFlush(diagnostico);

        // Get all the diagnosticoList where codigoCIE contains
        defaultDiagnosticoFiltering("codigoCIE.contains=" + DEFAULT_CODIGO_CIE, "codigoCIE.contains=" + UPDATED_CODIGO_CIE);
    }

    @Test
    @Transactional
    void getAllDiagnosticosByCodigoCIENotContainsSomething() throws Exception {
        // Initialize the database
        insertedDiagnostico = diagnosticoRepository.saveAndFlush(diagnostico);

        // Get all the diagnosticoList where codigoCIE does not contain
        defaultDiagnosticoFiltering("codigoCIE.doesNotContain=" + UPDATED_CODIGO_CIE, "codigoCIE.doesNotContain=" + DEFAULT_CODIGO_CIE);
    }

    @Test
    @Transactional
    void getAllDiagnosticosByConsultaIsEqualToSomething() throws Exception {
        ConsultaMedica consulta;
        if (TestUtil.findAll(em, ConsultaMedica.class).isEmpty()) {
            diagnosticoRepository.saveAndFlush(diagnostico);
            consulta = ConsultaMedicaResourceIT.createEntity();
        } else {
            consulta = TestUtil.findAll(em, ConsultaMedica.class).get(0);
        }
        em.persist(consulta);
        em.flush();
        diagnostico.setConsulta(consulta);
        diagnosticoRepository.saveAndFlush(diagnostico);
        Long consultaId = consulta.getId();
        // Get all the diagnosticoList where consulta equals to consultaId
        defaultDiagnosticoShouldBeFound("consultaId.equals=" + consultaId);

        // Get all the diagnosticoList where consulta equals to (consultaId + 1)
        defaultDiagnosticoShouldNotBeFound("consultaId.equals=" + (consultaId + 1));
    }

    private void defaultDiagnosticoFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultDiagnosticoShouldBeFound(shouldBeFound);
        defaultDiagnosticoShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultDiagnosticoShouldBeFound(String filter) throws Exception {
        restDiagnosticoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(diagnostico.getId().intValue())))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)))
            .andExpect(jsonPath("$.[*].codigoCIE").value(hasItem(DEFAULT_CODIGO_CIE)));

        // Check, that the count call also returns 1
        restDiagnosticoMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultDiagnosticoShouldNotBeFound(String filter) throws Exception {
        restDiagnosticoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restDiagnosticoMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingDiagnostico() throws Exception {
        // Get the diagnostico
        restDiagnosticoMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingDiagnostico() throws Exception {
        // Initialize the database
        insertedDiagnostico = diagnosticoRepository.saveAndFlush(diagnostico);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the diagnostico
        Diagnostico updatedDiagnostico = diagnosticoRepository.findById(diagnostico.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedDiagnostico are not directly saved in db
        em.detach(updatedDiagnostico);
        updatedDiagnostico.descripcion(UPDATED_DESCRIPCION).codigoCIE(UPDATED_CODIGO_CIE);
        DiagnosticoDTO diagnosticoDTO = diagnosticoMapper.toDto(updatedDiagnostico);

        restDiagnosticoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, diagnosticoDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(diagnosticoDTO))
            )
            .andExpect(status().isOk());

        // Validate the Diagnostico in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedDiagnosticoToMatchAllProperties(updatedDiagnostico);
    }

    @Test
    @Transactional
    void putNonExistingDiagnostico() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        diagnostico.setId(longCount.incrementAndGet());

        // Create the Diagnostico
        DiagnosticoDTO diagnosticoDTO = diagnosticoMapper.toDto(diagnostico);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDiagnosticoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, diagnosticoDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(diagnosticoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Diagnostico in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchDiagnostico() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        diagnostico.setId(longCount.incrementAndGet());

        // Create the Diagnostico
        DiagnosticoDTO diagnosticoDTO = diagnosticoMapper.toDto(diagnostico);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDiagnosticoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(diagnosticoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Diagnostico in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamDiagnostico() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        diagnostico.setId(longCount.incrementAndGet());

        // Create the Diagnostico
        DiagnosticoDTO diagnosticoDTO = diagnosticoMapper.toDto(diagnostico);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDiagnosticoMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(diagnosticoDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Diagnostico in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateDiagnosticoWithPatch() throws Exception {
        // Initialize the database
        insertedDiagnostico = diagnosticoRepository.saveAndFlush(diagnostico);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the diagnostico using partial update
        Diagnostico partialUpdatedDiagnostico = new Diagnostico();
        partialUpdatedDiagnostico.setId(diagnostico.getId());

        restDiagnosticoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDiagnostico.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDiagnostico))
            )
            .andExpect(status().isOk());

        // Validate the Diagnostico in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDiagnosticoUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedDiagnostico, diagnostico),
            getPersistedDiagnostico(diagnostico)
        );
    }

    @Test
    @Transactional
    void fullUpdateDiagnosticoWithPatch() throws Exception {
        // Initialize the database
        insertedDiagnostico = diagnosticoRepository.saveAndFlush(diagnostico);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the diagnostico using partial update
        Diagnostico partialUpdatedDiagnostico = new Diagnostico();
        partialUpdatedDiagnostico.setId(diagnostico.getId());

        partialUpdatedDiagnostico.descripcion(UPDATED_DESCRIPCION).codigoCIE(UPDATED_CODIGO_CIE);

        restDiagnosticoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDiagnostico.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDiagnostico))
            )
            .andExpect(status().isOk());

        // Validate the Diagnostico in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDiagnosticoUpdatableFieldsEquals(partialUpdatedDiagnostico, getPersistedDiagnostico(partialUpdatedDiagnostico));
    }

    @Test
    @Transactional
    void patchNonExistingDiagnostico() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        diagnostico.setId(longCount.incrementAndGet());

        // Create the Diagnostico
        DiagnosticoDTO diagnosticoDTO = diagnosticoMapper.toDto(diagnostico);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDiagnosticoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, diagnosticoDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(diagnosticoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Diagnostico in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchDiagnostico() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        diagnostico.setId(longCount.incrementAndGet());

        // Create the Diagnostico
        DiagnosticoDTO diagnosticoDTO = diagnosticoMapper.toDto(diagnostico);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDiagnosticoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(diagnosticoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Diagnostico in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamDiagnostico() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        diagnostico.setId(longCount.incrementAndGet());

        // Create the Diagnostico
        DiagnosticoDTO diagnosticoDTO = diagnosticoMapper.toDto(diagnostico);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDiagnosticoMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(diagnosticoDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Diagnostico in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteDiagnostico() throws Exception {
        // Initialize the database
        insertedDiagnostico = diagnosticoRepository.saveAndFlush(diagnostico);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the diagnostico
        restDiagnosticoMockMvc
            .perform(delete(ENTITY_API_URL_ID, diagnostico.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return diagnosticoRepository.count();
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

    protected Diagnostico getPersistedDiagnostico(Diagnostico diagnostico) {
        return diagnosticoRepository.findById(diagnostico.getId()).orElseThrow();
    }

    protected void assertPersistedDiagnosticoToMatchAllProperties(Diagnostico expectedDiagnostico) {
        assertDiagnosticoAllPropertiesEquals(expectedDiagnostico, getPersistedDiagnostico(expectedDiagnostico));
    }

    protected void assertPersistedDiagnosticoToMatchUpdatableProperties(Diagnostico expectedDiagnostico) {
        assertDiagnosticoAllUpdatablePropertiesEquals(expectedDiagnostico, getPersistedDiagnostico(expectedDiagnostico));
    }
}
