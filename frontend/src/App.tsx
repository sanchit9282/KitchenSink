import React from 'react';
import './utils/axiosConfig.ts';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext.tsx';
import Login from './components/auth/Login.tsx';
import Register from './components/auth/Register.tsx';
import MemberList from './components/MemberList.tsx';
import ProtectedRoute from './components/auth/ProtectedRoute.tsx';
import Header from './components/layout/Header.tsx';
import Unauthorized from './components/auth/Unauthorized.tsx';
import { Container, Typography } from '@mui/material';
import { ToastProvider } from './context/ToastContext.tsx';
import SessionDialog from './components/auth/SessionDialog.tsx';
import { useSessionManager } from './hooks/useSessionManager.ts';

// Create a wrapper component that uses session management
const SessionManagement: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { showDialog, remainingTime, loading, refreshToken, handleLogout } = useSessionManager();

  return (
    <>
      {children}
      <SessionDialog
        open={showDialog}
        remainingTime={remainingTime}
        loading={loading}
        onContinue={refreshToken}
        onLogout={handleLogout}
      />
    </>
  );
};

const App: React.FC = () => {
  return (
    <AuthProvider>
      <ToastProvider>
        <Router>
          <Header />
          <Container sx={{ mt: 4 }}>
            <Routes>
              {/* Public Routes */}
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
              <Route path="/unauthorized" element={<Unauthorized />} />
              
              {/* Protected Routes */}
              <Route path="/members" element={
                <ProtectedRoute>
                  <SessionManagement>
                    <MemberList />
                  </SessionManagement>
                </ProtectedRoute>
              } />

              {/* Admin Only Routes */}
              <Route path="/admin" element={
                <ProtectedRoute requiredRoles={['ROLE_ADMIN']}>
                  <SessionManagement>
                    <div>
                      <Typography variant="h4" gutterBottom>
                        Admin Dashboard
                      </Typography>
                      {/* Admin components */}
                    </div>
                  </SessionManagement>
                </ProtectedRoute>
              } />

              {/* Redirect root to members page */}
              <Route path="/" element={<Navigate to="/members" replace />} />
            </Routes>
          </Container>
        </Router>
      </ToastProvider>
    </AuthProvider>
  );
};

export default App;

