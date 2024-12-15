import React, { useState, useEffect, useCallback } from 'react';
import { 
  Table, 
  TableBody, 
  TableCell, 
  TableHead, 
  TableRow, 
  IconButton, 
  Box,
  Typography,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  CircularProgress,
  DialogContentText,
  Paper
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import RefreshIcon from '@mui/icons-material/Refresh';
import { Member } from '../types/Member';
import { useAuth } from '../context/AuthContext';
import axios from 'axios';
import MemberForm from './MemberForm';
import { useToast } from '../context/ToastContext';

interface PaginatedResponse<T> {
  success: boolean;
  message: string;
  data: {
    content: T[];
    last: boolean;
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
  };
  errors: null | any[];
}

const MemberList: React.FC = () => {
  const [members, setMembers] = useState<Member[]>([]);
  const [loading, setLoading] = useState(false);
  const { user } = useAuth();
  const isAdmin = user?.roles?.includes('ROLE_ADMIN');
  const [editingMember, setEditingMember] = useState<Member | null>(null);
  const [deletingMember, setDeletingMember] = useState<Member | null>(null);
  const [editFormData, setEditFormData] = useState({
    name: '',
    email: '',
    phoneNumber: ''
  });
  const { showToast } = useToast();

  const fetchMembers = useCallback(async () => {
    try {
      setLoading(true);
      const response = await axios.get<PaginatedResponse<Member>>('/api/members');
      if (response.data?.success && Array.isArray(response.data.data.content)) {
        setMembers(response.data.data.content);
      } else {
        setMembers([]);
        showToast('Invalid data format received from server', 'error');
      }
    } catch (error: any) {
      const message = error.response?.data?.message || 'Error fetching members';
      showToast(message, 'error');
      setMembers([]);
    } finally {
      setLoading(false);
    }
  }, [showToast]);

  useEffect(() => {
    fetchMembers();
  }, [fetchMembers]);

  const handleUpdate = async (id: string, member: Omit<Member, 'id'>) => {
    try {
      setLoading(true);
      await axios.put(`/api/members/${id}`, member);
      showToast('Member updated successfully', 'success');
      fetchMembers();
    } catch (error: any) {
      const message = error.response?.data?.message || 'Error updating member';
      showToast(message, 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: string) => {
    try {
      setLoading(true);
      await axios.delete(`/api/members/${id}`);
      showToast('Member deleted successfully', 'success');
      fetchMembers();
    } catch (error: any) {
      const message = error.response?.data?.message || 'Error deleting member';
      showToast(message, 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleEditClick = (member: Member) => {
    setEditingMember(member);
    setEditFormData({
      name: member.name,
      email: member.email,
      phoneNumber: member.phoneNumber
    });
  };

  const handleEditClose = () => {
    setEditingMember(null);
  };

  const handleEditSubmit = () => {
    if (editingMember) {
      handleUpdate(editingMember.id, editFormData);
      handleEditClose();
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setEditFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleDeleteClick = (member: Member) => {
    setDeletingMember(member);
  };

  const handleDeleteCancel = () => {
    setDeletingMember(null);
  };

  const handleDeleteConfirm = () => {
    if (deletingMember) {
      handleDelete(deletingMember.id);
      setDeletingMember(null);
    }
  };

  return (
    <>
      {isAdmin && (
        <Paper sx={{ mb: 4, p: 3 }}>
          <Box sx={{ mb: 3 }}>
            <Typography variant="h5" gutterBottom color="primary">
              Add New Member
            </Typography>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              Fill in the details below to add a new member to the system.
            </Typography>
          </Box>
          <MemberForm onSuccess={fetchMembers} />
        </Paper>
      )}

      <Paper sx={{ p: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h6">Member List</Typography>
          <Button 
            startIcon={<RefreshIcon />}
            onClick={fetchMembers}
            disabled={loading}
          >
            Refresh
          </Button>
        </Box>
        
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
            <CircularProgress />
          </Box>
        ) : (
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Name</TableCell>
                <TableCell>Email</TableCell>
                <TableCell>Phone Number</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {members.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={4} align="center">
                    No members found. {isAdmin && 'Add your first member above.'}
                  </TableCell>
                </TableRow>
              ) : (
                members.map((member) => (
                  <TableRow key={member.id}>
                    <TableCell>{member.name}</TableCell>
                    <TableCell>{member.email}</TableCell>
                    <TableCell>{member.phoneNumber}</TableCell>
                    <TableCell align="right">
                      <IconButton 
                        color="primary" 
                        onClick={() => handleEditClick(member)}
                        size="small"
                        disabled={!isAdmin}
                      >
                        <EditIcon />
                      </IconButton>
                      <IconButton 
                        color="error" 
                        onClick={() => handleDeleteClick(member)}
                        size="small"
                        disabled={!isAdmin}
                      >
                        <DeleteIcon />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        )}
      </Paper>

      <Dialog open={Boolean(editingMember)} onClose={handleEditClose}>
        <DialogTitle>Edit Member</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            name="name"
            label="Name"
            type="text"
            fullWidth
            value={editFormData.name}
            onChange={handleInputChange}
          />
          <TextField
            margin="dense"
            name="email"
            label="Email"
            type="email"
            fullWidth
            value={editFormData.email}
            onChange={handleInputChange}
          />
          <TextField
            margin="dense"
            name="phoneNumber"
            label="Phone Number"
            type="tel"
            fullWidth
            value={editFormData.phoneNumber}
            onChange={handleInputChange}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleEditClose} disabled={loading}>Cancel</Button>
          <Button 
            onClick={handleEditSubmit} 
            variant="contained"
            disabled={loading}
          >
            {loading ? 'Saving...' : 'Save'}
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog
        open={Boolean(deletingMember)}
        onClose={handleDeleteCancel}
      >
        <DialogTitle>Confirm Delete</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete member "{deletingMember?.name}"? 
            This action cannot be undone.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button 
            onClick={handleDeleteCancel}
            disabled={loading}
          >
            Cancel
          </Button>
          <Button 
            onClick={handleDeleteConfirm}
            color="error"
            variant="contained"
            disabled={loading}
          >
            {loading ? 'Deleting...' : 'Delete'}
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
};

export default MemberList;

