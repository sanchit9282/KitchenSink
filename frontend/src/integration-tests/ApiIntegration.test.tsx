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

describe('API Integration', () => {
  beforeEach(() => {
    localStorage.clear();
    jest.clearAllMocks();
  });

  describe('Request Interceptors', () => {
    it('adds authorization header to authenticated requests', async () => {
      // Setup auth state
      localStorage.setItem('token', 'test-token');
      localStorage.setItem('user', JSON.stringify({
        username: 'testuser',
        roles: ['ROLE_USER']
      }));

      renderWithProviders(<App />, { route: '/members' });

      await waitFor(() => {
        const [request] = mockedAxios.get.mock.calls[0];
        expect(request).toContain('/api/members');
        expect(mockedAxios.get).toHaveBeenCalledWith(
          expect.any(String),
          expect.objectContaining({
            headers: expect.objectContaining({
              Authorization: 'Bearer test-token'
            })
          })
        );
      });
    });

    it('includes content-type header for POST requests', async () => {
      localStorage.setItem('token', 'test-token');
      localStorage.setItem('user', JSON.stringify({
        username: 'admin',
        roles: ['ROLE_ADMIN']
      }));

      renderWithProviders(<App />, { route: '/members' });

      // Submit a form
      await waitFor(() => {
        fireEvent.change(screen.getByLabelText(/name/i), {
          target: { value: 'Test Name' }
        });
        fireEvent.click(screen.getByRole('button', { name: /add member/i }));
      });

      expect(mockedAxios.post).toHaveBeenCalledWith(
        expect.any(String),
        expect.any(Object),
        expect.objectContaining({
          headers: expect.objectContaining({
            'Content-Type': 'application/json'
          })
        })
      );
    });
  });

  describe('Response Interceptors', () => {
    it('handles 401 responses by redirecting to login', async () => {
      localStorage.setItem('token', 'expired-token');
      localStorage.setItem('user', JSON.stringify({
        username: 'testuser',
        roles: ['ROLE_USER']
      }));

      mockedAxios.get.mockRejectedValueOnce({
        response: { status: 401 }
      });

      renderWithProviders(<App />, { route: '/members' });

      await waitFor(() => {
        expect(window.location.pathname).toBe('/login');
        expect(localStorage.getItem('token')).toBeNull();
      });
    });

    it('handles network errors with appropriate message', async () => {
      localStorage.setItem('token', 'test-token');
      localStorage.setItem('user', JSON.stringify({
        username: 'testuser',
        roles: ['ROLE_USER']
      }));

      mockedAxios.get.mockRejectedValueOnce(new Error('Network Error'));

      renderWithProviders(<App />, { route: '/members' });

      await waitFor(() => {
        expect(screen.getByText(/network error/i)).toBeInTheDocument();
      });
    });
  });

  describe('API Error Handling', () => {
    it('handles validation errors from API', async () => {
      localStorage.setItem('token', 'test-token');
      localStorage.setItem('user', JSON.stringify({
        username: 'admin',
        roles: ['ROLE_ADMIN']
      }));

      mockedAxios.post.mockRejectedValueOnce({
        response: {
          status: 400,
          data: {
            message: 'Validation failed',
            errors: ['Invalid email format']
          }
        }
      });

      renderWithProviders(<App />, { route: '/members' });

      // Submit invalid form
      await waitFor(() => {
        fireEvent.change(screen.getByLabelText(/email/i), {
          target: { value: 'invalid-email' }
        });
        fireEvent.click(screen.getByRole('button', { name: /add member/i }));
      });

      expect(screen.getByText(/invalid email format/i)).toBeInTheDocument();
    });

    it('handles server errors with fallback message', async () => {
      localStorage.setItem('token', 'test-token');
      localStorage.setItem('user', JSON.stringify({
        username: 'testuser',
        roles: ['ROLE_USER']
      }));

      mockedAxios.get.mockRejectedValueOnce({
        response: { status: 500 }
      });

      renderWithProviders(<App />, { route: '/members' });

      await waitFor(() => {
        expect(screen.getByText(/something went wrong/i)).toBeInTheDocument();
      });
    });
  });

  describe('Request Retries', () => {
    it('retries failed requests before giving up', async () => {
      localStorage.setItem('token', 'test-token');
      localStorage.setItem('user', JSON.stringify({
        username: 'testuser',
        roles: ['ROLE_USER']
      }));

      // Fail twice then succeed
      mockedAxios.get
        .mockRejectedValueOnce(new Error('Network Error'))
        .mockRejectedValueOnce(new Error('Network Error'))
        .mockResolvedValueOnce({
          data: {
            success: true,
            data: {
              content: [],
              totalElements: 0
            }
          }
        });

      renderWithProviders(<App />, { route: '/members' });

      await waitFor(() => {
        expect(mockedAxios.get).toHaveBeenCalledTimes(3);
      });
    });
  });
}); 