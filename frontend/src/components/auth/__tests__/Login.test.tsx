import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { expect, jest, describe, beforeEach, it } from '@jest/globals';
import Login from '../Login';
import * as authService from '../../../services/auth.service';
import { useNavigate, useLocation } from 'react-router-dom';
import '@testing-library/jest-dom';
import { AuthProvider } from '../../../context/AuthContext';
import { ToastProvider } from '../../../context/ToastContext';

// Mocks
jest.mock('../../../services/auth.service');
jest.mock('react-router-dom', () => ({
  useNavigate: () => jest.fn(),
  useLocation: () => ({ state: { from: { pathname: '/members' } } })
}));

// Wrapper
const renderWithProviders = (ui: React.ReactElement) => {
  return render(
    <AuthProvider>
      <ToastProvider>
        {ui}
      </ToastProvider>
    </AuthProvider>
  );
};

describe('Login Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('handles successful login', async () => {
    jest.spyOn(authService, 'login').mockResolvedValueOnce({
      data: {
        token: 'test-token',
        refreshToken: 'test-refresh-token',
        username: 'testuser',
        roles: ['ROLE_USER']
      },
      status: 200,
      statusText: 'OK',
      headers: {},
      config: {} as any
    });

    renderWithProviders(<Login />);
    
    await userEvent.type(screen.getByLabelText(/email/i), 'test@example.com');
    await userEvent.type(screen.getByLabelText(/password/i), 'password123');
    fireEvent.click(screen.getByRole('button', { name: /login/i }));

    await waitFor(() => {
      expect(screen.getByText(/success/i)).toBeInTheDocument();
    });
  });

  it('handles login failure', async () => {
    jest.spyOn(authService, 'login').mockRejectedValueOnce({
      response: { data: { message: 'Invalid credentials' } }
    });

    renderWithProviders(<Login />);
    
    await userEvent.type(screen.getByLabelText(/email/i), 'test@example.com');
    await userEvent.type(screen.getByLabelText(/password/i), 'wrongpassword');
    fireEvent.click(screen.getByRole('button', { name: /login/i }));

    await waitFor(() => {
      expect(screen.getByText(/invalid credentials/i)).toBeInTheDocument();
    });
  });

  it('shows loading state during login', async () => {
    jest.spyOn(authService, 'login').mockImplementationOnce(
      () => new Promise(resolve => setTimeout(resolve, 100))
    );

    renderWithProviders(<Login />);
    
    await userEvent.type(screen.getByLabelText(/email/i), 'test@example.com');
    await userEvent.type(screen.getByLabelText(/password/i), 'password123');
    
    const loginButton = screen.getByRole('button', { name: /login/i });
    fireEvent.click(loginButton);

    expect(loginButton).toBeDisabled();
    expect(screen.getByText(/logging in/i)).toBeInTheDocument();
  });
}); 