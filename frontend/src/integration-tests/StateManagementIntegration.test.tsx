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

describe('State Management Integration', () => {
  beforeEach(() => {
    localStorage.clear();
    jest.clearAllMocks();
  });

  describe('Authentication State', () => {
    it('persists authentication state across page reloads', async () => {
      // Setup initial auth state
      const authState = {
        token: 'test-token',
        user: { username: 'testuser', roles: ['ROLE_USER'] }
      };
      localStorage.setItem('token', authState.token);
      localStorage.setItem('user', JSON.stringify(authState.user));

      // Render app
      renderWithProviders(<App />);

      // Verify auth state is loaded
      await waitFor(() => {
        expect(screen.getByText(authState.user.username)).toBeInTheDocument();
      });
    });

    it('clears authentication state on logout', async () => {
      // Setup initial auth state
      localStorage.setItem('token', 'test-token');
      localStorage.setItem('user', JSON.stringify({ username: 'testuser', roles: ['ROLE_USER'] }));

      renderWithProviders(<App />);

      // Perform logout
      fireEvent.click(screen.getByRole('button', { name: /user menu/i }));
      fireEvent.click(screen.getByText(/logout/i));

      // Verify state is cleared
      expect(localStorage.getItem('token')).toBeNull();
      expect(localStorage.getItem('user')).toBeNull();
      expect(window.location.pathname).toBe('/login');
    });
  });

  describe('Toast State', () => {
    it('shows and auto-dismisses toast messages', async () => {
      renderWithProviders(<App />, { route: '/login' });

      // Trigger a failed login to show toast
      mockedAxios.post.mockRejectedValueOnce({
        response: { data: { message: 'Invalid credentials' } }
      });

      fireEvent.click(screen.getByRole('button', { name: /login/i }));

      // Verify toast appears
      await waitFor(() => {
        expect(screen.getByText(/invalid credentials/i)).toBeInTheDocument();
      });

      // Verify toast disappears
      await waitFor(() => {
        expect(screen.queryByText(/invalid credentials/i)).not.toBeInTheDocument();
      }, { timeout: 6000 });
    });

    it('stacks multiple toast messages', async () => {
      renderWithProviders(<App />, { route: '/members' });

      // Setup auth state
      localStorage.setItem('token', 'test-token');
      localStorage.setItem('user', JSON.stringify({ 
        username: 'admin', 
        roles: ['ROLE_ADMIN'] 
      }));

      // Trigger multiple actions that show toasts
      mockedAxios.post.mockRejectedValueOnce({
        response: { data: { message: 'Error 1' } }
      });
      mockedAxios.post.mockRejectedValueOnce({
        response: { data: { message: 'Error 2' } }
      });

      fireEvent.click(screen.getAllByRole('button', { name: /add member/i })[0]);
      fireEvent.click(screen.getAllByRole('button', { name: /add member/i })[0]);

      // Verify both toasts are visible
      await waitFor(() => {
        expect(screen.getByText(/error 1/i)).toBeInTheDocument();
        expect(screen.getByText(/error 2/i)).toBeInTheDocument();
      });
    });
  });

  describe('Form State', () => {
    it('maintains form state during navigation', async () => {
      renderWithProviders(<App />, { route: '/members' });

      // Setup auth state
      localStorage.setItem('token', 'test-token');
      localStorage.setItem('user', JSON.stringify({ 
        username: 'admin', 
        roles: ['ROLE_ADMIN'] 
      }));

      // Fill form
      await waitFor(() => {
        fireEvent.change(screen.getByLabelText(/name/i), {
          target: { value: 'Test Name' }
        });
      });

      // Navigate away and back
      fireEvent.click(screen.getByRole('button', { name: /user menu/i }));
      fireEvent.click(screen.getByText(/profile/i));
      fireEvent.click(screen.getByText(/members/i));

      // Verify form state is preserved
      expect(screen.getByLabelText(/name/i)).toHaveValue('Test Name');
    });

    it('resets form state after successful submission', async () => {
      renderWithProviders(<App />, { route: '/members' });

      // Setup auth state
      localStorage.setItem('token', 'test-token');
      localStorage.setItem('user', JSON.stringify({ 
        username: 'admin', 
        roles: ['ROLE_ADMIN'] 
      }));

      mockedAxios.post.mockResolvedValueOnce({
        data: { message: 'Success' }
      });

      // Fill and submit form
      await waitFor(() => {
        fireEvent.change(screen.getByLabelText(/name/i), {
          target: { value: 'Test Name' }
        });
      });

      fireEvent.click(screen.getByRole('button', { name: /add member/i }));

      // Verify form is reset
      await waitFor(() => {
        expect(screen.getByLabelText(/name/i)).toHaveValue('');
      });
    });
  });

  describe('Loading State', () => {
    it('shows and hides loading indicators', async () => {
      renderWithProviders(<App />, { route: '/members' });

      // Setup auth state
      localStorage.setItem('token', 'test-token');
      localStorage.setItem('user', JSON.stringify({ 
        username: 'admin', 
        roles: ['ROLE_ADMIN'] 
      }));

      mockedAxios.get.mockImplementationOnce(() => 
        new Promise(resolve => setTimeout(resolve, 100))
      );

      // Verify loading state
      expect(screen.getByRole('progressbar')).toBeInTheDocument();

      // Wait for loading to complete
      await waitFor(() => {
        expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
      });
    });
  });
}); 