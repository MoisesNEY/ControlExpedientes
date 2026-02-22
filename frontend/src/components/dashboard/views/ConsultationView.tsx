import React from 'react';
import PatientHeader from '../PatientHeader';
import ConsultationForm from '../ConsultationForm';
import { usePatient } from '../../../context/PatientContext';

const ConsultationView = () => {
    const { selectedPatient } = usePatient();

    if (!selectedPatient) return null;

    return (
        <>
            <PatientHeader
                name={selectedPatient.name}
                id={selectedPatient.id}
                age={selectedPatient.age}
                gender={selectedPatient.gender}
                status={selectedPatient.status}
                image={selectedPatient.image}
            />
            <ConsultationForm />
        </>
    );
};

export default ConsultationView;
