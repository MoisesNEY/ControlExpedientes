package ni.edu.mney.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class HistorialClinicoTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static HistorialClinico getHistorialClinicoSample1() {
        return new HistorialClinico().id(1L).descripcion("descripcion1");
    }

    public static HistorialClinico getHistorialClinicoSample2() {
        return new HistorialClinico().id(2L).descripcion("descripcion2");
    }

    public static HistorialClinico getHistorialClinicoRandomSampleGenerator() {
        return new HistorialClinico().id(longCount.incrementAndGet()).descripcion(UUID.randomUUID().toString());
    }
}
