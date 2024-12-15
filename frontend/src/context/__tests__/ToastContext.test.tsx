import React from 'react';
import { render, screen, act } from '@testing-library/react';
import { expect, jest, describe, beforeEach, it } from '@jest/globals';
import { ToastProvider, useToast } from '../ToastContext';
import '@testing-library/jest-dom';

// Test component that uses the toast context
const TestComponent = () => {
  const { showToast } = useToast();
  return (
    <div>
      <button onClick={() => showToast('Success Message', 'success')}>
        Show Success
      </button>
      <button onClick={() => showToast('Error Message', 'error')}>
        Show Error
      </button>
      <button onClick={() => showToast('Info Message', 'info')}>
        Show Info
      </button>
    </div>
  );
};

describe('ToastContext', () => {
  beforeEach(() => {
    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.runOnlyPendingTimers();
    jest.useRealTimers();
  });

  it('shows success toast', () => {
    render(
      <ToastProvider>
        <TestComponent />
      </ToastProvider>
    );

    const successButton = screen.getByText('Show Success');
    act(() => {
      successButton.click();
    });

    expect(screen.getByText('Success Message')).toBeInTheDocument();
    expect(screen.getByRole('alert')).toHaveClass('success');
  });

  it('shows error toast', () => {
    render(
      <ToastProvider>
        <TestComponent />
      </ToastProvider>
    );

    const errorButton = screen.getByText('Show Error');
    act(() => {
      errorButton.click();
    });

    expect(screen.getByText('Error Message')).toBeInTheDocument();
    expect(screen.getByRole('alert')).toHaveClass('error');
  });

  it('automatically removes toast after timeout', () => {
    render(
      <ToastProvider>
        <TestComponent />
      </ToastProvider>
    );

    const infoButton = screen.getByText('Show Info');
    act(() => {
      infoButton.click();
    });

    expect(screen.getByText('Info Message')).toBeInTheDocument();

    act(() => {
      jest.advanceTimersByTime(3000); // Default timeout
    });

    expect(screen.queryByText('Info Message')).not.toBeInTheDocument();
  });

  it('handles multiple toasts', () => {
    const MultipleToastComponent = () => {
      const { showToast } = useToast();
      return (
        <button onClick={() => {
          showToast('First Toast', 'success');
          showToast('Second Toast', 'error');
          showToast('Third Toast', 'info');
        }}>
          Show Multiple
        </button>
      );
    };

    render(
      <ToastProvider>
        <MultipleToastComponent />
      </ToastProvider>
    );

    const button = screen.getByText('Show Multiple');
    act(() => {
      button.click();
    });

    expect(screen.getByText('First Toast')).toBeInTheDocument();
    expect(screen.getByText('Second Toast')).toBeInTheDocument();
    expect(screen.getByText('Third Toast')).toBeInTheDocument();
  });

  it('removes toasts in order', () => {
    render(
      <ToastProvider>
        <TestComponent />
      </ToastProvider>
    );

    const successButton = screen.getByText('Show Success');
    act(() => {
      successButton.click();
    });

    act(() => {
      jest.advanceTimersByTime(1500);
    });

    const errorButton = screen.getByText('Show Error');
    act(() => {
      errorButton.click();
    });

    expect(screen.getByText('Success Message')).toBeInTheDocument();
    expect(screen.getByText('Error Message')).toBeInTheDocument();

    act(() => {
      jest.advanceTimersByTime(1500);
    });

    expect(screen.queryByText('Success Message')).not.toBeInTheDocument();
    expect(screen.getByText('Error Message')).toBeInTheDocument();
  });
}); 