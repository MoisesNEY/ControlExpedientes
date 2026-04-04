import React, { createContext, useContext, useState } from 'react';

interface Patient {
    id: string;
    patientId?: number;
    name: string;
    age: string;
    gender: string;
    status: string;
    image?: string;
    appointmentId?: number;
}

interface PatientContextType {
    selectedPatient: Patient | null;
    selectPatient: (patient: Patient | null) => void;
    updateSelectedPatient: (patch: Partial<Patient>) => void;
}

const PatientContext = createContext<PatientContextType | null>(null);

export const PatientProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [selectedPatient, setSelectedPatient] = useState<Patient | null>(null);

    const selectPatient = (patient: Patient | null) => {
        setSelectedPatient(patient);
    };

    const updateSelectedPatient = (patch: Partial<Patient>) => {
        setSelectedPatient(prev => (prev ? { ...prev, ...patch } : prev));
    };

    return (
        <PatientContext.Provider value={{ selectedPatient, selectPatient, updateSelectedPatient }}>
            {children}
        </PatientContext.Provider>
    );
};

export const usePatient = () => {
    const context = useContext(PatientContext);
    if (!context) throw new Error('usePatient must be used within a PatientProvider');
    return context;
};
