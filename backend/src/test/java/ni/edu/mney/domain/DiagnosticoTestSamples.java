package ni.edu.mney.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class DiagnosticoTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Diagnostico getDiagnosticoSample1() {
        return new Diagnostico().id(1L).descripcion("descripcion1").codigoCIE("codigoCIE1");
    }

    public static Diagnostico getDiagnosticoSample2() {
        return new Diagnostico().id(2L).descripcion("descripcion2").codigoCIE("codigoCIE2");
    }

    public static Diagnostico getDiagnosticoRandomSampleGenerator() {
        return new Diagnostico()
            .id(longCount.incrementAndGet())
            .descripcion(UUID.randomUUID().toString())
            .codigoCIE(UUID.randomUUID().toString());
    }
}
