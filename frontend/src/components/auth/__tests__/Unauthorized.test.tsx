import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { expect, jest, describe, it } from '@jest/globals';
import Unauthorized from '../Unauthorized';
import { useNavigate } from 'react-router-dom';
import '@testing-library/jest-dom';

// Mock react-router-dom
jest.mock('react-router-dom', () => ({
  useNavigate: () => jest.fn()
}));

describe('Unauthorized Component', () => {
  const mockNavigate = jest.fn();

  beforeEach(() => {
    (useNavigate as jest.Mock).mockReturnValue(mockNavigate);
  });

  it('renders unauthorized message', () => {
    render(<Unauthorized />);
    
    expect(screen.getByText(/access denied/i)).toBeInTheDocument();
    expect(screen.getByText(/you do not have permission/i)).toBeInTheDocument();
  });

  it('navigates back when back button is clicked', () => {
    render(<Unauthorized />);
    
    fireEvent.click(screen.getByRole('button', { name: /go back/i }));
    expect(mockNavigate).toHaveBeenCalledWith(-1);
  });

  it('navigates to home when home button is clicked', () => {
    render(<Unauthorized />);
    
    fireEvent.click(screen.getByRole('button', { name: /go to home/i }));
    expect(mockNavigate).toHaveBeenCalledWith('/');
  });
}); 