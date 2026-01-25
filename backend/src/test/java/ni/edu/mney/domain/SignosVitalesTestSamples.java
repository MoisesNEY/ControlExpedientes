package ni.edu.mney.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class SignosVitalesTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static SignosVitales getSignosVitalesSample1() {
        return new SignosVitales().id(1L).presionArterial("presionArterial1").frecuenciaCardiaca(1);
    }

    public static SignosVitales getSignosVitalesSample2() {
        return new SignosVitales().id(2L).presionArterial("presionArterial2").frecuenciaCardiaca(2);
    }

    public static SignosVitales getSignosVitalesRandomSampleGenerator() {
        return new SignosVitales()
            .id(longCount.incrementAndGet())
            .presionArterial(UUID.randomUUID().toString())
            .frecuenciaCardiaca(intCount.incrementAndGet());
    }
}
