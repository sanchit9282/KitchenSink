import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { createMemoryHistory } from 'history';
import { Router } from 'react-router-dom';
import { AuthProvider } from '../context/AuthContext';
import { ToastProvider } from '../context/ToastContext';
import App from '../App';

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

describe('Navigation Integration', () => {
  beforeEach(() => {
    localStorage.clear();
    jest.clearAllMocks();
  });

  describe('Public Routes', () => {
    it('allows access to login page', () => {
      const { history } = renderWithProviders(<App />, { route: '/login' });
      expect(history.location.pathname).toBe('/login');
      expect(screen.getByRole('button', { name: /login/i })).toBeInTheDocument();
    });

    it('allows access to register page', () => {
      const { history } = renderWithProviders(<App />, { route: '/register' });
      expect(history.location.pathname).toBe('/register');
      expect(screen.getByRole('button', { name: /register/i })).toBeInTheDocument();
    });

    it('redirects to login from protected routes when not authenticated', () => {
      const { history } = renderWithProviders(<App />, { route: '/members' });
      expect(history.location.pathname).toBe('/login');
    });
  });

  describe('Protected Routes', () => {
    beforeEach(() => {
      // Setup authenticated state
      localStorage.setItem('token', 'valid-token');
      localStorage.setItem('user', JSON.stringify({
        username: 'testuser',
        roles: ['ROLE_USER']
      }));
    });

    it('allows access to members page when authenticated', () => {
      const { history } = renderWithProviders(<App />, { route: '/members' });
      expect(history.location.pathname).toBe('/members');
    });

    it('redirects to unauthorized page for insufficient permissions', () => {
      const { history } = renderWithProviders(<App />, { route: '/admin' });
      expect(history.location.pathname).toBe('/unauthorized');
    });

    it('maintains location state during redirects', () => {
      const { history } = renderWithProviders(<App />, { 
        route: '/members',
        // @ts-ignore
        state: { from: '/some-route' }
      });
      expect(history.location.state).toBeDefined();
    });
  });

  describe('Navigation Guards', () => {
    it('prevents navigation when form is dirty', async () => {
      // Setup authenticated state
      localStorage.setItem('token', 'valid-token');
      localStorage.setItem('user', JSON.stringify({
        username: 'admin',
        roles: ['ROLE_ADMIN']
      }));

      renderWithProviders(<App />, { route: '/members' });

      // Start editing a form
      await waitFor(() => {
        fireEvent.change(screen.getByLabelText(/name/i), {
          target: { value: 'New Name' }
        });
      });

      // Try to navigate away
      const confirmSpy = jest.spyOn(window, 'confirm').mockImplementation(() => false);
      fireEvent.click(screen.getByText(/logout/i));

      expect(confirmSpy).toHaveBeenCalled();
      confirmSpy.mockRestore();
    });
  });

  describe('Header Navigation', () => {
    beforeEach(() => {
      localStorage.setItem('token', 'valid-token');
      localStorage.setItem('user', JSON.stringify({
        username: 'testuser',
        roles: ['ROLE_USER']
      }));
    });

    it('navigates to correct routes from header menu', () => {
      const { history } = renderWithProviders(<App />);

      // Open user menu
      fireEvent.click(screen.getByRole('button', { name: /user menu/i }));

      // Click members link
      fireEvent.click(screen.getByText(/members/i));
      expect(history.location.pathname).toBe('/members');
    });

    it('updates active menu item', () => {
      renderWithProviders(<App />, { route: '/members' });

      // Open user menu
      fireEvent.click(screen.getByRole('button', { name: /user menu/i }));

      // Check active state
      const membersLink = screen.getByText(/members/i).closest('a');
      expect(membersLink).toHaveClass('active');
    });
  });

  describe('Error Navigation', () => {
    it('navigates to error page for non-existent routes', () => {
      const { history } = renderWithProviders(<App />, { route: '/non-existent' });
      expect(screen.getByText(/page not found/i)).toBeInTheDocument();
    });

    it('provides navigation options from error page', () => {
      renderWithProviders(<App />, { route: '/non-existent' });

      expect(screen.getByRole('button', { name: /go home/i })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /go back/i })).toBeInTheDocument();
    });
  });

  describe('History Management', () => {
    it('handles browser back navigation', () => {
      const { history } = renderWithProviders(<App />, { route: '/login' });

      // Navigate to register
      fireEvent.click(screen.getByText(/register/i));
      expect(history.location.pathname).toBe('/register');

      // Go back
      history.go(-1);
      expect(history.location.pathname).toBe('/login');
    });

    it('preserves state during navigation', () => {
      const { history } = renderWithProviders(<App />, { 
        route: '/members',
        // @ts-ignore
        state: { searchTerm: 'test' }
      });

      expect(history.location.state).toEqual({ searchTerm: 'test' });
    });
  });
}); 