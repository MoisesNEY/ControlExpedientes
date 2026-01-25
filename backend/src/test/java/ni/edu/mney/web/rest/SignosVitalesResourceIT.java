package ni.edu.mney.web.rest;

import static ni.edu.mney.domain.SignosVitalesAsserts.*;
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
import ni.edu.mney.domain.SignosVitales;
import ni.edu.mney.repository.SignosVitalesRepository;
import ni.edu.mney.service.dto.SignosVitalesDTO;
import ni.edu.mney.service.mapper.SignosVitalesMapper;
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
 * Integration tests for the {@link SignosVitalesResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class SignosVitalesResourceIT {

    private static final Double DEFAULT_PESO = 1D;
    private static final Double UPDATED_PESO = 2D;
    private static final Double SMALLER_PESO = 1D - 1D;

    private static final Double DEFAULT_ALTURA = 1D;
    private static final Double UPDATED_ALTURA = 2D;
    private static final Double SMALLER_ALTURA = 1D - 1D;

    private static final String DEFAULT_PRESION_ARTERIAL = "AAAAAAAAAA";
    private static final String UPDATED_PRESION_ARTERIAL = "BBBBBBBBBB";

    private static final Double DEFAULT_TEMPERATURA = 1D;
    private static final Double UPDATED_TEMPERATURA = 2D;
    private static final Double SMALLER_TEMPERATURA = 1D - 1D;

    private static final Integer DEFAULT_FRECUENCIA_CARDIACA = 1;
    private static final Integer UPDATED_FRECUENCIA_CARDIACA = 2;
    private static final Integer SMALLER_FRECUENCIA_CARDIACA = 1 - 1;

    private static final String ENTITY_API_URL = "/api/signos-vitales";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private SignosVitalesRepository signosVitalesRepository;

    @Autowired
    private SignosVitalesMapper signosVitalesMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restSignosVitalesMockMvc;

    private SignosVitales signosVitales;

    private SignosVitales insertedSignosVitales;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SignosVitales createEntity() {
        return new SignosVitales()
            .peso(DEFAULT_PESO)
            .altura(DEFAULT_ALTURA)
            .presionArterial(DEFAULT_PRESION_ARTERIAL)
            .temperatura(DEFAULT_TEMPERATURA)
            .frecuenciaCardiaca(DEFAULT_FRECUENCIA_CARDIACA);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SignosVitales createUpdatedEntity() {
        return new SignosVitales()
            .peso(UPDATED_PESO)
            .altura(UPDATED_ALTURA)
            .presionArterial(UPDATED_PRESION_ARTERIAL)
            .temperatura(UPDATED_TEMPERATURA)
            .frecuenciaCardiaca(UPDATED_FRECUENCIA_CARDIACA);
    }

    @BeforeEach
    void initTest() {
        signosVitales = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedSignosVitales != null) {
            signosVitalesRepository.delete(insertedSignosVitales);
            insertedSignosVitales = null;
        }
    }

    @Test
    @Transactional
    void createSignosVitales() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the SignosVitales
        SignosVitalesDTO signosVitalesDTO = signosVitalesMapper.toDto(signosVitales);
        var returnedSignosVitalesDTO = om.readValue(
            restSignosVitalesMockMvc
                .perform(
                    post(ENTITY_API_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(signosVitalesDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            SignosVitalesDTO.class
        );

        // Validate the SignosVitales in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedSignosVitales = signosVitalesMapper.toEntity(returnedSignosVitalesDTO);
        assertSignosVitalesUpdatableFieldsEquals(returnedSignosVitales, getPersistedSignosVitales(returnedSignosVitales));

        insertedSignosVitales = returnedSignosVitales;
    }

    @Test
    @Transactional
    void createSignosVitalesWithExistingId() throws Exception {
        // Create the SignosVitales with an existing ID
        signosVitales.setId(1L);
        SignosVitalesDTO signosVitalesDTO = signosVitalesMapper.toDto(signosVitales);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restSignosVitalesMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(signosVitalesDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SignosVitales in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllSignosVitales() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList
        restSignosVitalesMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(signosVitales.getId().intValue())))
            .andExpect(jsonPath("$.[*].peso").value(hasItem(DEFAULT_PESO)))
            .andExpect(jsonPath("$.[*].altura").value(hasItem(DEFAULT_ALTURA)))
            .andExpect(jsonPath("$.[*].presionArterial").value(hasItem(DEFAULT_PRESION_ARTERIAL)))
            .andExpect(jsonPath("$.[*].temperatura").value(hasItem(DEFAULT_TEMPERATURA)))
            .andExpect(jsonPath("$.[*].frecuenciaCardiaca").value(hasItem(DEFAULT_FRECUENCIA_CARDIACA)));
    }

    @Test
    @Transactional
    void getSignosVitales() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get the signosVitales
        restSignosVitalesMockMvc
            .perform(get(ENTITY_API_URL_ID, signosVitales.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(signosVitales.getId().intValue()))
            .andExpect(jsonPath("$.peso").value(DEFAULT_PESO))
            .andExpect(jsonPath("$.altura").value(DEFAULT_ALTURA))
            .andExpect(jsonPath("$.presionArterial").value(DEFAULT_PRESION_ARTERIAL))
            .andExpect(jsonPath("$.temperatura").value(DEFAULT_TEMPERATURA))
            .andExpect(jsonPath("$.frecuenciaCardiaca").value(DEFAULT_FRECUENCIA_CARDIACA));
    }

    @Test
    @Transactional
    void getSignosVitalesByIdFiltering() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        Long id = signosVitales.getId();

        defaultSignosVitalesFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultSignosVitalesFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultSignosVitalesFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllSignosVitalesByPesoIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where peso equals to
        defaultSignosVitalesFiltering("peso.equals=" + DEFAULT_PESO, "peso.equals=" + UPDATED_PESO);
    }

    @Test
    @Transactional
    void getAllSignosVitalesByPesoIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where peso in
        defaultSignosVitalesFiltering("peso.in=" + DEFAULT_PESO + "," + UPDATED_PESO, "peso.in=" + UPDATED_PESO);
    }

    @Test
    @Transactional
    void getAllSignosVitalesByPesoIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where peso is not null
        defaultSignosVitalesFiltering("peso.specified=true", "peso.specified=false");
    }

    @Test
    @Transactional
    void getAllSignosVitalesByPesoIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where peso is greater than or equal to
        defaultSignosVitalesFiltering("peso.greaterThanOrEqual=" + DEFAULT_PESO, "peso.greaterThanOrEqual=" + UPDATED_PESO);
    }

    @Test
    @Transactional
    void getAllSignosVitalesByPesoIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where peso is less than or equal to
        defaultSignosVitalesFiltering("peso.lessThanOrEqual=" + DEFAULT_PESO, "peso.lessThanOrEqual=" + SMALLER_PESO);
    }

    @Test
    @Transactional
    void getAllSignosVitalesByPesoIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where peso is less than
        defaultSignosVitalesFiltering("peso.lessThan=" + UPDATED_PESO, "peso.lessThan=" + DEFAULT_PESO);
    }

    @Test
    @Transactional
    void getAllSignosVitalesByPesoIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where peso is greater than
        defaultSignosVitalesFiltering("peso.greaterThan=" + SMALLER_PESO, "peso.greaterThan=" + DEFAULT_PESO);
    }

    @Test
    @Transactional
    void getAllSignosVitalesByAlturaIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where altura equals to
        defaultSignosVitalesFiltering("altura.equals=" + DEFAULT_ALTURA, "altura.equals=" + UPDATED_ALTURA);
    }

    @Test
    @Transactional
    void getAllSignosVitalesByAlturaIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where altura in
        defaultSignosVitalesFiltering("altura.in=" + DEFAULT_ALTURA + "," + UPDATED_ALTURA, "altura.in=" + UPDATED_ALTURA);
    }

    @Test
    @Transactional
    void getAllSignosVitalesByAlturaIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where altura is not null
        defaultSignosVitalesFiltering("altura.specified=true", "altura.specified=false");
    }

    @Test
    @Transactional
    void getAllSignosVitalesByAlturaIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where altura is greater than or equal to
        defaultSignosVitalesFiltering("altura.greaterThanOrEqual=" + DEFAULT_ALTURA, "altura.greaterThanOrEqual=" + UPDATED_ALTURA);
    }

    @Test
    @Transactional
    void getAllSignosVitalesByAlturaIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where altura is less than or equal to
        defaultSignosVitalesFiltering("altura.lessThanOrEqual=" + DEFAULT_ALTURA, "altura.lessThanOrEqual=" + SMALLER_ALTURA);
    }

    @Test
    @Transactional
    void getAllSignosVitalesByAlturaIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where altura is less than
        defaultSignosVitalesFiltering("altura.lessThan=" + UPDATED_ALTURA, "altura.lessThan=" + DEFAULT_ALTURA);
    }

    @Test
    @Transactional
    void getAllSignosVitalesByAlturaIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where altura is greater than
        defaultSignosVitalesFiltering("altura.greaterThan=" + SMALLER_ALTURA, "altura.greaterThan=" + DEFAULT_ALTURA);
    }

    @Test
    @Transactional
    void getAllSignosVitalesByPresionArterialIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where presionArterial equals to
        defaultSignosVitalesFiltering(
            "presionArterial.equals=" + DEFAULT_PRESION_ARTERIAL,
            "presionArterial.equals=" + UPDATED_PRESION_ARTERIAL
        );
    }

    @Test
    @Transactional
    void getAllSignosVitalesByPresionArterialIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where presionArterial in
        defaultSignosVitalesFiltering(
            "presionArterial.in=" + DEFAULT_PRESION_ARTERIAL + "," + UPDATED_PRESION_ARTERIAL,
            "presionArterial.in=" + UPDATED_PRESION_ARTERIAL
        );
    }

    @Test
    @Transactional
    void getAllSignosVitalesByPresionArterialIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where presionArterial is not null
        defaultSignosVitalesFiltering("presionArterial.specified=true", "presionArterial.specified=false");
    }

    @Test
    @Transactional
    void getAllSignosVitalesByPresionArterialContainsSomething() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where presionArterial contains
        defaultSignosVitalesFiltering(
            "presionArterial.contains=" + DEFAULT_PRESION_ARTERIAL,
            "presionArterial.contains=" + UPDATED_PRESION_ARTERIAL
        );
    }

    @Test
    @Transactional
    void getAllSignosVitalesByPresionArterialNotContainsSomething() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where presionArterial does not contain
        defaultSignosVitalesFiltering(
            "presionArterial.doesNotContain=" + UPDATED_PRESION_ARTERIAL,
            "presionArterial.doesNotContain=" + DEFAULT_PRESION_ARTERIAL
        );
    }

    @Test
    @Transactional
    void getAllSignosVitalesByTemperaturaIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where temperatura equals to
        defaultSignosVitalesFiltering("temperatura.equals=" + DEFAULT_TEMPERATURA, "temperatura.equals=" + UPDATED_TEMPERATURA);
    }

    @Test
    @Transactional
    void getAllSignosVitalesByTemperaturaIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where temperatura in
        defaultSignosVitalesFiltering(
            "temperatura.in=" + DEFAULT_TEMPERATURA + "," + UPDATED_TEMPERATURA,
            "temperatura.in=" + UPDATED_TEMPERATURA
        );
    }

    @Test
    @Transactional
    void getAllSignosVitalesByTemperaturaIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where temperatura is not null
        defaultSignosVitalesFiltering("temperatura.specified=true", "temperatura.specified=false");
    }

    @Test
    @Transactional
    void getAllSignosVitalesByTemperaturaIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where temperatura is greater than or equal to
        defaultSignosVitalesFiltering(
            "temperatura.greaterThanOrEqual=" + DEFAULT_TEMPERATURA,
            "temperatura.greaterThanOrEqual=" + UPDATED_TEMPERATURA
        );
    }

    @Test
    @Transactional
    void getAllSignosVitalesByTemperaturaIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where temperatura is less than or equal to
        defaultSignosVitalesFiltering(
            "temperatura.lessThanOrEqual=" + DEFAULT_TEMPERATURA,
            "temperatura.lessThanOrEqual=" + SMALLER_TEMPERATURA
        );
    }

    @Test
    @Transactional
    void getAllSignosVitalesByTemperaturaIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where temperatura is less than
        defaultSignosVitalesFiltering("temperatura.lessThan=" + UPDATED_TEMPERATURA, "temperatura.lessThan=" + DEFAULT_TEMPERATURA);
    }

    @Test
    @Transactional
    void getAllSignosVitalesByTemperaturaIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where temperatura is greater than
        defaultSignosVitalesFiltering("temperatura.greaterThan=" + SMALLER_TEMPERATURA, "temperatura.greaterThan=" + DEFAULT_TEMPERATURA);
    }

    @Test
    @Transactional
    void getAllSignosVitalesByFrecuenciaCardiacaIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where frecuenciaCardiaca equals to
        defaultSignosVitalesFiltering(
            "frecuenciaCardiaca.equals=" + DEFAULT_FRECUENCIA_CARDIACA,
            "frecuenciaCardiaca.equals=" + UPDATED_FRECUENCIA_CARDIACA
        );
    }

    @Test
    @Transactional
    void getAllSignosVitalesByFrecuenciaCardiacaIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where frecuenciaCardiaca in
        defaultSignosVitalesFiltering(
            "frecuenciaCardiaca.in=" + DEFAULT_FRECUENCIA_CARDIACA + "," + UPDATED_FRECUENCIA_CARDIACA,
            "frecuenciaCardiaca.in=" + UPDATED_FRECUENCIA_CARDIACA
        );
    }

    @Test
    @Transactional
    void getAllSignosVitalesByFrecuenciaCardiacaIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where frecuenciaCardiaca is not null
        defaultSignosVitalesFiltering("frecuenciaCardiaca.specified=true", "frecuenciaCardiaca.specified=false");
    }

    @Test
    @Transactional
    void getAllSignosVitalesByFrecuenciaCardiacaIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where frecuenciaCardiaca is greater than or equal to
        defaultSignosVitalesFiltering(
            "frecuenciaCardiaca.greaterThanOrEqual=" + DEFAULT_FRECUENCIA_CARDIACA,
            "frecuenciaCardiaca.greaterThanOrEqual=" + UPDATED_FRECUENCIA_CARDIACA
        );
    }

    @Test
    @Transactional
    void getAllSignosVitalesByFrecuenciaCardiacaIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where frecuenciaCardiaca is less than or equal to
        defaultSignosVitalesFiltering(
            "frecuenciaCardiaca.lessThanOrEqual=" + DEFAULT_FRECUENCIA_CARDIACA,
            "frecuenciaCardiaca.lessThanOrEqual=" + SMALLER_FRECUENCIA_CARDIACA
        );
    }

    @Test
    @Transactional
    void getAllSignosVitalesByFrecuenciaCardiacaIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where frecuenciaCardiaca is less than
        defaultSignosVitalesFiltering(
            "frecuenciaCardiaca.lessThan=" + UPDATED_FRECUENCIA_CARDIACA,
            "frecuenciaCardiaca.lessThan=" + DEFAULT_FRECUENCIA_CARDIACA
        );
    }

    @Test
    @Transactional
    void getAllSignosVitalesByFrecuenciaCardiacaIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        // Get all the signosVitalesList where frecuenciaCardiaca is greater than
        defaultSignosVitalesFiltering(
            "frecuenciaCardiaca.greaterThan=" + SMALLER_FRECUENCIA_CARDIACA,
            "frecuenciaCardiaca.greaterThan=" + DEFAULT_FRECUENCIA_CARDIACA
        );
    }

    @Test
    @Transactional
    void getAllSignosVitalesByConsultaIsEqualToSomething() throws Exception {
        ConsultaMedica consulta;
        if (TestUtil.findAll(em, ConsultaMedica.class).isEmpty()) {
            signosVitalesRepository.saveAndFlush(signosVitales);
            consulta = ConsultaMedicaResourceIT.createEntity();
        } else {
            consulta = TestUtil.findAll(em, ConsultaMedica.class).get(0);
        }
        em.persist(consulta);
        em.flush();
        signosVitales.setConsulta(consulta);
        signosVitalesRepository.saveAndFlush(signosVitales);
        Long consultaId = consulta.getId();
        // Get all the signosVitalesList where consulta equals to consultaId
        defaultSignosVitalesShouldBeFound("consultaId.equals=" + consultaId);

        // Get all the signosVitalesList where consulta equals to (consultaId + 1)
        defaultSignosVitalesShouldNotBeFound("consultaId.equals=" + (consultaId + 1));
    }

    private void defaultSignosVitalesFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultSignosVitalesShouldBeFound(shouldBeFound);
        defaultSignosVitalesShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultSignosVitalesShouldBeFound(String filter) throws Exception {
        restSignosVitalesMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(signosVitales.getId().intValue())))
            .andExpect(jsonPath("$.[*].peso").value(hasItem(DEFAULT_PESO)))
            .andExpect(jsonPath("$.[*].altura").value(hasItem(DEFAULT_ALTURA)))
            .andExpect(jsonPath("$.[*].presionArterial").value(hasItem(DEFAULT_PRESION_ARTERIAL)))
            .andExpect(jsonPath("$.[*].temperatura").value(hasItem(DEFAULT_TEMPERATURA)))
            .andExpect(jsonPath("$.[*].frecuenciaCardiaca").value(hasItem(DEFAULT_FRECUENCIA_CARDIACA)));

        // Check, that the count call also returns 1
        restSignosVitalesMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultSignosVitalesShouldNotBeFound(String filter) throws Exception {
        restSignosVitalesMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restSignosVitalesMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingSignosVitales() throws Exception {
        // Get the signosVitales
        restSignosVitalesMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingSignosVitales() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the signosVitales
        SignosVitales updatedSignosVitales = signosVitalesRepository.findById(signosVitales.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedSignosVitales are not directly saved in db
        em.detach(updatedSignosVitales);
        updatedSignosVitales
            .peso(UPDATED_PESO)
            .altura(UPDATED_ALTURA)
            .presionArterial(UPDATED_PRESION_ARTERIAL)
            .temperatura(UPDATED_TEMPERATURA)
            .frecuenciaCardiaca(UPDATED_FRECUENCIA_CARDIACA);
        SignosVitalesDTO signosVitalesDTO = signosVitalesMapper.toDto(updatedSignosVitales);

        restSignosVitalesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, signosVitalesDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(signosVitalesDTO))
            )
            .andExpect(status().isOk());

        // Validate the SignosVitales in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedSignosVitalesToMatchAllProperties(updatedSignosVitales);
    }

    @Test
    @Transactional
    void putNonExistingSignosVitales() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        signosVitales.setId(longCount.incrementAndGet());

        // Create the SignosVitales
        SignosVitalesDTO signosVitalesDTO = signosVitalesMapper.toDto(signosVitales);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSignosVitalesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, signosVitalesDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(signosVitalesDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SignosVitales in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchSignosVitales() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        signosVitales.setId(longCount.incrementAndGet());

        // Create the SignosVitales
        SignosVitalesDTO signosVitalesDTO = signosVitalesMapper.toDto(signosVitales);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSignosVitalesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(signosVitalesDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SignosVitales in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamSignosVitales() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        signosVitales.setId(longCount.incrementAndGet());

        // Create the SignosVitales
        SignosVitalesDTO signosVitalesDTO = signosVitalesMapper.toDto(signosVitales);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSignosVitalesMockMvc
            .perform(
                put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(signosVitalesDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the SignosVitales in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateSignosVitalesWithPatch() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the signosVitales using partial update
        SignosVitales partialUpdatedSignosVitales = new SignosVitales();
        partialUpdatedSignosVitales.setId(signosVitales.getId());

        partialUpdatedSignosVitales
            .peso(UPDATED_PESO)
            .altura(UPDATED_ALTURA)
            .presionArterial(UPDATED_PRESION_ARTERIAL)
            .temperatura(UPDATED_TEMPERATURA);

        restSignosVitalesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSignosVitales.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSignosVitales))
            )
            .andExpect(status().isOk());

        // Validate the SignosVitales in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSignosVitalesUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedSignosVitales, signosVitales),
            getPersistedSignosVitales(signosVitales)
        );
    }

    @Test
    @Transactional
    void fullUpdateSignosVitalesWithPatch() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the signosVitales using partial update
        SignosVitales partialUpdatedSignosVitales = new SignosVitales();
        partialUpdatedSignosVitales.setId(signosVitales.getId());

        partialUpdatedSignosVitales
            .peso(UPDATED_PESO)
            .altura(UPDATED_ALTURA)
            .presionArterial(UPDATED_PRESION_ARTERIAL)
            .temperatura(UPDATED_TEMPERATURA)
            .frecuenciaCardiaca(UPDATED_FRECUENCIA_CARDIACA);

        restSignosVitalesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSignosVitales.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSignosVitales))
            )
            .andExpect(status().isOk());

        // Validate the SignosVitales in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSignosVitalesUpdatableFieldsEquals(partialUpdatedSignosVitales, getPersistedSignosVitales(partialUpdatedSignosVitales));
    }

    @Test
    @Transactional
    void patchNonExistingSignosVitales() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        signosVitales.setId(longCount.incrementAndGet());

        // Create the SignosVitales
        SignosVitalesDTO signosVitalesDTO = signosVitalesMapper.toDto(signosVitales);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSignosVitalesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, signosVitalesDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(signosVitalesDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SignosVitales in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchSignosVitales() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        signosVitales.setId(longCount.incrementAndGet());

        // Create the SignosVitales
        SignosVitalesDTO signosVitalesDTO = signosVitalesMapper.toDto(signosVitales);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSignosVitalesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(signosVitalesDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SignosVitales in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamSignosVitales() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        signosVitales.setId(longCount.incrementAndGet());

        // Create the SignosVitales
        SignosVitalesDTO signosVitalesDTO = signosVitalesMapper.toDto(signosVitales);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSignosVitalesMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(signosVitalesDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the SignosVitales in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteSignosVitales() throws Exception {
        // Initialize the database
        insertedSignosVitales = signosVitalesRepository.saveAndFlush(signosVitales);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the signosVitales
        restSignosVitalesMockMvc
            .perform(delete(ENTITY_API_URL_ID, signosVitales.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return signosVitalesRepository.count();
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

    protected SignosVitales getPersistedSignosVitales(SignosVitales signosVitales) {
        return signosVitalesRepository.findById(signosVitales.getId()).orElseThrow();
    }

    protected void assertPersistedSignosVitalesToMatchAllProperties(SignosVitales expectedSignosVitales) {
        assertSignosVitalesAllPropertiesEquals(expectedSignosVitales, getPersistedSignosVitales(expectedSignosVitales));
    }

    protected void assertPersistedSignosVitalesToMatchUpdatableProperties(SignosVitales expectedSignosVitales) {
        assertSignosVitalesAllUpdatablePropertiesEquals(expectedSignosVitales, getPersistedSignosVitales(expectedSignosVitales));
    }
}
