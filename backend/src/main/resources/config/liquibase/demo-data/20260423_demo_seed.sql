DELETE FROM interaccion_medicamentosa;
DELETE FROM resultado_laboratorio;
DELETE FROM auditoria_acciones;
DELETE FROM historial_clinico;
DELETE FROM receta;
DELETE FROM tratamiento;
DELETE FROM signos_vitales;
DELETE FROM diagnostico;
DELETE FROM consulta_medica;
DELETE FROM cita_medica;
DELETE FROM paciente;
DELETE FROM expediente_clinico;
DELETE FROM medicamento;

INSERT INTO expediente_clinico (id, numero_expediente, fecha_apertura, observaciones) VALUES
    (1, 'EXP-2026-0301', '2026-03-01', 'Ingreso por control de enfermedades crónicas.'),
    (2, 'EXP-2026-0302', '2026-03-03', 'Paciente referido desde jornada comunitaria.'),
    (3, 'EXP-2026-0303', '2026-03-05', 'Seguimiento pediátrico y esquema de vacunas.'),
    (4, 'EXP-2026-0304', '2026-03-08', 'Apertura por episodio respiratorio agudo.'),
    (5, 'EXP-2026-0305', '2026-03-12', 'Paciente embarazada en control prenatal.'),
    (6, 'EXP-2026-0306', '2026-03-16', 'Control metabólico y educación nutricional.'),
    (7, 'EXP-2026-0307', '2026-03-20', 'Seguimiento post alta por crisis asmática.'),
    (8, 'EXP-2026-0308', '2026-03-24', 'Revisión músculo esquelética por dolor lumbar.');

INSERT INTO paciente (id, codigo, nombres, apellidos, sexo, fecha_nacimiento, cedula, telefono, direccion, estado_civil, email, activo, expediente_id) VALUES
    (1, 'PAC-260301', 'Marta Elena', 'Guzman Rivas', 'FEMENINO', '1987-06-18', '001-180687-0001A', '8888-1010', 'Managua, Nicaragua', 'CASADO', 'marta.guzman@example.com', true, 1),
    (2, 'PAC-260302', 'Luis Alberto', 'Morales Centeno', 'MASCULINO', '1979-09-04', '001-040979-0002B', '8888-2020', 'Masaya, Nicaragua', 'CASADO', 'luis.morales@example.com', true, 2),
    (3, 'PAC-260303', 'Daniela Sofia', 'Pineda Rocha', 'FEMENINO', '2016-01-09', '001-090116-0003C', '8888-3030', 'Granada, Nicaragua', 'SOLTERO', 'familia.pineda@example.com', true, 3),
    (4, 'PAC-260304', 'Jose Manuel', 'Toruño Flores', 'MASCULINO', '1993-02-11', '001-110293-0004D', '8888-4040', 'Leon, Nicaragua', 'SOLTERO', 'jose.toruno@example.com', true, 4),
    (5, 'PAC-260305', 'Andrea Isabel', 'Mendoza Ruiz', 'FEMENINO', '1998-07-21', '001-210798-0005E', '8888-5050', 'Tipitapa, Nicaragua', 'SOLTERO', 'andrea.mendoza@example.com', true, 5),
    (6, 'PAC-260306', 'Ramon Ernesto', 'Lopez Valle', 'MASCULINO', '1968-11-30', '001-301168-0006F', '8888-6060', 'Esteli, Nicaragua', 'CASADO', 'ramon.lopez@example.com', true, 6),
    (7, 'PAC-260307', 'Karen Patricia', 'Navarro Mejia', 'FEMENINO', '2001-04-14', '001-140401-0007G', '8888-7070', 'Managua, Nicaragua', 'SOLTERO', 'karen.navarro@example.com', true, 7),
    (8, 'PAC-260308', 'Oscar Javier', 'Blandon Vega', 'MASCULINO', '1984-12-02', '001-021284-0008H', '8888-8080', 'Matagalpa, Nicaragua', 'DIVORCIADO', 'oscar.blandon@example.com', true, 8);

INSERT INTO medicamento (id, nombre, descripcion, stock) VALUES
    (1, 'Paracetamol 500mg', 'Analgésico y antipirético de uso frecuente.', 420),
    (2, 'Ibuprofeno 400mg', 'Antiinflamatorio no esteroideo.', 260),
    (3, 'Amoxicilina 500mg', 'Antibiótico para infecciones respiratorias.', 180),
    (4, 'Omeprazol 20mg', 'Protector gástrico.', 210),
    (5, 'Loratadina 10mg', 'Antihistamínico para alergias.', 180),
    (6, 'Metformina 850mg', 'Control metabólico en diabetes tipo 2.', 160),
    (7, 'Losartan 50mg', 'Antihipertensivo.', 150),
    (8, 'Salbutamol inhalador', 'Broncodilatador de rescate.', 64),
    (9, 'Azitromicina 500mg', 'Antibiótico macrólido.', 96),
    (10, 'Diclofenaco 50mg', 'Analgésico antiinflamatorio.', 140),
    (11, 'Aspirina 100mg', 'Antiagregante plaquetario.', 110),
    (12, 'Warfarina 5mg', 'Anticoagulante oral.', 72),
    (13, 'Fluconazol 150mg', 'Antifúngico sistémico.', 54),
    (14, 'Methotrexato 2.5mg', 'Inmunomodulador de manejo especializado.', 28),
    (15, 'Clopidogrel 75mg', 'Antiagregante plaquetario de mantenimiento.', 80);

INSERT INTO cita_medica (id, fecha_hora, estado, observaciones, user_id, paciente_id) VALUES
    (1, '2026-03-01 08:00:00', 'ATENDIDA', 'Control inicial por hipertensión y dispepsia.', NULL, 1),
    (2, '2026-03-03 09:15:00', 'ATENDIDA', 'Evaluación por tos y fiebre de 48 horas.', NULL, 2),
    (3, '2026-03-05 10:30:00', 'ATENDIDA', 'Consulta pediátrica de control y rinofaringitis.', NULL, 3),
    (4, '2026-03-08 14:00:00', 'ATENDIDA', 'Consulta respiratoria con manejo inhalado.', NULL, 4),
    (5, '2026-03-12 08:30:00', 'ATENDIDA', 'Control prenatal del segundo trimestre.', NULL, 5),
    (6, '2026-03-16 11:00:00', 'ATENDIDA', 'Revisión metabólica y ajuste dietético.', NULL, 6),
    (7, '2026-03-20 07:45:00', 'ATENDIDA', 'Seguimiento posterior a crisis asmática.', NULL, 7),
    (8, '2026-03-24 15:30:00', 'ATENDIDA', 'Dolor lumbar mecánico asociado a sobreesfuerzo.', NULL, 8),
    (9, '2026-03-26 08:20:00', 'PROGRAMADA', 'Control de presión arterial y adherencia.', NULL, 1),
    (10, '2026-03-27 09:40:00', 'EN_SALA_ESPERA', 'Control respiratorio con resultados de laboratorio.', NULL, 4),
    (11, '2026-03-28 10:00:00', 'ESPERANDO_MEDICO', 'Revisión de glucosa capilar y consejería.', NULL, 6),
    (12, '2026-03-29 13:15:00', 'CANCELADA', 'Paciente reagendó por motivos laborales.', NULL, 8);

INSERT INTO consulta_medica (id, fecha_consulta, motivo_consulta, notas_medicas, user_id, expediente_id) VALUES
    (1, '2026-03-01', 'Control de hipertensión y gastritis', 'Presión arterial limítrofe. Se refuerza dieta baja en sodio y protección gástrica.', NULL, 1),
    (2, '2026-03-03', 'Fiebre con odinofagia', 'Se identifican amígdalas hiperémicas y congestión faríngea sin datos de alarma.', NULL, 2),
    (3, '2026-03-05', 'Congestión nasal pediátrica', 'Paciente hidratada, sin dificultad respiratoria. Manejo ambulatorio y vigilancia domiciliaria.', NULL, 3),
    (4, '2026-03-08', 'Disnea y sibilancias', 'Respuesta favorable a broncodilatador. Se indicó plan de rescate y signos de alarma.', NULL, 4),
    (5, '2026-03-12', 'Control prenatal', 'Altura uterina acorde, sin edema y con buena evolución clínica.', NULL, 5),
    (6, '2026-03-16', 'Control metabólico', 'Glucosa en ayuno por encima de meta. Se ajusta educación nutricional y adherencia.', NULL, 6),
    (7, '2026-03-20', 'Seguimiento por asma', 'Persisten síntomas nocturnos leves. Se refuerza técnica inhalatoria.', NULL, 7),
    (8, '2026-03-24', 'Dolor lumbar mecánico', 'No hay compromiso neurológico. Se recomienda analgesia y ejercicios de estiramiento.', NULL, 8);

INSERT INTO diagnostico (id, descripcion, codigo_cie, consulta_id) VALUES
    (1, 'Hipertensión esencial en seguimiento ambulatorio', 'I10', 1),
    (2, 'Gastritis sin datos de complicación', 'K297', 1),
    (3, 'Faringitis aguda', 'J029', 2),
    (4, 'Rinofaringitis aguda', 'J00', 3),
    (5, 'Asma no especificada con reagudización leve', 'J459', 4),
    (6, 'Supervisión de embarazo normal', 'Z349', 5),
    (7, 'Diabetes mellitus tipo 2 sin complicaciones', 'E119', 6),
    (8, 'Asma no especificada en control', 'J459', 7),
    (9, 'Lumbago no especificado', 'M545', 8);

INSERT INTO signos_vitales (id, peso, altura, presion_arterial, temperatura, frecuencia_cardiaca, consulta_id) VALUES
    (1, 71.2, 1.61, '138/88', 36.7, 78, 1),
    (2, 83.5, 1.74, '122/80', 37.9, 92, 2),
    (3, 24.6, 1.19, '96/60', 37.2, 104, 3),
    (4, 68.8, 1.70, '118/76', 36.9, 96, 4),
    (5, 65.0, 1.58, '110/72', 36.6, 82, 5),
    (6, 89.1, 1.73, '132/84', 36.8, 80, 6),
    (7, 57.4, 1.62, '114/74', 36.5, 76, 7),
    (8, 86.0, 1.76, '126/82', 36.6, 79, 8);

INSERT INTO tratamiento (id, indicaciones, duracion_dias, consulta_id) VALUES
    (1, 'Control domiciliario de presión arterial y reducción de sal en la dieta.', 30, 1),
    (2, 'Hidratación oral, reposo relativo y vigilancia de fiebre persistente.', 5, 2),
    (3, 'Lavados nasales, control de temperatura y seguimiento por pediatría.', 4, 3),
    (4, 'Uso correcto de inhalador, evitar desencadenantes y regreso si empeora.', 14, 4),
    (5, 'Continuar vitaminas prenatales y asistir a ultrasonido programado.', 30, 5),
    (6, 'Plan alimentario con reducción de harinas y caminatas supervisadas.', 30, 6),
    (7, 'Mantener inhalador de rescate a mano y registrar síntomas nocturnos.', 21, 7),
    (8, 'Aplicar calor local, pausas activas y ejercicios de movilidad.', 10, 8);

INSERT INTO receta (id, dosis, frecuencia, duracion, medicamento_id, consulta_id, cantidad) VALUES
    (1, '50 mg', 'Cada 24 horas', '30 días', 7, 1, 30),
    (2, '20 mg', 'Antes del desayuno', '21 días', 4, 1, 21),
    (3, '500 mg', 'Cada 8 horas', '7 días', 3, 2, 21),
    (4, '10 mg', 'Cada 24 horas', '5 días', 5, 3, 5),
    (5, '2 inhalaciones', 'Cada 6 horas si hay crisis', '14 días', 8, 4, 1),
    (6, '1 tableta', 'Cada 24 horas', '30 días', 4, 5, 30),
    (7, '850 mg', 'Con desayuno y cena', '30 días', 6, 6, 60),
    (8, '2 inhalaciones', 'Antes de dormir y si hay síntomas', '21 días', 8, 7, 2),
    (9, '50 mg', 'Cada 12 horas después de comida', '5 días', 10, 8, 10),
    (10, '500 mg', 'Cada 8 horas si hay dolor', '5 días', 1, 8, 15);

INSERT INTO historial_clinico (id, fecha_registro, descripcion, expediente_id) VALUES
    (1, '2026-03-01 07:45:00', 'Recepción verifica datos demográficos y abre expediente.', 1),
    (2, '2026-03-01 08:50:00', 'Se entrega plan de control de presión arterial.', 1),
    (3, '2026-03-03 09:00:00', 'Paciente ingresa con fiebre y dolor de garganta.', 2),
    (4, '2026-03-03 10:05:00', 'Se indica tratamiento antibiótico y signos de alarma.', 2),
    (5, '2026-03-05 10:10:00', 'Madre reporta congestión nasal desde hace dos días.', 3),
    (6, '2026-03-05 11:00:00', 'Se agenda control pediátrico telefónico para 72 horas.', 3),
    (7, '2026-03-08 13:40:00', 'Paciente triage con disnea leve y sibilancias.', 4),
    (8, '2026-03-08 14:50:00', 'Mejoría clínica posterior a nebulización y educación de rescate.', 4),
    (9, '2026-03-12 08:10:00', 'Control prenatal sin datos de alarma.', 5),
    (10, '2026-03-12 08:55:00', 'Se refuerza asistencia a laboratorio prenatal.', 5),
    (11, '2026-03-16 10:40:00', 'Paciente refiere adherencia parcial a dieta para diabetes.', 6),
    (12, '2026-03-16 11:45:00', 'Se programa revisión de glucosa y control a dos semanas.', 6),
    (13, '2026-03-20 07:30:00', 'Seguimiento por crisis asmática con síntomas nocturnos leves.', 7),
    (14, '2026-03-20 08:30:00', 'Se ajusta plan de uso de inhalador de rescate.', 7),
    (15, '2026-03-24 15:10:00', 'Dolor lumbar posterior a sobrecarga laboral.', 8),
    (16, '2026-03-24 16:00:00', 'Se entrega incapacidad breve y ejercicios de estiramiento.', 8);

INSERT INTO auditoria_acciones (id, entidad, accion, fecha, descripcion, user_id) VALUES
    (1, 'Paciente', 'CREAR', '2026-03-01 07:40:00', 'Registro inicial de Marta Elena Guzman Rivas.', NULL),
    (2, 'ExpedienteClinico', 'CREAR', '2026-03-01 07:41:00', 'Apertura del expediente EXP-2026-0301.', NULL),
    (3, 'CitaMedica', 'ACTUALIZAR', '2026-03-03 09:12:00', 'Cambio a estado EN_SALA_ESPERA para PAC-260302.', NULL),
    (4, 'ConsultaMedica', 'CREAR', '2026-03-03 09:20:00', 'Inicio de consulta por fiebre y odinofagia.', NULL),
    (5, 'Receta', 'CREAR', '2026-03-03 09:55:00', 'Se emite receta con amoxicilina para la consulta 2.', NULL),
    (6, 'Paciente', 'CREAR', '2026-03-05 10:05:00', 'Registro pediátrico PAC-260303.', NULL),
    (7, 'SignosVitales', 'CREAR', '2026-03-08 13:50:00', 'Toma de signos vitales previa a manejo respiratorio.', NULL),
    (8, 'ConsultaMedica', 'ACTUALIZAR', '2026-03-08 14:45:00', 'Consulta 4 finalizada con plan inhalado.', NULL),
    (9, 'Paciente', 'CREAR', '2026-03-12 08:00:00', 'Registro obstétrico PAC-260305.', NULL),
    (10, 'CitaMedica', 'ACTUALIZAR', '2026-03-16 10:58:00', 'Paciente PAC-260306 pasa a consulta médica.', NULL),
    (11, 'Tratamiento', 'CREAR', '2026-03-16 11:20:00', 'Se documenta plan nutricional para consulta 6.', NULL),
    (12, 'ConsultaMedica', 'CREAR', '2026-03-20 07:50:00', 'Seguimiento por asma para PAC-260307.', NULL),
    (13, 'Receta', 'CREAR', '2026-03-20 08:15:00', 'Receta inhalada emitida para consulta 7.', NULL),
    (14, 'ConsultaMedica', 'CREAR', '2026-03-24 15:35:00', 'Consulta musculoesquelética inicial para PAC-260308.', NULL),
    (15, 'CitaMedica', 'ACTUALIZAR', '2026-03-27 09:40:00', 'Cita 10 registrada en sala de espera.', NULL),
    (16, 'CitaMedica', 'ACTUALIZAR', '2026-03-29 09:00:00', 'Cita 12 cancelada por reagendamiento del paciente.', NULL);

INSERT INTO resultado_laboratorio (id, tipo_examen, resultado, valor_referencia, unidad, observaciones, fecha_examen, paciente_id, consulta_id) VALUES
    (1, 'Glucosa en ayuno', '148', '70-100', 'mg/dL', 'Resultado pendiente de nuevo control en 15 días.', '2026-03-15', 6, 6),
    (2, 'Hemoglobina', '11.8', '11-15', 'g/dL', 'Control prenatal dentro de rango aceptable.', '2026-03-11', 5, 5),
    (3, 'Proteína C reactiva', '8.2', '0-5', 'mg/L', 'Inflamación leve asociada a cuadro respiratorio.', '2026-03-03', 2, 2),
    (4, 'Pico espiratorio', '330', '320-380', 'L/min', 'Se documenta mejoría posterior a broncodilatador.', '2026-03-20', 7, 7);

INSERT INTO interaccion_medicamentosa (id, medicamento_a_id, medicamento_b_id, severidad, descripcion, recomendacion) VALUES
    (1, 2, 11, 'MODERADA', 'El ibuprofeno puede disminuir el efecto antiagregante de la aspirina y aumentar el riesgo gastrointestinal.', 'Administrar en horarios separados y valorar paracetamol si el dolor es leve.'),
    (2, 3, 14, 'GRAVE', 'La amoxicilina puede reducir la eliminación de methotrexato y elevar su toxicidad.', 'Usar antibiótico alternativo o monitorizar biometría y función renal.'),
    (3, 4, 15, 'MODERADA', 'Omeprazol puede disminuir la activación de clopidogrel por interferencia metabólica.', 'Preferir un inhibidor gástrico con menor interacción o vigilar respuesta clínica.'),
    (4, 12, 11, 'GRAVE', 'Warfarina con aspirina incrementa de forma importante el riesgo de sangrado.', 'Evitar combinación salvo indicación estricta y con control cercano de INR.'),
    (5, 13, 12, 'GRAVE', 'Fluconazol puede elevar el efecto anticoagulante de warfarina.', 'Ajustar dosis y reforzar monitorización de INR durante la coadministración.');
