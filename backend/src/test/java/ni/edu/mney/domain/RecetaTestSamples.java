package ni.edu.mney.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class RecetaTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Receta getRecetaSample1() {
        return new Receta().id(1L).dosis("dosis1").frecuencia("frecuencia1").duracion("duracion1");
    }

    public static Receta getRecetaSample2() {
        return new Receta().id(2L).dosis("dosis2").frecuencia("frecuencia2").duracion("duracion2");
    }

    public static Receta getRecetaRandomSampleGenerator() {
        return new Receta()
            .id(longCount.incrementAndGet())
            .dosis(UUID.randomUUID().toString())
            .frecuencia(UUID.randomUUID().toString())
            .duracion(UUID.randomUUID().toString());
    }
}
