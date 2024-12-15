import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { expect, jest, describe, beforeEach, it } from '@jest/globals';
import Register from '../Register';
import { useNavigate } from 'react-router-dom';
import '@testing-library/jest-dom';
import { ToastProvider } from '../../../context/ToastContext';

// Mock dependencies
jest.mock('react-router-dom', () => ({
  useNavigate: () => jest.fn()
}));

describe('Register Component', () => {
  const mockNavigate = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (useNavigate as jest.Mock).mockReturnValue(mockNavigate);
  });

  it('renders registration form', () => {
    render(
      <ToastProvider>
        <Register />
      </ToastProvider>
    );

    expect(screen.getByLabelText(/username/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /register/i })).toBeInTheDocument();
  });

  it('handles form input changes', () => {
    render(
      <ToastProvider>
        <Register />
      </ToastProvider>
    );

    const usernameInput = screen.getByLabelText(/username/i);
    const emailInput = screen.getByLabelText(/email/i);
    const passwordInput = screen.getByLabelText(/password/i);

    fireEvent.change(usernameInput, { target: { value: 'testuser' } });
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'password123' } });

    expect(usernameInput).toHaveValue('testuser');
    expect(emailInput).toHaveValue('test@example.com');
    expect(passwordInput).toHaveValue('password123');
  });

  it('navigates to login page', () => {
    render(
      <ToastProvider>
        <Register />
      </ToastProvider>
    );

    fireEvent.click(screen.getByText(/already have an account/i));
    expect(mockNavigate).toHaveBeenCalledWith('/login');
  });

  it('disables button during submission', () => {
    render(
      <ToastProvider>
        <Register />
      </ToastProvider>
    );

    const registerButton = screen.getByRole('button', { name: /register/i });
    fireEvent.click(registerButton);
    expect(registerButton).toBeDisabled();
  });
}); 