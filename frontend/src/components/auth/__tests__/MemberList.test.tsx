import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { expect, jest, describe, beforeEach, it } from '@jest/globals';
import MemberList from '../../MemberList';
import axios from 'axios';
import '@testing-library/jest-dom';
import { AuthProvider } from '../../../context/AuthContext';
import { ToastProvider } from '../../../context/ToastContext';

// Mock axios
jest.mock('axios');
const mockedAxios = axios as jest.Mocked<typeof axios>;

// Mock auth context
jest.mock('../../../context/AuthContext', () => ({
  AuthProvider: ({ children }: { children: React.ReactNode }) => <>{children}</>,
  useAuth: () => ({
    user: { roles: ['ROLE_ADMIN'] }
  })
}));

// Mock toast context
jest.mock('../../../context/ToastContext', () => ({
  ToastProvider: ({ children }: { children: React.ReactNode }) => <>{children}</>,
  useToast: () => ({
    showToast: jest.fn()
  })
}));

const renderWithProviders = (ui: React.ReactElement) => {
  return render(
    <AuthProvider>
      <ToastProvider>
        {ui}
      </ToastProvider>
    </AuthProvider>
  );
};

describe('MemberList Component', () => {
  const mockMembers = [
    { id: '1', name: 'John Doe', email: 'john@example.com', phoneNumber: '1234567890' },
    { id: '2', name: 'Jane Smith', email: 'jane@example.com', phoneNumber: '0987654321' }
  ];

  const mockPaginatedResponse = {
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
  };

  beforeEach(() => {
    jest.clearAllMocks();
    mockedAxios.get.mockResolvedValue(mockPaginatedResponse);
  });

  describe('List Display', () => {
    it('renders member list with data', async () => {
      renderWithProviders(<MemberList />);

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
        expect(screen.getByText('Jane Smith')).toBeInTheDocument();
      });
    });

    it('shows loading state while fetching data', () => {
      renderWithProviders(<MemberList />);
      expect(screen.getByRole('progressbar')).toBeInTheDocument();
    });

    it('shows empty state message when no members', async () => {
      mockedAxios.get.mockResolvedValueOnce({
        data: { success: true, data: { content: [], totalElements: 0 } }
      });

      renderWithProviders(<MemberList />);

      await waitFor(() => {
        expect(screen.getByText(/no members found/i)).toBeInTheDocument();
      });
    });
  });

  describe('CRUD Operations', () => {
    it('handles member deletion', async () => {
      mockedAxios.delete.mockResolvedValueOnce({ data: { message: 'Deleted successfully' } });
      renderWithProviders(<MemberList />);

      await waitFor(() => {
        fireEvent.click(screen.getAllByRole('button', { name: /delete/i })[0]);
      });

      // Confirm deletion
      fireEvent.click(screen.getByRole('button', { name: /delete$/i }));

      await waitFor(() => {
        expect(mockedAxios.delete).toHaveBeenCalledWith('/api/members/1');
      });
    });

    it('handles member editing', async () => {
      mockedAxios.put.mockResolvedValueOnce({ data: { message: 'Updated successfully' } });
      renderWithProviders(<MemberList />);

      await waitFor(() => {
        fireEvent.click(screen.getAllByRole('button', { name: /edit/i })[0]);
      });

      await userEvent.type(screen.getByLabelText(/name/i), ' Updated');
      fireEvent.click(screen.getByRole('button', { name: /save/i }));

      await waitFor(() => {
        expect(mockedAxios.put).toHaveBeenCalled();
      });
    });
  });

  describe('Role-based Access', () => {
    it('hides admin actions for non-admin users', async () => {
      jest.spyOn(require('../../../context/AuthContext'), 'useAuth').mockReturnValue({
        user: { roles: ['ROLE_USER'] }
      });

      renderWithProviders(<MemberList />);

      await waitFor(() => {
        expect(screen.queryByText(/add new member/i)).not.toBeInTheDocument();
        expect(screen.queryByRole('button', { name: /edit/i })).not.toBeInTheDocument();
        expect(screen.queryByRole('button', { name: /delete/i })).not.toBeInTheDocument();
      });
    });
  });

  describe('Error Handling', () => {
    it('handles API errors gracefully', async () => {
      mockedAxios.get.mockRejectedValueOnce({
        response: { data: { message: 'Failed to fetch members' } }
      });

      renderWithProviders(<MemberList />);

      await waitFor(() => {
        expect(screen.getByText(/failed to fetch members/i)).toBeInTheDocument();
      });
    });

    it('handles network errors', async () => {
      mockedAxios.get.mockRejectedValueOnce(new Error('Network Error'));

      renderWithProviders(<MemberList />);

      await waitFor(() => {
        expect(screen.getByText(/error fetching members/i)).toBeInTheDocument();
      });
    });
  });

  describe('Refresh Functionality', () => {
    it('refreshes member list when refresh button clicked', async () => {
      renderWithProviders(<MemberList />);

      await waitFor(() => {
        fireEvent.click(screen.getByRole('button', { name: /refresh/i }));
      });

      expect(mockedAxios.get).toHaveBeenCalledTimes(2);
    });
  });
}); 