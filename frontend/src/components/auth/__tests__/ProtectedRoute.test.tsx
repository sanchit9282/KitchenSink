import React from 'react';
import { render, screen } from '@testing-library/react';
import { expect, jest, describe, beforeEach, it } from '@jest/globals';
import ProtectedRoute from '../ProtectedRoute';
import { useAuth } from '../../../context/AuthContext';
import { useNavigate, useLocation } from 'react-router-dom';
import '@testing-library/jest-dom';
import { AuthProvider } from '../../../context/AuthContext';

// Mock the auth hook
jest.mock('../../../context/AuthContext', () => ({
  AuthProvider: ({ children }: { children: React.ReactNode }) => <>{children}</>,
  useAuth: jest.fn()
}));

// Mock react-router-dom
jest.mock('react-router-dom', () => ({
  useNavigate: () => jest.fn(),
  useLocation: () => ({ pathname: '/protected' })
}));

// Test component
const TestComponent = () => <div>Protected Content</div>;

describe('ProtectedRoute Component', () => {
  const mockNavigate = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (useNavigate as jest.Mock).mockReturnValue(mockNavigate);
  });

  it('renders children when user is authenticated', () => {
    (useAuth as jest.Mock).mockReturnValue({
      user: { username: 'testuser', roles: ['ROLE_USER'] },
      token: 'valid-token'
    });

    render(
      <AuthProvider>
        <ProtectedRoute>
          <TestComponent />
        </ProtectedRoute>
      </AuthProvider>
    );

    expect(screen.getByText('Protected Content')).toBeInTheDocument();
    expect(mockNavigate).not.toHaveBeenCalled();
  });

  it('redirects to login when user is not authenticated', () => {
    (useAuth as jest.Mock).mockReturnValue({
      user: null,
      token: null
    });

    render(
      <AuthProvider>
        <ProtectedRoute>
          <TestComponent />
        </ProtectedRoute>
      </AuthProvider>
    );

    expect(mockNavigate).toHaveBeenCalledWith('/login', {
      state: { from: { pathname: '/protected' } }
    });
  });

  it('handles role-based access control', () => {
    (useAuth as jest.Mock).mockReturnValue({
      user: { username: 'testuser', roles: ['ROLE_USER'] },
      token: 'valid-token'
    });

    render(
      <AuthProvider>
        <ProtectedRoute requiredRoles={['ROLE_ADMIN']}>
          <TestComponent />
        </ProtectedRoute>
      </AuthProvider>
    );

    expect(mockNavigate).toHaveBeenCalledWith('/unauthorized');
  });

  it('allows access with correct role', () => {
    (useAuth as jest.Mock).mockReturnValue({
      user: { username: 'admin', roles: ['ROLE_ADMIN'] },
      token: 'valid-token'
    });

    render(
      <AuthProvider>
        <ProtectedRoute requiredRoles={['ROLE_ADMIN']}>
          <TestComponent />
        </ProtectedRoute>
      </AuthProvider>
    );

    expect(screen.getByText('Protected Content')).toBeInTheDocument();
    expect(mockNavigate).not.toHaveBeenCalled();
  });
}); 