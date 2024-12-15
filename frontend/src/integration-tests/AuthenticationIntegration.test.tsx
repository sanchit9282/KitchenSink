import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { createMemoryHistory } from 'history';
import { Router } from 'react-router-dom';
import { AuthProvider } from '../context/AuthContext';
import { ToastProvider } from '../context/ToastContext';
import App from '../App';
import axios from 'axios';

// Mock axios
jest.mock('axios');
const mockedAxios = axios as jest.Mocked<typeof axios>;

// Create a wrapper component for providers
const renderWithProviders = (ui: React.ReactElement, { route = '/' } = {}) => {
  const history = createMemoryHistory({ initialEntries: [route] });
  return {
    ...render(
      <AuthProvider>
        <ToastProvider>
          <Router location={history.location} navigator={history}>
            {ui}
          </Router>
        </ToastProvider>
      </AuthProvider>
    ),
    history,
  };
};

describe('Authentication Integration', () => {
  beforeEach(() => {
    localStorage.clear();
    jest.clearAllMocks();
  });

  describe('Login Flow', () => {
    it('successfully logs in and redirects to members page', async () => {
      const mockLoginResponse = {
        data: {
          token: 'test-token',
          refreshToken: 'test-refresh-token',
          username: 'testuser',
          roles: ['ROLE_USER']
        }
      };

      mockedAxios.post.mockResolvedValueOnce(mockLoginResponse);

      renderWithProviders(<App />, { route: '/login' });

      // Fill in login form
      await waitFor(() => {
        fireEvent.change(screen.getByLabelText(/email/i), {
          target: { value: 'test@example.com' }
        });
        fireEvent.change(screen.getByLabelText(/password/i), {
          target: { value: 'password123' }
        });
      });

      // Submit form
      fireEvent.click(screen.getByRole('button', { name: /login/i }));

      // Verify redirect and storage
      await waitFor(() => {
        expect(window.location.pathname).toBe('/members');
        expect(localStorage.getItem('token')).toBe('test-token');
        expect(localStorage.getItem('refreshToken')).toBe('test-refresh-token');
      });
    });

    it('shows error message on login failure', async () => {
      mockedAxios.post.mockRejectedValueOnce({
        response: { data: { message: 'Invalid credentials' } }
      });

      renderWithProviders(<App />, { route: '/login' });

      // Fill and submit form
      await waitFor(() => {
        fireEvent.change(screen.getByLabelText(/email/i), {
          target: { value: 'wrong@example.com' }
        });
        fireEvent.change(screen.getByLabelText(/password/i), {
          target: { value: 'wrongpass' }
        });
      });

      fireEvent.click(screen.getByRole('button', { name: /login/i }));

      // Verify error message
      await waitFor(() => {
        expect(screen.getByText(/invalid credentials/i)).toBeInTheDocument();
      });
    });
  });

  describe('Registration Flow', () => {
    it('successfully registers and redirects to login', async () => {
      mockedAxios.post.mockResolvedValueOnce({
        data: { message: 'Registration successful' }
      });

      renderWithProviders(<App />, { route: '/register' });

      // Fill registration form
      await waitFor(() => {
        fireEvent.change(screen.getByLabelText(/username/i), {
          target: { value: 'newuser' }
        });
        fireEvent.change(screen.getByLabelText(/email/i), {
          target: { value: 'new@example.com' }
        });
        fireEvent.change(screen.getByLabelText(/password/i), {
          target: { value: 'password123' }
        });
      });

      // Submit form
      fireEvent.click(screen.getByRole('button', { name: /register/i }));

      // Verify redirect
      await waitFor(() => {
        expect(window.location.pathname).toBe('/login');
        expect(screen.getByText(/registration successful/i)).toBeInTheDocument();
      });
    });
  });

  describe('Protected Routes', () => {
    it('redirects to login when accessing protected route without auth', () => {
      renderWithProviders(<App />, { route: '/members' });

      expect(window.location.pathname).toBe('/login');
    });

    it('allows access to protected route with valid token', () => {
      // Setup authenticated state
      localStorage.setItem('token', 'valid-token');
      localStorage.setItem('user', JSON.stringify({
        username: 'testuser',
        roles: ['ROLE_USER']
      }));

      renderWithProviders(<App />, { route: '/members' });

      expect(window.location.pathname).toBe('/members');
    });
  });

  describe('Session Management', () => {
    it('shows session expiry dialog before token expires', async () => {
      // Mock token with near expiration
      const nearExpiryToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.' +
        'eyJzdWIiOiIxMjM0NTY3ODkwIiwiZXhwIjoiMTYxNjc2MjQwMCJ9.' +
        'signature';

      localStorage.setItem('token', nearExpiryToken);
      localStorage.setItem('user', JSON.stringify({
        username: 'testuser',
        roles: ['ROLE_USER']
      }));

      renderWithProviders(<App />, { route: '/members' });

      await waitFor(() => {
        expect(screen.getByText(/session expiring/i)).toBeInTheDocument();
      });
    });

    it('refreshes token successfully', async () => {
      const mockRefreshResponse = {
        data: { accessToken: 'new-token' }
      };

      mockedAxios.post.mockResolvedValueOnce(mockRefreshResponse);

      // Setup near-expired session
      localStorage.setItem('token', 'old-token');
      localStorage.setItem('refreshToken', 'valid-refresh-token');
      localStorage.setItem('user', JSON.stringify({
        username: 'testuser',
        roles: ['ROLE_USER']
      }));

      renderWithProviders(<App />, { route: '/members' });

      // Click continue session
      await waitFor(() => {
        fireEvent.click(screen.getByText(/continue session/i));
      });

      // Verify token refresh
      await waitFor(() => {
        expect(localStorage.getItem('token')).toBe('new-token');
      });
    });
  });
}); 