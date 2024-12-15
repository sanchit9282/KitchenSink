import React from 'react';
import { render, screen, act } from '@testing-library/react';
import { expect, jest, describe, beforeEach, it } from '@jest/globals';
import { AuthProvider, useAuth } from '../AuthContext';
import '@testing-library/jest-dom';

// Test component that uses the auth context
const TestComponent = () => {
  const { user, token, login, logout } = useAuth();
  return (
    <div>
      <div data-testid="user-info">{user?.username || 'No user'}</div>
      <div data-testid="token-info">{token || 'No token'}</div>
      <button onClick={() => login('test-token', { username: 'testuser', roles: ['USER'] })}>
        Login
      </button>
      <button onClick={logout}>Logout</button>
    </div>
  );
};

describe('AuthContext', () => {
  beforeEach(() => {
    localStorage.clear();
    jest.clearAllMocks();
  });

  it('provides initial authentication state', () => {
    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    expect(screen.getByTestId('user-info')).toHaveTextContent('No user');
    expect(screen.getByTestId('token-info')).toHaveTextContent('No token');
  });

  it('handles login', () => {
    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    const loginButton = screen.getByText('Login');
    act(() => {
      loginButton.click();
    });

    expect(screen.getByTestId('user-info')).toHaveTextContent('testuser');
    expect(screen.getByTestId('token-info')).toHaveTextContent('test-token');
    expect(localStorage.getItem('token')).toBe('test-token');
  });

  it('handles logout', () => {
    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    // First login
    const loginButton = screen.getByText('Login');
    act(() => {
      loginButton.click();
    });

    // Then logout
    const logoutButton = screen.getByText('Logout');
    act(() => {
      logoutButton.click();
    });

    expect(screen.getByTestId('user-info')).toHaveTextContent('No user');
    expect(screen.getByTestId('token-info')).toHaveTextContent('No token');
    expect(localStorage.getItem('token')).toBeNull();
  });

  it('loads user from localStorage on mount', () => {
    // Setup initial state in localStorage
    localStorage.setItem('token', 'saved-token');
    localStorage.setItem('user', JSON.stringify({ username: 'saveduser', roles: ['USER'] }));

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    expect(screen.getByTestId('user-info')).toHaveTextContent('saveduser');
    expect(screen.getByTestId('token-info')).toHaveTextContent('saved-token');
  });

  it('handles invalid stored data', () => {
    // Set invalid JSON in localStorage
    localStorage.setItem('user', 'invalid-json');
    localStorage.setItem('token', 'some-token');

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    expect(screen.getByTestId('user-info')).toHaveTextContent('No user');
    expect(screen.getByTestId('token-info')).toHaveTextContent('No token');
  });
}); 