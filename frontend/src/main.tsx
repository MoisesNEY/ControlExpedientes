import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import { AuthProvider } from './context/AuthContext'
import { ThemeProvider } from './context/ThemeContext'
import { PatientProvider } from './context/PatientContext'
import { SystemSettingsProvider } from './context/SystemSettingsContext'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AuthProvider>
      <ThemeProvider>
        <SystemSettingsProvider>
          <PatientProvider>
            <App />
          </PatientProvider>
        </SystemSettingsProvider>
      </ThemeProvider>
    </AuthProvider>
  </StrictMode>,
)
