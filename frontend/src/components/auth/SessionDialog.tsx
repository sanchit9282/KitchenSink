import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Button,
  CircularProgress,
  Box
} from '@mui/material';

interface SessionDialogProps {
  open: boolean;
  remainingTime: number;
  loading: boolean;
  onContinue: () => void;
  onLogout: () => void;
}

const SessionDialog: React.FC<SessionDialogProps> = ({
  open,
  remainingTime,
  loading,
  onContinue,
  onLogout
}) => {
  return (
    <Dialog
      open={open}
      onClose={() => {}} // Prevent closing by clicking outside
      disableEscapeKeyDown
    >
      <DialogTitle>Session Expiring</DialogTitle>
      <DialogContent>
        <DialogContentText>
          Your session will expire in {Math.ceil(remainingTime / 1000)} seconds. 
          Would you like to continue your session?
        </DialogContentText>
        {loading && (
          <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
            <CircularProgress size={24} />
          </Box>
        )}
      </DialogContent>
      <DialogActions>
        <Button 
          onClick={onLogout} 
          disabled={loading}
          color="error"
        >
          Logout
        </Button>
        <Button 
          onClick={onContinue} 
          disabled={loading}
          variant="contained"
          autoFocus
        >
          Continue Session
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default SessionDialog; 