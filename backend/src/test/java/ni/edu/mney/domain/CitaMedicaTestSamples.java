package ni.edu.mney.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class CitaMedicaTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static CitaMedica getCitaMedicaSample1() {
        return new CitaMedica().id(1L).observaciones("observaciones1");
    }

    public static CitaMedica getCitaMedicaSample2() {
        return new CitaMedica().id(2L).observaciones("observaciones2");
    }

    public static CitaMedica getCitaMedicaRandomSampleGenerator() {
        return new CitaMedica().id(longCount.incrementAndGet()).observaciones(UUID.randomUUID().toString());
    }
}
