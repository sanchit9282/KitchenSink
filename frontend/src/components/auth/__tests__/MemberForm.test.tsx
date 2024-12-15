import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { expect, jest, describe, beforeEach, it } from '@jest/globals';
import MemberForm from '../../MemberForm';
import axios from 'axios';
import '@testing-library/jest-dom';
import { ToastProvider } from '../../../context/ToastContext';

// Mock axios
jest.mock('axios');
const mockedAxios = axios as jest.Mocked<typeof axios>;

// Mock toast context
jest.mock('../../../context/ToastContext', () => ({
  ToastProvider: ({ children }: { children: React.ReactNode }) => <>{children}</>,
  useToast: () => ({
    showToast: jest.fn()
  })
}));

const renderWithProviders = (ui: React.ReactElement) => {
  return render(
    <ToastProvider>
      {ui}
    </ToastProvider>
  );
};

describe('MemberForm Component', () => {
  const mockOnSuccess = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders form fields correctly', () => {
    renderWithProviders(<MemberForm onSuccess={mockOnSuccess} />);

    expect(screen.getByLabelText(/name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/phone number/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /add member/i })).toBeInTheDocument();
  });

  it('handles successful member addition', async () => {
    mockedAxios.post.mockResolvedValueOnce({ data: { message: 'Member added successfully' } });

    renderWithProviders(<MemberForm onSuccess={mockOnSuccess} />);

    await userEvent.type(screen.getByLabelText(/name/i), 'John Doe');
    await userEvent.type(screen.getByLabelText(/email/i), 'john@example.com');
    await userEvent.type(screen.getByLabelText(/phone number/i), '1234567890');

    fireEvent.click(screen.getByRole('button', { name: /add member/i }));

    await waitFor(() => {
      expect(mockedAxios.post).toHaveBeenCalledWith('/api/members', {
        name: 'John Doe',
        email: 'john@example.com',
        phoneNumber: '1234567890'
      });
      expect(mockOnSuccess).toHaveBeenCalled();
    });
  });

  it('handles validation errors', async () => {
    renderWithProviders(<MemberForm onSuccess={mockOnSuccess} />);

    fireEvent.click(screen.getByRole('button', { name: /add member/i }));

    await waitFor(() => {
      expect(screen.getByText(/name is required/i)).toBeInTheDocument();
      expect(screen.getByText(/email is required/i)).toBeInTheDocument();
      expect(screen.getByText(/phone number is required/i)).toBeInTheDocument();
    });
  });

  it('handles API error', async () => {
    const errorMessage = 'Email already exists';
    mockedAxios.post.mockRejectedValueOnce({
      response: { data: { message: errorMessage } }
    });

    renderWithProviders(<MemberForm onSuccess={mockOnSuccess} />);

    await userEvent.type(screen.getByLabelText(/name/i), 'John Doe');
    await userEvent.type(screen.getByLabelText(/email/i), 'existing@example.com');
    await userEvent.type(screen.getByLabelText(/phone number/i), '1234567890');

    fireEvent.click(screen.getByRole('button', { name: /add member/i }));

    await waitFor(() => {
      expect(screen.getByText(errorMessage)).toBeInTheDocument();
      expect(mockOnSuccess).not.toHaveBeenCalled();
    });
  });

  it('shows loading state during submission', async () => {
    mockedAxios.post.mockImplementationOnce(
      () => new Promise(resolve => setTimeout(resolve, 100))
    );

    renderWithProviders(<MemberForm onSuccess={mockOnSuccess} />);

    await userEvent.type(screen.getByLabelText(/name/i), 'John Doe');
    await userEvent.type(screen.getByLabelText(/email/i), 'john@example.com');
    await userEvent.type(screen.getByLabelText(/phone number/i), '1234567890');

    const submitButton = screen.getByRole('button', { name: /add member/i });
    fireEvent.click(submitButton);

    expect(submitButton).toBeDisabled();
    expect(screen.getByText(/adding member/i)).toBeInTheDocument();
  });

  it('clears form after successful submission', async () => {
    mockedAxios.post.mockResolvedValueOnce({ data: { message: 'Success' } });

    renderWithProviders(<MemberForm onSuccess={mockOnSuccess} />);

    await userEvent.type(screen.getByLabelText(/name/i), 'John Doe');
    await userEvent.type(screen.getByLabelText(/email/i), 'john@example.com');
    await userEvent.type(screen.getByLabelText(/phone number/i), '1234567890');

    fireEvent.click(screen.getByRole('button', { name: /add member/i }));

    await waitFor(() => {
      expect(screen.getByLabelText(/name/i)).toHaveValue('');
      expect(screen.getByLabelText(/email/i)).toHaveValue('');
      expect(screen.getByLabelText(/phone number/i)).toHaveValue('');
    });
  });
}); 