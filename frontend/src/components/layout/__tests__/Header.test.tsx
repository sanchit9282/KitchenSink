import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { expect, jest, describe, beforeEach, it } from '@jest/globals';
import Header from '../Header';
import { useAuth } from '../../../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import '@testing-library/jest-dom';

// Mock dependencies
jest.mock('../../../context/AuthContext', () => ({
  useAuth: jest.fn()
}));

jest.mock('react-router-dom', () => ({
  useNavigate: () => jest.fn(),
  Link: ({ children, to }: { children: React.ReactNode; to: string }) => (
    <a href={to}>{children}</a>
  )
}));

describe('Header Component', () => {
  const mockNavigate = jest.fn();
  const mockLogout = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (useNavigate as jest.Mock).mockReturnValue(mockNavigate);
  });

  it('renders logo and navigation for unauthenticated users', () => {
    (useAuth as jest.Mock).mockReturnValue({
      user: null,
      logout: mockLogout
    });

    render(<Header />);

    expect(screen.getByText(/kitchen sink/i)).toBeInTheDocument();
    expect(screen.getByText(/login/i)).toBeInTheDocument();
    expect(screen.getByText(/register/i)).toBeInTheDocument();
  });

  it('renders user menu for authenticated users', () => {
    (useAuth as jest.Mock).mockReturnValue({
      user: { username: 'testuser', roles: ['USER'] },
      logout: mockLogout
    });

    render(<Header />);

    expect(screen.getByText('testuser')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /user menu/i })).toBeInTheDocument();
  });

  it('shows admin links for admin users', () => {
    (useAuth as jest.Mock).mockReturnValue({
      user: { username: 'admin', roles: ['ADMIN'] },
      logout: mockLogout
    });

    render(<Header />);

    expect(screen.getByText(/admin/i)).toBeInTheDocument();
  });

  it('handles logout', () => {
    (useAuth as jest.Mock).mockReturnValue({
      user: { username: 'testuser', roles: ['USER'] },
      logout: mockLogout
    });

    render(<Header />);

    fireEvent.click(screen.getByRole('button', { name: /user menu/i }));
    fireEvent.click(screen.getByText(/logout/i));

    expect(mockLogout).toHaveBeenCalled();
  });

  it('navigates to correct routes when links are clicked', () => {
    (useAuth as jest.Mock).mockReturnValue({
      user: null,
      logout: mockLogout
    });

    render(<Header />);

    fireEvent.click(screen.getByText(/login/i));
    expect(mockNavigate).toHaveBeenCalledWith('/login');

    fireEvent.click(screen.getByText(/register/i));
    expect(mockNavigate).toHaveBeenCalledWith('/register');
  });
}); 