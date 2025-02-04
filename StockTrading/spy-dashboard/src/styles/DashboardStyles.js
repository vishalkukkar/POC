import { styled } from '@mui/material/styles';
import { Paper, Typography, TableCell, TableRow } from '@mui/material';

export const DarkPaper = styled(Paper)(({ theme }) => ({
  padding: theme.spacing(3),
  backgroundColor: '#2A2A2A',
  color: 'white',
  borderRadius: 8,
  boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
}));

export const PriceText = styled(Typography)(({ theme, ispositive }) => ({
  fontSize: '2.5rem',
  fontWeight: 'bold',
  color: ispositive === 'true' ? '#00C805' : '#FF5000',
  marginBottom: theme.spacing(2),
}));

export const StyledTableCell = styled(TableCell)(({ theme }) => ({
  color: 'white',
  borderBottom: '1px solid rgba(255,255,255,0.1)',
  padding: '16px',
  fontSize: '0.9rem',
}));

export const StyledTableRow = styled(TableRow)(({ theme }) => ({
  '&:hover': {
    backgroundColor: 'rgba(255,255,255,0.05)',
    transition: 'background-color 0.2s',
  },
}));

export const OptionsContainer = styled(DarkPaper)(({ theme }) => ({
  marginTop: theme.spacing(3),
  overflowX: 'auto',
}));