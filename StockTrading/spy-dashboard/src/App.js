import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { 
  Container, 
  Grid, 
  Typography,
  Box,
  Table,
  TableBody,
  TableHead,
  TableContainer,
  Chip,
  CircularProgress
} from '@mui/material';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import TrendingDownIcon from '@mui/icons-material/TrendingDown';
import {
  DarkPaper,
  PriceText,
  StyledTableCell,
  StyledTableRow,
  OptionsContainer
} from './styles/DashboardStyles';
import barStyles from './styles/BarStyles.module.css'; // Import CSS Module

function App() {
  const [state, setState] = useState({
    accountData: { data: null, loading: true, error: null },
    spyData: { data: null, loading: true, error: null },
    options: { data: [], loading: true, error: null },
    spyLiveData: { data: null, loading: true, error: null },
    autoTrade: { data: [], loading: false, error: null }  // Add new state
  });

  const fetchData = async (endpoint, stateKey, transformer = null) => {
    try {
      const response = await axios.get(`http://localhost:5002/api/${endpoint}`);
      const transformedData = transformer ? transformer(response.data) : response.data;
      
      setState(prev => ({
        ...prev,
        [stateKey]: {
          data: transformedData,
          loading: false,
          error: null
        }
      }));
    } catch (error) {
      console.error(`Error fetching ${endpoint}:`, error);
      setState(prev => ({
        ...prev,
        [stateKey]: {
          ...prev[stateKey],
          loading: false,
          error: `Failed to load ${stateKey}`
        }
      }));
    }
  };

  const fetchAccountData = () => fetchData('account', 'accountData');
  const fetchLiveData = () => fetchData('spy/live', 'spyLiveData');
  const fetchOptionsData = () => fetchData('spy/options', 'options', data => data.option_contracts);

  // Add new function to check and place trades
  // Update checkAutoTrade function to store all response data
  const checkAutoTrade = async () => {
    try {
      const response = await axios.get('http://localhost:5002/api/spy/auto-trade');
      setState(prev => ({
        ...prev,
        autoTrade: {
          ...prev.autoTrade,
          data: response.data
        }
      }));
    } catch (error) {
      console.error('Auto-trade check failed:', error);
    }
  };

  useEffect(() => {
    const fetchAllData = async () => {
      await Promise.all([
        fetchLiveData(),
        fetchOptionsData(),
        fetchAccountData(),
        checkAutoTrade()
      ]);
    };

    fetchAllData();

    const intervals = [
      { fn: fetchLiveData, time: 5000 },
      { fn: fetchOptionsData, time: 30000 },
      { fn: fetchAccountData, time: 10000 },
      { fn: checkAutoTrade, time: 30000 }
    ].map(({ fn, time }) => setInterval(fn, time));

    return () => intervals.forEach(clearInterval);
  }, []);

  // Keep existing LoadingState and ErrorState components
  const LoadingState = () => (
    <Box display="flex" justifyContent="center" alignItems="center" p={3}>
      <CircularProgress color="inherit" />
    </Box>
  );

  const ErrorState = ({ message }) => (
    <Typography color="error" align="center">{message}</Typography>
  );

  // Keep existing renderSpyProximityBar function
  const renderSpyProximityBar = () => {
    if (!state.spyLiveData.data) return null;

    const { current_price, week_52_high } = state.spyLiveData.data;
    const difference = week_52_high - current_price;
    let barColor = 'red';

    if (difference < 5) {
      barColor = 'green';
    } else if (difference < 10) {
      barColor = 'yellow';
    }

    const barWidth = ((week_52_high - difference) / week_52_high) * 100;

    return (
      <div className={barStyles.barContainer}>
        <div
          className={barStyles.bar}
          style={{ width: `${barWidth}%`, backgroundColor: barColor }}
        />
      </div>
    );
  };

  // Add new render function for auto-trade section
  // Update the renderAutoTrades function
  const renderAutoTrades = () => {
    const tradeData = state.autoTrade.data;
  
    return (
      <Grid item xs={12} md={4}>
        <DarkPaper>
          <Typography variant="h6" gutterBottom>Auto-Trade Monitor</Typography>
          {tradeData ? (
            <Box sx={{ p: 2, bgcolor: 'rgba(255,255,255,0.05)', borderRadius: 1 }}>
              <Grid container spacing={2}>
                <Grid item xs={6}>
                  <Typography variant="body2" color="gray">Current Price</Typography>
                  <Typography variant="h6" sx={{ color: '#fff' }}>
                    ${tradeData.current_price?.toFixed(2) || 'N/A'}
                  </Typography>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="body2" color="gray">52-Week High</Typography>
                  <Typography variant="h6" sx={{ color: '#fff' }}>
                    {tradeData.week_52_high ? 
                      `$${tradeData.week_52_high.toFixed(2)}` : 
                      'Calculating...'}
                  </Typography>
                </Grid>
              </Grid>
              <Box sx={{ mt: 2 }}>
                <Typography variant="body2" color="gray">Distance to 52W High</Typography>
                <Typography sx={{ color: '#fff' }}>
                  {tradeData.week_52_high && tradeData.current_price ? 
                    `$${(tradeData.week_52_high - tradeData.current_price).toFixed(2)}` : 
                    'Calculating...'}
                </Typography>
              </Box>
              <Box sx={{ 
                mt: 2, 
                p: 1, 
                bgcolor: tradeData.error ? 'rgba(255,80,0,0.1)' : 
                            tradeData.should_trade ? 'rgba(0,200,5,0.2)' : 
                            'rgba(255,80,0,0.2)', 
                borderRadius: 1,
                border: `1px solid ${tradeData.error ? '#FF5000' : 
                            tradeData.should_trade ? '#00C805' : '#FF5000'}`
              }}>
                <Typography variant="body2" sx={{ 
                  color: tradeData.error ? '#FF5000' : 
                         tradeData.should_trade ? '#00C805' : '#FF5000' 
                }}>
                  {tradeData.error ? `⚠️ ${tradeData.error}` :
                   tradeData.should_trade ? '✓ Ready to Trade' : 
                   '⏳ Waiting for optimal conditions'}
                </Typography>
              </Box>
            </Box>
          ) : (
            <Typography color="gray">
              Loading trade conditions...
            </Typography>
          )}
        </DarkPaper>
      </Grid>
    );
  };

  return (
    <Box sx={{ backgroundColor: '#1A1A1A', minHeight: '100vh', py: 3 }}>
      <Container>
        <Grid container spacing={3}>
          {/* Account Overview */}
          <Grid item xs={12} md={4}>
            <DarkPaper>
              <Typography variant="h6" gutterBottom>Account Overview</Typography>
              {state.accountData.loading ? <LoadingState /> : 
               state.accountData.error ? <ErrorState message={state.accountData.error} /> :
               state.accountData.data && (
                <>
                  <PriceText ispositive={(state.accountData.data.balance_change >= 0).toString()}>
                    ${parseFloat(state.accountData.data.portfolio_value).toLocaleString()}
                  </PriceText>
                  <Box display="flex" alignItems="center" mb={2}>
                    {state.accountData.data.balance_change >= 0 ? 
                      <TrendingUpIcon sx={{ color: '#00C805' }} /> : 
                      <TrendingDownIcon sx={{ color: '#FF5000' }} />}
                    <Typography sx={{ ml: 1 }}>
                      ${parseFloat(state.accountData.data.balance_change).toLocaleString()}
                    </Typography>
                  </Box>
                  <Typography variant="body2">
                    Buying Power: ${parseFloat(state.accountData.data.buying_power).toLocaleString()}
                  </Typography>
                </>
              )}
            </DarkPaper>
          </Grid>

          {/* SPY Live Data */}
          <Grid item xs={12} md={4}>
            <DarkPaper>
              <Typography variant="h6" gutterBottom>SPY Live Data</Typography>
              {state.spyLiveData.loading ? <LoadingState /> :
               state.spyLiveData.error ? <ErrorState message={state.spyLiveData.error} /> :
               state.spyLiveData.data && (
                <>
                  <PriceText ispositive={(state.spyLiveData.data.current_price > 0).toString()}>
                    ${state.spyLiveData.data.current_price?.toFixed(2) || 'N/A'}
                  </PriceText>
                  <Grid container spacing={2}>
                    <Grid item xs={6}>
                      <Typography variant="body2">Bid</Typography>
                      <Typography>
                        ${state.spyLiveData.data.bid_price?.toFixed(2) || 'N/A'}
                      </Typography>
                    </Grid>
                    <Grid item xs={6}>
                      <Typography variant="body2">Ask</Typography>
                      <Typography>
                        ${state.spyLiveData.data.ask_price?.toFixed(2) || 'N/A'}
                      </Typography>
                    </Grid>
                  </Grid>
                  <Typography variant="body2" sx={{ mt: 2 }}>
                    52-Week High: ${state.spyLiveData.data.week_52_high?.toFixed(2) || 'Calculating...'}
                  </Typography>
                  {state.spyLiveData.data.week_52_high && state.spyLiveData.data.current_price && renderSpyProximityBar()}
                </>
              )}
            </DarkPaper>
          </Grid>

          {/* Auto-Trade Monitor */}
          {renderAutoTrades()}

          {/* Options Chain */}
          <Grid item xs={12}>
            <OptionsContainer>
              <Typography variant="h6" gutterBottom>Options Chain</Typography>
              {state.options.loading ? <LoadingState /> :
               state.options.error ? <ErrorState message={state.options.error} /> :
               state.options.data && (
                <TableContainer>
                  <Table>
                    <TableHead>
                      <StyledTableRow>
                        <StyledTableCell>Symbol</StyledTableCell>
                        <StyledTableCell>Type</StyledTableCell>
                        <StyledTableCell>Strike</StyledTableCell>
                        <StyledTableCell>Expiration</StyledTableCell>
                        <StyledTableCell>Last Price</StyledTableCell>
                        <StyledTableCell>Open Interest</StyledTableCell>
                      </StyledTableRow>
                    </TableHead>
                    <TableBody>
                      {state.options.data.map((option) => (
                        <StyledTableRow key={option.id}>
                          <StyledTableCell>{option.symbol}</StyledTableCell>
                          <StyledTableCell>
                            <Chip
                              label={option.type.toUpperCase()}
                              size="small"
                              sx={{
                                backgroundColor: option.type === 'call' ? '#00C805' : '#FF5000',
                                color: 'white',
                              }}
                            />
                          </StyledTableCell>
                          <StyledTableCell>${option.strike_price}</StyledTableCell>
                          <StyledTableCell>
                            {option.expiration_date}
                          </StyledTableCell>
                          <StyledTableCell>
                            ${parseFloat(option.close_price).toFixed(2)}
                          </StyledTableCell>
                          <StyledTableCell>
                            {parseInt(option.open_interest).toLocaleString()}
                          </StyledTableCell>
                        </StyledTableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              )}
            </OptionsContainer>
          </Grid>
        </Grid>
      </Container>
    </Box>
  );
}

export default App;
