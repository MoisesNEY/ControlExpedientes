package ni.edu.mney.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ConsultaMedicaTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static ConsultaMedica getConsultaMedicaSample1() {
        return new ConsultaMedica().id(1L).motivoConsulta("motivoConsulta1").notasMedicas("notasMedicas1");
    }

    public static ConsultaMedica getConsultaMedicaSample2() {
        return new ConsultaMedica().id(2L).motivoConsulta("motivoConsulta2").notasMedicas("notasMedicas2");
    }

    public static ConsultaMedica getConsultaMedicaRandomSampleGenerator() {
        return new ConsultaMedica()
            .id(longCount.incrementAndGet())
            .motivoConsulta(UUID.randomUUID().toString())
            .notasMedicas(UUID.randomUUID().toString());
    }
}
