import React from 'react';
import { render, screen, act } from '@testing-library/react';
import { expect, jest, describe, beforeEach, it } from '@jest/globals';
import { ToastProvider, useToast } from '../../../context/ToastContext';
import '@testing-library/jest-dom';

// Test component that uses the toast
const TestComponent = () => {
  const { showToast } = useToast();
  
  return (
    <button onClick={() => showToast('Test message', 'success')}>
      Show Toast
    </button>
  );
};

describe('Toast Component', () => {
  beforeEach(() => {
    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.runOnlyPendingTimers();
    jest.useRealTimers();
  });

  it('renders success toast message', () => {
    render(
      <ToastProvider>
        <TestComponent />
      </ToastProvider>
    );

    const button = screen.getByRole('button');
    button.click();

    expect(screen.getByText('Test message')).toBeInTheDocument();
    expect(screen.getByRole('alert')).toHaveClass('success');
  });

  it('automatically closes toast after delay', () => {
    render(
      <ToastProvider>
        <TestComponent />
      </ToastProvider>
    );

    const button = screen.getByRole('button');
    button.click();

    expect(screen.getByText('Test message')).toBeInTheDocument();

    act(() => {
      jest.advanceTimersByTime(3000);
    });

    expect(screen.queryByText('Test message')).not.toBeInTheDocument();
  });

  it('renders different toast types', () => {
    const ToastTester = () => {
      const { showToast } = useToast();
      return (
        <>
          <button onClick={() => showToast('Success', 'success')}>Success</button>
          <button onClick={() => showToast('Error', 'error')}>Error</button>
          <button onClick={() => showToast('Info', 'info')}>Info</button>
          <button onClick={() => showToast('Warning', 'warning')}>Warning</button>
        </>
      );
    };

    render(
      <ToastProvider>
        <ToastTester />
      </ToastProvider>
    );

    const buttons = screen.getAllByRole('button');
    buttons.forEach(button => button.click());

    expect(screen.getByText('Success')).toBeInTheDocument();
    expect(screen.getByText('Error')).toBeInTheDocument();
    expect(screen.getByText('Info')).toBeInTheDocument();
    expect(screen.getByText('Warning')).toBeInTheDocument();
  });

  it('handles multiple toasts', () => {
    const ToastTester = () => {
      const { showToast } = useToast();
      return (
        <button onClick={() => {
          showToast('First message', 'success');
          showToast('Second message', 'error');
        }}>
          Show Multiple
        </button>
      );
    };

    render(
      <ToastProvider>
        <ToastTester />
      </ToastProvider>
    );

    const button = screen.getByRole('button');
    button.click();

    expect(screen.getByText('First message')).toBeInTheDocument();
    expect(screen.getByText('Second message')).toBeInTheDocument();
  });
}); 