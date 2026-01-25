package ni.edu.mney.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TratamientoTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Tratamiento getTratamientoSample1() {
        return new Tratamiento().id(1L).indicaciones("indicaciones1").duracionDias(1);
    }

    public static Tratamiento getTratamientoSample2() {
        return new Tratamiento().id(2L).indicaciones("indicaciones2").duracionDias(2);
    }

    public static Tratamiento getTratamientoRandomSampleGenerator() {
        return new Tratamiento()
            .id(longCount.incrementAndGet())
            .indicaciones(UUID.randomUUID().toString())
            .duracionDias(intCount.incrementAndGet());
    }
}
