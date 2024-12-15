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

describe('Member Management Integration', () => {
  beforeEach(() => {
    localStorage.clear();
    jest.clearAllMocks();
    // Setup authenticated state
    localStorage.setItem('token', 'valid-token');
    localStorage.setItem('user', JSON.stringify({
      username: 'admin',
      roles: ['ROLE_ADMIN']
    }));
  });

  describe('Member List', () => {
    const mockMembers = [
      { id: '1', name: 'John Doe', email: 'john@example.com', phoneNumber: '1234567890' },
      { id: '2', name: 'Jane Smith', email: 'jane@example.com', phoneNumber: '0987654321' }
    ];

    it('loads and displays members', async () => {
      mockedAxios.get.mockResolvedValueOnce({
        data: {
          success: true,
          data: {
            content: mockMembers,
            totalElements: 2,
            totalPages: 1,
            size: 10,
            number: 0
          }
        }
      });

      renderWithProviders(<App />, { route: '/members' });

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
        expect(screen.getByText('Jane Smith')).toBeInTheDocument();
      });
    });

    it('shows loading state while fetching members', async () => {
      mockedAxios.get.mockImplementationOnce(() => 
        new Promise(resolve => setTimeout(resolve, 100))
      );

      renderWithProviders(<App />, { route: '/members' });

      expect(screen.getByRole('progressbar')).toBeInTheDocument();
    });

    it('handles empty member list', async () => {
      mockedAxios.get.mockResolvedValueOnce({
        data: {
          success: true,
          data: {
            content: [],
            totalElements: 0,
            totalPages: 0,
            size: 10,
            number: 0
          }
        }
      });

      renderWithProviders(<App />, { route: '/members' });

      await waitFor(() => {
        expect(screen.getByText(/no members found/i)).toBeInTheDocument();
      });
    });
  });

  describe('Member Creation', () => {
    it('successfully creates a new member', async () => {
      mockedAxios.post.mockResolvedValueOnce({
        data: {
          success: true,
          message: 'Member added successfully'
        }
      });

      renderWithProviders(<App />, { route: '/members' });

      // Fill member form
      await waitFor(() => {
        fireEvent.change(screen.getByLabelText(/name/i), {
          target: { value: 'New Member' }
        });
        fireEvent.change(screen.getByLabelText(/email/i), {
          target: { value: 'new@example.com' }
        });
        fireEvent.change(screen.getByLabelText(/phone number/i), {
          target: { value: '1234567890' }
        });
      });

      // Submit form
      fireEvent.click(screen.getByRole('button', { name: /add member/i }));

      await waitFor(() => {
        expect(screen.getByText(/member added successfully/i)).toBeInTheDocument();
      });
    });

    it('shows validation errors', async () => {
      renderWithProviders(<App />, { route: '/members' });

      // Submit empty form
      fireEvent.click(screen.getByRole('button', { name: /add member/i }));

      await waitFor(() => {
        expect(screen.getByText(/name is required/i)).toBeInTheDocument();
        expect(screen.getByText(/email is required/i)).toBeInTheDocument();
        expect(screen.getByText(/phone number is required/i)).toBeInTheDocument();
      });
    });
  });

  describe('Member Update', () => {
    it('successfully updates a member', async () => {
      // Mock initial member list load
      mockedAxios.get.mockResolvedValueOnce({
        data: {
          success: true,
          data: {
            content: [{ id: '1', name: 'John Doe', email: 'john@example.com', phoneNumber: '1234567890' }],
            totalElements: 1,
            totalPages: 1,
            size: 10,
            number: 0
          }
        }
      });

      // Mock update request
      mockedAxios.put.mockResolvedValueOnce({
        data: {
          success: true,
          message: 'Member updated successfully'
        }
      });

      renderWithProviders(<App />, { route: '/members' });

      // Click edit button
      await waitFor(() => {
        fireEvent.click(screen.getByRole('button', { name: /edit/i }));
      });

      // Update form
      await waitFor(() => {
        fireEvent.change(screen.getByLabelText(/name/i), {
          target: { value: 'Updated Name' }
        });
      });

      // Save changes
      fireEvent.click(screen.getByRole('button', { name: /save/i }));

      await waitFor(() => {
        expect(screen.getByText(/member updated successfully/i)).toBeInTheDocument();
      });
    });
  });

  describe('Member Deletion', () => {
    it('successfully deletes a member', async () => {
      // Mock initial member list load
      mockedAxios.get.mockResolvedValueOnce({
        data: {
          success: true,
          data: {
            content: [{ id: '1', name: 'John Doe', email: 'john@example.com', phoneNumber: '1234567890' }],
            totalElements: 1,
            totalPages: 1,
            size: 10,
            number: 0
          }
        }
      });

      // Mock delete request
      mockedAxios.delete.mockResolvedValueOnce({
        data: {
          success: true,
          message: 'Member deleted successfully'
        }
      });

      renderWithProviders(<App />, { route: '/members' });

      // Click delete button
      await waitFor(() => {
        fireEvent.click(screen.getByRole('button', { name: /delete/i }));
      });

      // Confirm deletion
      fireEvent.click(screen.getByRole('button', { name: /confirm/i }));

      await waitFor(() => {
        expect(screen.getByText(/member deleted successfully/i)).toBeInTheDocument();
      });
    });
  });
}); 