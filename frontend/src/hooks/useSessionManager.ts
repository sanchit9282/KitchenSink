import { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import axios from 'axios';

const TOKEN_REFRESH_THRESHOLD = 60000; // Show dialog 1 minute before expiration
const CHECK_INTERVAL = 10000; // Check every 10 seconds

export const useSessionManager = () => {
  const [showDialog, setShowDialog] = useState(false);
  const [remainingTime, setRemainingTime] = useState(0);
  const [loading, setLoading] = useState(false);
  const { token, login, logout } = useAuth();
  const { showToast } = useToast();

  const parseJwt = (token: string) => {
    try {
      return JSON.parse(atob(token.split('.')[1]));
    } catch (e) {
      return null;
    }
  };

  const getTokenExpirationTime = useCallback((token: string | null) => {
    if (!token) return 0;
    const decodedToken = parseJwt(token);
    if (!decodedToken) return 0;
    return decodedToken.exp * 1000; // Convert to milliseconds
  }, []);

  const refreshToken = async () => {
    try {
      setLoading(true);
      const response = await axios.post('/api/auth/refresh-token', {
        refreshToken: localStorage.getItem('refreshToken')
      });
      
      login(response.data.accessToken, JSON.parse(localStorage.getItem('user') || '{}'));
      setShowDialog(false);
      showToast('Session extended successfully', 'success');
    } catch (error) {
      showToast('Failed to extend session', 'error');
      handleLogout();
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = useCallback(() => {
    setShowDialog(false);
    showToast('Logging out in 3 seconds...', 'info');
    setTimeout(() => {
      logout();
    }, 3000);
  }, [showToast, logout]);

  useEffect(() => {
    if (!token) return;

    const checkSession = () => {
      const expirationTime = getTokenExpirationTime(token);
      const currentTime = Date.now();
      const timeUntilExpiration = expirationTime - currentTime;

      if (timeUntilExpiration <= TOKEN_REFRESH_THRESHOLD && !showDialog) {
        setShowDialog(true);
        setRemainingTime(timeUntilExpiration);
      } else if (timeUntilExpiration <= 0) {
        handleLogout();
      }
    };

    const intervalId = setInterval(checkSession, CHECK_INTERVAL);
    return () => clearInterval(intervalId);
  }, [token, getTokenExpirationTime, showDialog, handleLogout]);

  return {
    showDialog,
    remainingTime,
    loading,
    refreshToken,
    handleLogout
  };
}; 