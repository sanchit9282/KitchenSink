import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, Typography, Button, Paper } from '@mui/material';
import BlockIcon from '@mui/icons-material/Block';

const Unauthorized: React.FC = () => {
  const navigate = useNavigate();

  return (
    <Box sx={{ 
      display: 'flex', 
      justifyContent: 'center', 
      alignItems: 'center', 
      minHeight: '100vh' 
    }}>
      <Paper sx={{ p: 4, maxWidth: 400, width: '100%', textAlign: 'center' }}>
        <BlockIcon color="error" sx={{ fontSize: 60, mb: 2 }} />
        <Typography variant="h5" component="h1" gutterBottom>
          Access Denied
        </Typography>
        <Typography variant="body1" color="text.secondary" paragraph>
          You don't have permission to access this page. Please contact your administrator if you think this is a mistake.
        </Typography>
        <Button
          variant="contained"
          onClick={() => navigate('/members')}
          sx={{ mr: 2 }}
        >
          Go to Members
        </Button>
        <Button
          variant="outlined"
          onClick={() => navigate('/login')}
        >
          Login as Different User
        </Button>
      </Paper>
    </Box>
  );
};

export default Unauthorized; 