import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import { AuthProvider } from './context/AuthContext'
import { ThemeProvider } from './context/ThemeContext'
import { PatientProvider } from './context/PatientContext'
import { SystemSettingsProvider } from './context/SystemSettingsContext'
import { LanguageProvider } from './context/LanguageContext'
import { GlobalTranslator } from './i18n/GlobalTranslator'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AuthProvider>
      <ThemeProvider>
        <LanguageProvider>
          <SystemSettingsProvider>
            <PatientProvider>
              <GlobalTranslator />
              <App />
            </PatientProvider>
          </SystemSettingsProvider>
        </LanguageProvider>
      </ThemeProvider>
    </AuthProvider>
  </StrictMode>,
)
