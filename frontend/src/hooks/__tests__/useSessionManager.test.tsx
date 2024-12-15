import { render } from '@testing-library/react';
import { expect, jest, describe, beforeEach, it } from '@jest/globals';
import { useSessionManager } from '../useSessionManager';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { renderHook } from '@testing-library/react';

// Mock dependencies
jest.mock('../../context/AuthContext', () => ({
  useAuth: jest.fn()
}));

jest.mock('../../context/ToastContext', () => ({
  useToast: jest.fn()
}));

const wrapper = ({ children }: { children: React.ReactNode }) => children;

describe('useSessionManager Hook', () => {
  const mockLogin = jest.fn();
  const mockLogout = jest.fn();
  const mockShowToast = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (useAuth as jest.Mock).mockReturnValue({
      token: null,
      login: mockLogin,
      logout: mockLogout
    });
    (useToast as jest.Mock).mockReturnValue({
      showToast: mockShowToast
    });
  });

  it('initializes with correct default values', () => {
    const { result } = renderHook(() => useSessionManager(), { wrapper });
    expect(result.current.showDialog).toBeFalsy();
    expect(result.current.loading).toBeFalsy();
    expect(result.current.remainingTime).toBe(0);
  });

  it('handles logout', () => {
    const { result } = renderHook(() => useSessionManager(), { wrapper });
    result.current.handleLogout();
    expect(mockShowToast).toHaveBeenCalledWith('Logging out in 3 seconds...', 'info');
  });
}); 