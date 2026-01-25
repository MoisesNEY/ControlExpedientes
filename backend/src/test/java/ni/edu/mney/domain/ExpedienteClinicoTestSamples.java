package ni.edu.mney.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ExpedienteClinicoTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static ExpedienteClinico getExpedienteClinicoSample1() {
        return new ExpedienteClinico().id(1L).numeroExpediente("numeroExpediente1").observaciones("observaciones1");
    }

    public static ExpedienteClinico getExpedienteClinicoSample2() {
        return new ExpedienteClinico().id(2L).numeroExpediente("numeroExpediente2").observaciones("observaciones2");
    }

    public static ExpedienteClinico getExpedienteClinicoRandomSampleGenerator() {
        return new ExpedienteClinico()
            .id(longCount.incrementAndGet())
            .numeroExpediente(UUID.randomUUID().toString())
            .observaciones(UUID.randomUUID().toString());
    }
}
