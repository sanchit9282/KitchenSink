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

describe('UI Integration', () => {
  beforeEach(() => {
    localStorage.clear();
    jest.clearAllMocks();
  });

  describe('Form Interactions', () => {
    it('shows form validation errors on submit', async () => {
      localStorage.setItem('token', 'test-token');
      localStorage.setItem('user', JSON.stringify({
        username: 'admin',
        roles: ['ROLE_ADMIN']
      }));

      renderWithProviders(<App />, { route: '/members' });

      // Submit empty form
      fireEvent.click(screen.getByRole('button', { name: /add member/i }));

      await waitFor(() => {
        expect(screen.getByText(/name is required/i)).toBeInTheDocument();
        expect(screen.getByText(/email is required/i)).toBeInTheDocument();
        expect(screen.getByText(/phone number is required/i)).toBeInTheDocument();
      });
    });

    it('disables submit button during form submission', async () => {
      localStorage.setItem('token', 'test-token');
      localStorage.setItem('user', JSON.stringify({
        username: 'admin',
        roles: ['ROLE_ADMIN']
      }));

      mockedAxios.post.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)));

      renderWithProviders(<App />, { route: '/members' });

      // Fill and submit form
      await waitFor(() => {
        fireEvent.change(screen.getByLabelText(/name/i), {
          target: { value: 'Test Name' }
        });
        fireEvent.click(screen.getByRole('button', { name: /add member/i }));
      });

      expect(screen.getByRole('button', { name: /adding member/i })).toBeDisabled();
    });
  });

  describe('Modal Interactions', () => {
    it('opens and closes edit modal', async () => {
      localStorage.setItem('token', 'test-token');
      localStorage.setItem('user', JSON.stringify({
        username: 'admin',
        roles: ['ROLE_ADMIN']
      }));

      // Mock member data
      mockedAxios.get.mockResolvedValueOnce({
        data: {
          success: true,
          data: {
            content: [{ id: '1', name: 'John Doe', email: 'john@example.com', phoneNumber: '1234567890' }],
            totalElements: 1
          }
        }
      });

      renderWithProviders(<App />, { route: '/members' });

      // Open edit modal
      await waitFor(() => {
        fireEvent.click(screen.getByRole('button', { name: /edit/i }));
      });

      expect(screen.getByRole('dialog')).toBeInTheDocument();

      // Close modal
      fireEvent.click(screen.getByRole('button', { name: /cancel/i }));
      
      await waitFor(() => {
        expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
      });
    });

    it('confirms before delete action', async () => {
      localStorage.setItem('token', 'test-token');
      localStorage.setItem('user', JSON.stringify({
        username: 'admin',
        roles: ['ROLE_ADMIN']
      }));

      // Mock member data
      mockedAxios.get.mockResolvedValueOnce({
        data: {
          success: true,
          data: {
            content: [{ id: '1', name: 'John Doe', email: 'john@example.com', phoneNumber: '1234567890' }],
            totalElements: 1
          }
        }
      });

      renderWithProviders(<App />, { route: '/members' });

      // Click delete button
      await waitFor(() => {
        fireEvent.click(screen.getByRole('button', { name: /delete/i }));
      });

      expect(screen.getByText(/are you sure/i)).toBeInTheDocument();
    });
  });

  describe('Toast Notifications', () => {
    it('shows success toast after successful action', async () => {
      localStorage.setItem('token', 'test-token');
      localStorage.setItem('user', JSON.stringify({
        username: 'admin',
        roles: ['ROLE_ADMIN']
      }));

      mockedAxios.post.mockResolvedValueOnce({
        data: { message: 'Member added successfully' }
      });

      renderWithProviders(<App />, { route: '/members' });

      // Submit form
      await waitFor(() => {
        fireEvent.change(screen.getByLabelText(/name/i), {
          target: { value: 'Test Name' }
        });
        fireEvent.click(screen.getByRole('button', { name: /add member/i }));
      });

      expect(screen.getByText(/member added successfully/i)).toBeInTheDocument();
    });

    it('stacks multiple toasts', async () => {
      localStorage.setItem('token', 'test-token');
      localStorage.setItem('user', JSON.stringify({
        username: 'admin',
        roles: ['ROLE_ADMIN']
      }));

      mockedAxios.post
        .mockRejectedValueOnce({ response: { data: { message: 'Error 1' } } })
        .mockRejectedValueOnce({ response: { data: { message: 'Error 2' } } });

      renderWithProviders(<App />, { route: '/members' });

      // Trigger multiple errors
      await waitFor(() => {
        fireEvent.click(screen.getByRole('button', { name: /add member/i }));
        fireEvent.click(screen.getByRole('button', { name: /add member/i }));
      });

      expect(screen.getByText(/error 1/i)).toBeInTheDocument();
      expect(screen.getByText(/error 2/i)).toBeInTheDocument();
    });
  });

  describe('Loading Indicators', () => {
    it('shows loading spinner during data fetch', async () => {
      localStorage.setItem('token', 'test-token');
      localStorage.setItem('user', JSON.stringify({
        username: 'admin',
        roles: ['ROLE_ADMIN']
      }));

      mockedAxios.get.mockImplementationOnce(() => 
        new Promise(resolve => setTimeout(resolve, 100))
      );

      renderWithProviders(<App />, { route: '/members' });

      expect(screen.getByRole('progressbar')).toBeInTheDocument();
    });

    it('shows loading text in buttons during action', async () => {
      localStorage.setItem('token', 'test-token');
      localStorage.setItem('user', JSON.stringify({
        username: 'admin',
        roles: ['ROLE_ADMIN']
      }));

      mockedAxios.post.mockImplementationOnce(() => 
        new Promise(resolve => setTimeout(resolve, 100))
      );

      renderWithProviders(<App />, { route: '/members' });

      // Submit form
      await waitFor(() => {
        fireEvent.click(screen.getByRole('button', { name: /add member/i }));
      });

      expect(screen.getByText(/adding\.\.\./i)).toBeInTheDocument();
    });
  });
}); 