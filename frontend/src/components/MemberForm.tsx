import React, { useState } from 'react';
import { TextField, Button, Box, Grid } from '@mui/material';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import { useToast } from '../context/ToastContext';
import axios from 'axios';

interface MemberFormProps {
  onSuccess?: () => void;
}

const MemberForm: React.FC<MemberFormProps> = ({ onSuccess }) => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [loading, setLoading] = useState(false);
  
  const { showToast } = useToast();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      await axios.post('/api/members', { name, email, phoneNumber });
      showToast('Member added successfully', 'success');
      setName('');
      setEmail('');
      setPhoneNumber('');
      onSuccess?.();
    } catch (error: any) {
      const message = error.response?.data?.message || 'Failed to add member';
      showToast(message, 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box component="form" onSubmit={handleSubmit}>
      <Grid container spacing={2}>
        <Grid item xs={12} sm={4}>
          <TextField
            fullWidth
            label="Name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
            disabled={loading}
          />
        </Grid>
        <Grid item xs={12} sm={4}>
          <TextField
            fullWidth
            label="Email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            disabled={loading}
          />
        </Grid>
        <Grid item xs={12} sm={4}>
          <TextField
            fullWidth
            label="Phone Number"
            type="tel"
            value={phoneNumber}
            onChange={(e) => setPhoneNumber(e.target.value)}
            required
            disabled={loading}
          />
        </Grid>
        <Grid item xs={12}>
          <Button
            type="submit"
            variant="contained"
            startIcon={<PersonAddIcon />}
            disabled={loading}
          >
            {loading ? 'Adding Member...' : 'Add Member'}
          </Button>
        </Grid>
      </Grid>
    </Box>
  );
};

export default MemberForm;

