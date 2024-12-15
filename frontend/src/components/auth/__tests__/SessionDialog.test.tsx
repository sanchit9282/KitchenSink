import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { expect, jest, describe, beforeEach, it } from '@jest/globals';
import SessionDialog from '../SessionDialog';
import '@testing-library/jest-dom';
import { ThemeProvider } from '@mui/material/styles';
import { theme } from '../../../theme';

describe('SessionDialog Component', () => {
  const mockProps = {
    open: true,
    remainingTime: 60000, // 1 minute in milliseconds
    loading: false,
    onContinue: jest.fn(),
    onLogout: jest.fn()
  };

  const renderWithTheme = (ui: React.ReactElement) => {
    return render(
      <ThemeProvider theme={theme}>
        {ui}
      </ThemeProvider>
    );
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders dialog when open', () => {
    renderWithTheme(<SessionDialog {...mockProps} />);

    expect(screen.getByRole('dialog')).toBeInTheDocument();
    expect(screen.getByText(/session expiring/i)).toBeInTheDocument();
    expect(screen.getByText(/1 minute/i)).toBeInTheDocument();
  });

  it('does not render when closed', () => {
    renderWithTheme(<SessionDialog {...mockProps} open={false} />);

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });

  it('handles continue session click', async () => {
    renderWithTheme(<SessionDialog {...mockProps} />);

    fireEvent.click(screen.getByRole('button', { name: /continue session/i }));

    expect(mockProps.onContinue).toHaveBeenCalled();
  });

  it('handles logout click', async () => {
    renderWithTheme(<SessionDialog {...mockProps} />);

    fireEvent.click(screen.getByRole('button', { name: /logout/i }));

    expect(mockProps.onLogout).toHaveBeenCalled();
  });

  it('shows loading state', () => {
    renderWithTheme(<SessionDialog {...mockProps} loading={true} />);

    expect(screen.getByRole('button', { name: /continue session/i })).toBeDisabled();
    expect(screen.getByRole('button', { name: /logout/i })).toBeDisabled();
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('formats remaining time correctly', () => {
    // Test different time formats
    const cases = [
      { time: 60000, expected: /1 minute/ },
      { time: 120000, expected: /2 minutes/ },
      { time: 30000, expected: /30 seconds/ },
      { time: 5000, expected: /5 seconds/ }
    ];

    cases.forEach(({ time, expected }) => {
      const { unmount } = renderWithTheme(
        <SessionDialog {...mockProps} remainingTime={time} />
      );
      expect(screen.getByText(expected)).toBeInTheDocument();
      unmount();
    });
  });

  it('prevents closing by clicking outside', () => {
    renderWithTheme(<SessionDialog {...mockProps} />);
    
    const dialog = screen.getByRole('dialog');
    fireEvent.click(dialog.parentElement!); // Click backdrop

    expect(dialog).toBeInTheDocument(); // Dialog should still be visible
  });

  it('prevents closing by escape key', () => {
    renderWithTheme(<SessionDialog {...mockProps} />);
    
    const dialog = screen.getByRole('dialog');
    fireEvent.keyDown(dialog, { key: 'Escape', code: 'Escape' });

    expect(dialog).toBeInTheDocument(); // Dialog should still be visible
  });
}); 