import React, { createContext, useContext, useState } from 'react';

interface Patient {
    id: string;
    name: string;
    age: string;
    gender: string;
    status: string;
    image: string;
    appointmentId?: number;
}

interface PatientContextType {
    selectedPatient: Patient | null;
    selectPatient: (patient: Patient | null) => void;
}

const PatientContext = createContext<PatientContextType | null>(null);

export const PatientProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [selectedPatient, setSelectedPatient] = useState<Patient | null>(null);

    const selectPatient = (patient: Patient | null) => {
        setSelectedPatient(patient);
    };

    return (
        <PatientContext.Provider value={{ selectedPatient, selectPatient }}>
            {children}
        </PatientContext.Provider>
    );
};

export const usePatient = () => {
    const context = useContext(PatientContext);
    if (!context) throw new Error('usePatient must be used within a PatientProvider');
    return context;
};
