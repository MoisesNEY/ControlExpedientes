package ni.edu.mney.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class PacienteTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Paciente getPacienteSample1() {
        return new Paciente()
            .id(1L)
            .codigo("codigo1")
            .nombres("nombres1")
            .apellidos("apellidos1")
            .cedula("cedula1")
            .telefono("telefono1")
            .direccion("direccion1")
            .email("email1");
    }

    public static Paciente getPacienteSample2() {
        return new Paciente()
            .id(2L)
            .codigo("codigo2")
            .nombres("nombres2")
            .apellidos("apellidos2")
            .cedula("cedula2")
            .telefono("telefono2")
            .direccion("direccion2")
            .email("email2");
    }

    public static Paciente getPacienteRandomSampleGenerator() {
        return new Paciente()
            .id(longCount.incrementAndGet())
            .codigo(UUID.randomUUID().toString())
            .nombres(UUID.randomUUID().toString())
            .apellidos(UUID.randomUUID().toString())
            .cedula(UUID.randomUUID().toString())
            .telefono(UUID.randomUUID().toString())
            .direccion(UUID.randomUUID().toString())
            .email(UUID.randomUUID().toString());
    }
}
