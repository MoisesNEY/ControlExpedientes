package ni.edu.mney.web.rest;

import static ni.edu.mney.domain.MedicamentoAsserts.*;
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
import ni.edu.mney.domain.Medicamento;
import ni.edu.mney.repository.MedicamentoRepository;
import ni.edu.mney.service.dto.MedicamentoDTO;
import ni.edu.mney.service.mapper.MedicamentoMapper;
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
 * Integration tests for the {@link MedicamentoResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class MedicamentoResourceIT {

    private static final String DEFAULT_NOMBRE = "AAAAAAAAAA";
    private static final String UPDATED_NOMBRE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPCION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPCION = "BBBBBBBBBB";

    private static final Integer DEFAULT_STOCK = 1;
    private static final Integer UPDATED_STOCK = 2;
    private static final Integer SMALLER_STOCK = 1 - 1;

    private static final String ENTITY_API_URL = "/api/medicamentos";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Autowired
    private MedicamentoMapper medicamentoMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restMedicamentoMockMvc;

    private Medicamento medicamento;

    private Medicamento insertedMedicamento;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Medicamento createEntity() {
        return new Medicamento().nombre(DEFAULT_NOMBRE).descripcion(DEFAULT_DESCRIPCION).stock(DEFAULT_STOCK);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Medicamento createUpdatedEntity() {
        return new Medicamento().nombre(UPDATED_NOMBRE).descripcion(UPDATED_DESCRIPCION).stock(UPDATED_STOCK);
    }

    @BeforeEach
    void initTest() {
        medicamento = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedMedicamento != null) {
            medicamentoRepository.delete(insertedMedicamento);
            insertedMedicamento = null;
        }
    }

    @Test
    @Transactional
    void createMedicamento() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Medicamento
        MedicamentoDTO medicamentoDTO = medicamentoMapper.toDto(medicamento);
        var returnedMedicamentoDTO = om.readValue(
            restMedicamentoMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(medicamentoDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            MedicamentoDTO.class
        );

        // Validate the Medicamento in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedMedicamento = medicamentoMapper.toEntity(returnedMedicamentoDTO);
        assertMedicamentoUpdatableFieldsEquals(returnedMedicamento, getPersistedMedicamento(returnedMedicamento));

        insertedMedicamento = returnedMedicamento;
    }

    @Test
    @Transactional
    void createMedicamentoWithExistingId() throws Exception {
        // Create the Medicamento with an existing ID
        medicamento.setId(1L);
        MedicamentoDTO medicamentoDTO = medicamentoMapper.toDto(medicamento);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restMedicamentoMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(medicamentoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Medicamento in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNombreIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        medicamento.setNombre(null);

        // Create the Medicamento, which fails.
        MedicamentoDTO medicamentoDTO = medicamentoMapper.toDto(medicamento);

        restMedicamentoMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(medicamentoDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStockIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        medicamento.setStock(null);

        // Create the Medicamento, which fails.
        MedicamentoDTO medicamentoDTO = medicamentoMapper.toDto(medicamento);

        restMedicamentoMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(medicamentoDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllMedicamentos() throws Exception {
        // Initialize the database
        insertedMedicamento = medicamentoRepository.saveAndFlush(medicamento);

        // Get all the medicamentoList
        restMedicamentoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(medicamento.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)))
            .andExpect(jsonPath("$.[*].stock").value(hasItem(DEFAULT_STOCK)));
    }

    @Test
    @Transactional
    void getMedicamento() throws Exception {
        // Initialize the database
        insertedMedicamento = medicamentoRepository.saveAndFlush(medicamento);

        // Get the medicamento
        restMedicamentoMockMvc
            .perform(get(ENTITY_API_URL_ID, medicamento.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(medicamento.getId().intValue()))
            .andExpect(jsonPath("$.nombre").value(DEFAULT_NOMBRE))
            .andExpect(jsonPath("$.descripcion").value(DEFAULT_DESCRIPCION))
            .andExpect(jsonPath("$.stock").value(DEFAULT_STOCK));
    }

    @Test
    @Transactional
    void getMedicamentosByIdFiltering() throws Exception {
        // Initialize the database
        insertedMedicamento = medicamentoRepository.saveAndFlush(medicamento);

        Long id = medicamento.getId();

        defaultMedicamentoFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultMedicamentoFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultMedicamentoFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllMedicamentosByNombreIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicamento = medicamentoRepository.saveAndFlush(medicamento);

        // Get all the medicamentoList where nombre equals to
        defaultMedicamentoFiltering("nombre.equals=" + DEFAULT_NOMBRE, "nombre.equals=" + UPDATED_NOMBRE);
    }

    @Test
    @Transactional
    void getAllMedicamentosByNombreIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMedicamento = medicamentoRepository.saveAndFlush(medicamento);

        // Get all the medicamentoList where nombre in
        defaultMedicamentoFiltering("nombre.in=" + DEFAULT_NOMBRE + "," + UPDATED_NOMBRE, "nombre.in=" + UPDATED_NOMBRE);
    }

    @Test
    @Transactional
    void getAllMedicamentosByNombreIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMedicamento = medicamentoRepository.saveAndFlush(medicamento);

        // Get all the medicamentoList where nombre is not null
        defaultMedicamentoFiltering("nombre.specified=true", "nombre.specified=false");
    }

    @Test
    @Transactional
    void getAllMedicamentosByNombreContainsSomething() throws Exception {
        // Initialize the database
        insertedMedicamento = medicamentoRepository.saveAndFlush(medicamento);

        // Get all the medicamentoList where nombre contains
        defaultMedicamentoFiltering("nombre.contains=" + DEFAULT_NOMBRE, "nombre.contains=" + UPDATED_NOMBRE);
    }

    @Test
    @Transactional
    void getAllMedicamentosByNombreNotContainsSomething() throws Exception {
        // Initialize the database
        insertedMedicamento = medicamentoRepository.saveAndFlush(medicamento);

        // Get all the medicamentoList where nombre does not contain
        defaultMedicamentoFiltering("nombre.doesNotContain=" + UPDATED_NOMBRE, "nombre.doesNotContain=" + DEFAULT_NOMBRE);
    }

    @Test
    @Transactional
    void getAllMedicamentosByDescripcionIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicamento = medicamentoRepository.saveAndFlush(medicamento);

        // Get all the medicamentoList where descripcion equals to
        defaultMedicamentoFiltering("descripcion.equals=" + DEFAULT_DESCRIPCION, "descripcion.equals=" + UPDATED_DESCRIPCION);
    }

    @Test
    @Transactional
    void getAllMedicamentosByDescripcionIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMedicamento = medicamentoRepository.saveAndFlush(medicamento);

        // Get all the medicamentoList where descripcion in
        defaultMedicamentoFiltering(
            "descripcion.in=" + DEFAULT_DESCRIPCION + "," + UPDATED_DESCRIPCION,
            "descripcion.in=" + UPDATED_DESCRIPCION
        );
    }

    @Test
    @Transactional
    void getAllMedicamentosByDescripcionIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMedicamento = medicamentoRepository.saveAndFlush(medicamento);

        // Get all the medicamentoList where descripcion is not null
        defaultMedicamentoFiltering("descripcion.specified=true", "descripcion.specified=false");
    }

    @Test
    @Transactional
    void getAllMedicamentosByDescripcionContainsSomething() throws Exception {
        // Initialize the database
        insertedMedicamento = medicamentoRepository.saveAndFlush(medicamento);

        // Get all the medicamentoList where descripcion contains
        defaultMedicamentoFiltering("descripcion.contains=" + DEFAULT_DESCRIPCION, "descripcion.contains=" + UPDATED_DESCRIPCION);
    }

    @Test
    @Transactional
    void getAllMedicamentosByDescripcionNotContainsSomething() throws Exception {
        // Initialize the database
        insertedMedicamento = medicamentoRepository.saveAndFlush(medicamento);

        // Get all the medicamentoList where descripcion does not contain
        defaultMedicamentoFiltering(
            "descripcion.doesNotContain=" + UPDATED_DESCRIPCION,
            "descripcion.doesNotContain=" + DEFAULT_DESCRIPCION
        );
    }

    @Test
    @Transactional
    void getAllMedicamentosByStockIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicamento = medicamentoRepository.saveAndFlush(medicamento);

        // Get all the medicamentoList where stock equals to
        defaultMedicamentoFiltering("stock.equals=" + DEFAULT_STOCK, "stock.equals=" + UPDATED_STOCK);
    }

    @Test
    @Transactional
    void getAllMedicamentosByStockIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMedicamento = medicamentoRepository.saveAndFlush(medicamento);

        // Get all the medicamentoList where stock in
        defaultMedicamentoFiltering("stock.in=" + DEFAULT_STOCK + "," + UPDATED_STOCK, "stock.in=" + UPDATED_STOCK);
    }

    @Test
    @Transactional
    void getAllMedicamentosByStockIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMedicamento = medicamentoRepository.saveAndFlush(medicamento);

        // Get all the medicamentoList where stock is not null
        defaultMedicamentoFiltering("stock.specified=true", "stock.specified=false");
    }

    @Test
    @Transactional
    void getAllMedicamentosByStockIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicamento = medicamentoRepository.saveAndFlush(medicamento);

        // Get all the medicamentoList where stock is greater than or equal to
        defaultMedicamentoFiltering("stock.greaterThanOrEqual=" + DEFAULT_STOCK, "stock.greaterThanOrEqual=" + UPDATED_STOCK);
    }

    @Test
    @Transactional
    void getAllMedicamentosByStockIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMedicamento = medicamentoRepository.saveAndFlush(medicamento);

        // Get all the medicamentoList where stock is less than or equal to
        defaultMedicamentoFiltering("stock.lessThanOrEqual=" + DEFAULT_STOCK, "stock.lessThanOrEqual=" + SMALLER_STOCK);
    }

    @Test
    @Transactional
    void getAllMedicamentosByStockIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedMedicamento = medicamentoRepository.saveAndFlush(medicamento);

        // Get all the medicamentoList where stock is less than
        defaultMedicamentoFiltering("stock.lessThan=" + UPDATED_STOCK, "stock.lessThan=" + DEFAULT_STOCK);
    }

    @Test
    @Transactional
    void getAllMedicamentosByStockIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedMedicamento = medicamentoRepository.saveAndFlush(medicamento);

        // Get all the medicamentoList where stock is greater than
        defaultMedicamentoFiltering("stock.greaterThan=" + SMALLER_STOCK, "stock.greaterThan=" + DEFAULT_STOCK);
    }

    private void defaultMedicamentoFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultMedicamentoShouldBeFound(shouldBeFound);
        defaultMedicamentoShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultMedicamentoShouldBeFound(String filter) throws Exception {
        restMedicamentoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(medicamento.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)))
            .andExpect(jsonPath("$.[*].stock").value(hasItem(DEFAULT_STOCK)));

        // Check, that the count call also returns 1
        restMedicamentoMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultMedicamentoShouldNotBeFound(String filter) throws Exception {
        restMedicamentoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restMedicamentoMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingMedicamento() throws Exception {
        // Get the medicamento
        restMedicamentoMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingMedicamento() throws Exception {
        // Initialize the database
        insertedMedicamento = medicamentoRepository.saveAndFlush(medicamento);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the medicamento
        Medicamento updatedMedicamento = medicamentoRepository.findById(medicamento.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedMedicamento are not directly saved in db
        em.detach(updatedMedicamento);
        updatedMedicamento.nombre(UPDATED_NOMBRE).descripcion(UPDATED_DESCRIPCION).stock(UPDATED_STOCK);
        MedicamentoDTO medicamentoDTO = medicamentoMapper.toDto(updatedMedicamento);

        restMedicamentoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, medicamentoDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(medicamentoDTO))
            )
            .andExpect(status().isOk());

        // Validate the Medicamento in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedMedicamentoToMatchAllProperties(updatedMedicamento);
    }

    @Test
    @Transactional
    void putNonExistingMedicamento() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        medicamento.setId(longCount.incrementAndGet());

        // Create the Medicamento
        MedicamentoDTO medicamentoDTO = medicamentoMapper.toDto(medicamento);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMedicamentoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, medicamentoDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(medicamentoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Medicamento in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchMedicamento() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        medicamento.setId(longCount.incrementAndGet());

        // Create the Medicamento
        MedicamentoDTO medicamentoDTO = medicamentoMapper.toDto(medicamento);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMedicamentoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(medicamentoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Medicamento in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamMedicamento() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        medicamento.setId(longCount.incrementAndGet());

        // Create the Medicamento
        MedicamentoDTO medicamentoDTO = medicamentoMapper.toDto(medicamento);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMedicamentoMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(medicamentoDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Medicamento in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateMedicamentoWithPatch() throws Exception {
        // Initialize the database
        insertedMedicamento = medicamentoRepository.saveAndFlush(medicamento);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the medicamento using partial update
        Medicamento partialUpdatedMedicamento = new Medicamento();
        partialUpdatedMedicamento.setId(medicamento.getId());

        partialUpdatedMedicamento.nombre(UPDATED_NOMBRE).stock(UPDATED_STOCK);

        restMedicamentoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMedicamento.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMedicamento))
            )
            .andExpect(status().isOk());

        // Validate the Medicamento in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMedicamentoUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedMedicamento, medicamento),
            getPersistedMedicamento(medicamento)
        );
    }

    @Test
    @Transactional
    void fullUpdateMedicamentoWithPatch() throws Exception {
        // Initialize the database
        insertedMedicamento = medicamentoRepository.saveAndFlush(medicamento);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the medicamento using partial update
        Medicamento partialUpdatedMedicamento = new Medicamento();
        partialUpdatedMedicamento.setId(medicamento.getId());

        partialUpdatedMedicamento.nombre(UPDATED_NOMBRE).descripcion(UPDATED_DESCRIPCION).stock(UPDATED_STOCK);

        restMedicamentoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMedicamento.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMedicamento))
            )
            .andExpect(status().isOk());

        // Validate the Medicamento in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMedicamentoUpdatableFieldsEquals(partialUpdatedMedicamento, getPersistedMedicamento(partialUpdatedMedicamento));
    }

    @Test
    @Transactional
    void patchNonExistingMedicamento() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        medicamento.setId(longCount.incrementAndGet());

        // Create the Medicamento
        MedicamentoDTO medicamentoDTO = medicamentoMapper.toDto(medicamento);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMedicamentoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, medicamentoDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(medicamentoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Medicamento in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchMedicamento() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        medicamento.setId(longCount.incrementAndGet());

        // Create the Medicamento
        MedicamentoDTO medicamentoDTO = medicamentoMapper.toDto(medicamento);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMedicamentoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(medicamentoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Medicamento in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamMedicamento() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        medicamento.setId(longCount.incrementAndGet());

        // Create the Medicamento
        MedicamentoDTO medicamentoDTO = medicamentoMapper.toDto(medicamento);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMedicamentoMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(medicamentoDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Medicamento in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteMedicamento() throws Exception {
        // Initialize the database
        insertedMedicamento = medicamentoRepository.saveAndFlush(medicamento);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the medicamento
        restMedicamentoMockMvc
            .perform(delete(ENTITY_API_URL_ID, medicamento.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return medicamentoRepository.count();
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

    protected Medicamento getPersistedMedicamento(Medicamento medicamento) {
        return medicamentoRepository.findById(medicamento.getId()).orElseThrow();
    }

    protected void assertPersistedMedicamentoToMatchAllProperties(Medicamento expectedMedicamento) {
        assertMedicamentoAllPropertiesEquals(expectedMedicamento, getPersistedMedicamento(expectedMedicamento));
    }

    protected void assertPersistedMedicamentoToMatchUpdatableProperties(Medicamento expectedMedicamento) {
        assertMedicamentoAllUpdatablePropertiesEquals(expectedMedicamento, getPersistedMedicamento(expectedMedicamento));
    }
}
