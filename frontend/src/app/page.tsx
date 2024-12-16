'use client'

import { useState, useEffect, useCallback } from 'react'
import { 
  Card, 
  CardContent, 
  CardHeader, 
  Typography, 
  TextField, 
  Button, 
  Table, 
  TableBody, 
  TableCell, 
  TableHead, 
  TableRow, 
  TableContainer, 
  Paper,
  Alert,
  Snackbar,
  CircularProgress
} from '@mui/material'
import { Add, Delete, Refresh, PersonAdd } from '@mui/icons-material'
import React from 'react'

interface Member {
  _id: string
  name: string
  email: string
  phoneNumber: string
}

export default function MemberManagement() {
  const [members, setMembers] = useState<Member[]>([])
  const [loading, setLoading] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [toast, setToast] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' })
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    phoneNumber: ''
  })

  const showToast = (message: string, severity: 'success' | 'error') => {
    setToast({ open: true, message, severity })
  }

  const fetchMembers = useCallback(async () => {
    try {
      setLoading(true)
      const response = await fetch('/api/members')
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      
      const data = await response.json()
      setMembers(data)
    } catch (error) {
      console.error('Fetch error:', error)
      showToast("Failed to fetch members. Please try again.", 'error')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchMembers()
  }, [fetchMembers])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setSubmitting(true)

    try {
      const response = await fetch('/api/members', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(formData),
      })

      if (!response.ok) {
        const errorData = await response.json()
        throw new Error(errorData.error || 'Failed to add member')
      }

      const newMember = await response.json()
      showToast("Member added successfully!", 'success')
      setFormData({ name: '', email: '', phoneNumber: '' })
      setMembers(prev => [...prev, newMember])
    } catch (error) {
      console.error('Submit error:', error)
      showToast(error instanceof Error ? error.message : "Failed to add member", 'error')
    } finally {
      setSubmitting(false)
    }
  }

  const handleDelete = async (id: string) => {
    try {
      const response = await fetch(`/api/members/${id}`, {
        method: 'DELETE',
      })

      if (!response.ok) {
        throw new Error('Failed to delete member')
      }

      showToast("Member deleted successfully!", 'success')
      setMembers(prev => prev.filter(member => member._id !== id))
    } catch (error) {
      console.error('Delete error:', error)
      showToast("Failed to delete member", 'error')
    }
  }

  const testConnection = async () => {
    try {
      const response = await fetch('/api/test')
      const data = await response.json()
      if (response.ok) {
        showToast(`MongoDB connection successful. Member count: ${data.count}`, 'success')
      } else {
        throw new Error(data.error || 'Failed to connect to MongoDB')
      }
    } catch (error) {
      console.error('Test connection error:', error)
      showToast(error instanceof Error ? error.message : "Failed to test MongoDB connection", 'error')
    }
  }

  return (
    <div style={{ minHeight: '100vh', padding: '2rem', backgroundColor: '#f5f5f5' }}>
      <Card sx={{ maxWidth: 1000, margin: '0 auto' }}>
        <CardHeader 
          title="Members"
          subheader="Manage your member database with ease. Add new members and view existing ones."
        />
        <CardContent>
          <Button variant="contained" onClick={testConnection} sx={{ mb: 3 }}>
            Test MongoDB Connection
          </Button>
          
          {/* Add Member Form */}
          <Card variant="outlined" sx={{ mb: 3, p: 2, backgroundColor: '#f8f8f8' }}>
            <Typography variant="h6" sx={{ mb: 2 }}>Add New Member</Typography>
            <form onSubmit={handleSubmit}>
              <div style={{ display: 'grid', gap: '1rem', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))' }}>
                <TextField
                  label="Name"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  required
                  disabled={submitting}
                  fullWidth
                />
                <TextField
                  type="email"
                  label="Email"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  required
                  disabled={submitting}
                  fullWidth
                />
                <TextField
                  label="Phone Number"
                  value={formData.phoneNumber}
                  onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
                  required
                  disabled={submitting}
                  fullWidth
                />
              </div>
              <Button 
                type="submit" 
                variant="contained" 
                disabled={submitting}
                startIcon={submitting ? <CircularProgress size={20} /> : <PersonAdd />}
                sx={{ mt: 2 }}
              >
                {submitting ? 'Adding Member...' : 'Add Member'}
              </Button>
            </form>
          </Card>

          {/* Members List */}
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
              <Typography variant="h6">Member List</Typography>
              <Button
                variant="outlined"
                onClick={fetchMembers}
                disabled={loading}
                startIcon={loading ? <CircularProgress size={20} /> : <Refresh />}
              >
                Refresh
              </Button>
            </div>

            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Name</TableCell>
                    <TableCell>Email</TableCell>
                    <TableCell>Phone Number</TableCell>
                    <TableCell>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {loading ? (
                    <TableRow>
                      <TableCell colSpan={4} align="center" sx={{ py: 3 }}>
                        <CircularProgress />
                        <Typography variant="body2" sx={{ mt: 1, color: 'text.secondary' }}>
                          Loading members...
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ) : members.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={4} align="center" sx={{ py: 3 }}>
                        <Typography variant="body2" color="text.secondary">
                          No members found. Add your first member above.
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ) : (
                    members.map((member) => (
                      <TableRow key={member._id}>
                        <TableCell>{member.name}</TableCell>
                        <TableCell>{member.email}</TableCell>
                        <TableCell>{member.phoneNumber}</TableCell>
                        <TableCell>
                          <Button
                            variant="contained"
                            color="error"
                            size="small"
                            onClick={() => handleDelete(member._id)}
                            startIcon={<Delete />}
                          >
                            Delete
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </div>
        </CardContent>
      </Card>

      <Snackbar 
        open={toast.open} 
        autoHideDuration={6000} 
        onClose={() => setToast({ ...toast, open: false })}
      >
        <Alert severity={toast.severity} sx={{ width: '100%' }}>
          {toast.message}
        </Alert>
      </Snackbar>
    </div>
  )
}

