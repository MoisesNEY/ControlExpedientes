package ni.edu.mney.config;

import java.time.Duration;
import org.ehcache.config.builders.*;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;
import tech.jhipster.config.JHipsterProperties;

@Configuration
@EnableCaching
public class CacheConfiguration {

    private final javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration;

    public CacheConfiguration(JHipsterProperties jHipsterProperties) {
        JHipsterProperties.Cache.Ehcache ehcache = jHipsterProperties.getCache().getEhcache();

        jcacheConfiguration = Eh107Configuration.fromEhcacheCacheConfiguration(
                CacheConfigurationBuilder.newCacheConfigurationBuilder(
                        Object.class,
                        Object.class,
                        ResourcePoolsBuilder.heap(ehcache.getMaxEntries()))
                        .withExpiry(ExpiryPolicyBuilder
                                .timeToLiveExpiration(Duration.ofSeconds(ehcache.getTimeToLiveSeconds())))
                        .build());
    }

    @Bean
    public JCacheManagerCustomizer cacheManagerCustomizer() {
        return cm -> {
            createCache(cm, ni.edu.mney.domain.Paciente.class.getName());
            createCache(cm, ni.edu.mney.domain.Paciente.class.getName() + ".citas");
            createCache(cm, ni.edu.mney.domain.ExpedienteClinico.class.getName());
            createCache(cm, ni.edu.mney.domain.ExpedienteClinico.class.getName() + ".consultas");
            createCache(cm, ni.edu.mney.domain.ExpedienteClinico.class.getName() + ".historials");
            createCache(cm, ni.edu.mney.domain.ConsultaMedica.class.getName());
            createCache(cm, ni.edu.mney.domain.ConsultaMedica.class.getName() + ".diagnosticos");
            createCache(cm, ni.edu.mney.domain.ConsultaMedica.class.getName() + ".tratamientos");
            createCache(cm, ni.edu.mney.domain.ConsultaMedica.class.getName() + ".signosVitales");
            createCache(cm, ni.edu.mney.domain.ConsultaMedica.class.getName() + ".recetas");
            createCache(cm, ni.edu.mney.domain.Diagnostico.class.getName());
            createCache(cm, ni.edu.mney.domain.Tratamiento.class.getName());
            createCache(cm, ni.edu.mney.domain.Medicamento.class.getName());
            createCache(cm, ni.edu.mney.domain.Receta.class.getName());
            createCache(cm, ni.edu.mney.domain.CitaMedica.class.getName());
            createCache(cm, ni.edu.mney.domain.SignosVitales.class.getName());
            createCache(cm, ni.edu.mney.domain.HistorialClinico.class.getName());
            createCache(cm, ni.edu.mney.domain.AuditoriaAcciones.class.getName());
            createCache(cm, ni.edu.mney.repository.UserRepository.USERS_BY_LOGIN_CACHE);
            createCache(cm, ni.edu.mney.repository.UserRepository.USERS_BY_EMAIL_CACHE);
            // jhipster-needle-ehcache-add-entry
        };
    }

    private void createCache(javax.cache.CacheManager cm, String cacheName) {
        javax.cache.Cache<Object, Object> cache = cm.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        } else {
            cm.createCache(cacheName, jcacheConfiguration);
        }
    }
}
