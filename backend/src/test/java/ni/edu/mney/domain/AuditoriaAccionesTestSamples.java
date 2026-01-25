package ni.edu.mney.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class AuditoriaAccionesTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static AuditoriaAcciones getAuditoriaAccionesSample1() {
        return new AuditoriaAcciones().id(1L).entidad("entidad1").accion("accion1").descripcion("descripcion1");
    }

    public static AuditoriaAcciones getAuditoriaAccionesSample2() {
        return new AuditoriaAcciones().id(2L).entidad("entidad2").accion("accion2").descripcion("descripcion2");
    }

    public static AuditoriaAcciones getAuditoriaAccionesRandomSampleGenerator() {
        return new AuditoriaAcciones()
            .id(longCount.incrementAndGet())
            .entidad(UUID.randomUUID().toString())
            .accion(UUID.randomUUID().toString())
            .descripcion(UUID.randomUUID().toString());
    }
}
