from alpaca.trading.client import TradingClient
from alpaca.data.historical import StockHistoricalDataClient
from alpaca.data.requests import StockBarsRequest
from alpaca.data.timeframe import TimeFrame
from datetime import datetime, timedelta
import pandas as pd
import time
import requests

# API credentials
API_KEY = "PKBI3NFXXW1OIC0W7O1H"
SECRET_KEY = "SIbL1e17hrKtdbw9OvmvtOEkYEopTpInS5DKY5KS"

# Initialize clients
trading_client = TradingClient(API_KEY, SECRET_KEY, paper=True)
data_client = StockHistoricalDataClient(API_KEY, SECRET_KEY)

def get_52_week_high(symbol):
    """Get 52-week high for a symbol"""
    try:
        end = datetime.now()
        start = end - timedelta(days=365)
        
        request = StockBarsRequest(
            symbol_or_symbols=[symbol],
            timeframe=TimeFrame.Day,
            start=start,
            end=end
        )
        
        bars = data_client.get_stock_bars(request)
        if bars is None:
            raise Exception(f"No data returned for {symbol}")
            
        df = bars.df
        if df.empty:
            raise Exception(f"Empty DataFrame received for {symbol}")
            
        return df.loc[(symbol,)]['high'].max()
    except Exception as e:
        print(f"Error in get_52_week_high: {str(e)}")
        print(f"DataFrame shape: {df.shape if 'df' in locals() else 'No DataFrame'}")
        print(f"DataFrame columns: {df.columns if 'df' in locals() else 'No columns'}")
        raise

def get_current_price(symbol):
    """Get current price for a symbol"""
    try:
        request = StockBarsRequest(
            symbol_or_symbols=[symbol],
            timeframe=TimeFrame.Minute,
            start=datetime.now() - timedelta(minutes=5),
            end=datetime.now()
        )
        
        bars = data_client.get_stock_bars(request)
        if bars is None:
            raise Exception(f"No current price data for {symbol}")
            
        df = bars.df
        if df.empty:
            raise Exception(f"Empty DataFrame for current price of {symbol}")
            
        return df.loc[(symbol,)]['close'].iloc[-1]
    except Exception as e:
        print(f"Error in get_current_price: {str(e)}")
        print(f"DataFrame info: {df.info() if 'df' in locals() else 'No DataFrame'}")
        raise

def get_available_options(symbol, strike_price=None):
    """Get available option contracts for a symbol"""
    try:
        url = "https://paper-api.alpaca.markets/v2/options/contracts"
        
        params = {
            "underlying_symbols": symbol,
            "type": "call",
            "style": "american",
            "status": "active"
        }
        
        if strike_price:
            params["strike_price_gte"] = strike_price - 1
            params["strike_price_lte"] = strike_price + 1

        headers = {
            "accept": "application/json",
            "APCA-API-KEY-ID": API_KEY,
            "APCA-API-SECRET-KEY": SECRET_KEY
        }

        response = requests.get(url, headers=headers, params=params)
        if response.status_code == 200:
            return response.json()
        else:
            raise Exception(f"API Error: {response.status_code} - {response.text}")
            
    except Exception as e:
        print(f"Error getting option contracts: {str(e)}")
        raise

def buy_spy_option(strike_price, expiry_date):
    """Buy SPY option"""
    try:
        # Get available options near our strike price
        options = get_available_options('SPY', strike_price)
        
        if not options or 'option_contracts' not in options:
            print("No suitable options contracts found")
            return None
            
        # Find the closest match to our desired strike and expiry
        target_date = datetime.strptime(expiry_date, '%y%m%d').strftime('%Y-%m-%d')
        
        suitable_contracts = [
            contract for contract in options['option_contracts']
            if abs(float(contract['strike_price']) - strike_price) <= 1
            and contract['expiration_date'] >= target_date
        ]
        
        if not suitable_contracts:
            print("No suitable contracts found matching criteria")
            return None
            
        # Select the first suitable contract
        contract = suitable_contracts[0]
        print(f"Found suitable contract: {contract['symbol']}")
        
        # Place the order
        order = trading_client.submit_order(
            symbol=contract['symbol'],
            qty=1,
            side='buy',
            type='market',
            time_in_force='day',
            extended_hours=False
        )
        print(f"Option order placed: {order.id}")
        return order
        
    except Exception as e:
        print(f"Error in buy_spy_option: {str(e)}")
        raise

def check_and_sell_positions(profit_target=0.05):
    """Check positions and sell if profit target is met"""
    try:
        # Get only options positions
        positions = trading_client.get_all_positions(asset_class='options')
        if not positions:
            print("No options positions found to check")
            return
            
        for position in positions:
            try:
                current_price = float(position.current_price)
                avg_entry_price = float(position.avg_entry_price)
                profit_pct = (current_price - avg_entry_price) / avg_entry_price
                
                print(f"Option Position {position.symbol}: Entry: ${avg_entry_price}, Current: ${current_price}, Profit: {profit_pct*100:.2f}%")
                
                if profit_pct >= profit_target:
                    order = trading_client.submit_order(
                        symbol=position.symbol,
                        qty=position.qty,
                        side='sell',
                        type='market',
                        time_in_force='day',
                        extended_hours=False
                    )
                    print(f"Profit target reached. Selling option position: {order.id}")
            except Exception as e:
                print(f"Error processing option position {position.symbol}: {str(e)}")
                continue
    except Exception as e:
        print(f"Error in check_and_sell_positions: {str(e)}")
        raise

def main():
    print("Starting trading bot...")
    while True:
        try:
            print("\n--- New trading cycle ---")
            current_price = get_current_price('SPY')
            print(f"Successfully got current price: ${current_price}")
            
            week_52_high = get_52_week_high('SPY')
            print(f"Successfully got 52-week high: ${week_52_high}")
            
            print(f"Current SPY Price: ${current_price}")
            print(f"52-Week High: ${week_52_high}")
            
            if current_price >= (week_52_high - 1):
                strike_price = int(current_price - 5)
                expiry_date = (datetime.now() + timedelta(days=180)).strftime('%y%m%d')
                
                print(f"SPY near 52-week high! Buying call option:")
                print(f"Strike Price: ${strike_price}")
                print(f"Expiry Date: {expiry_date}")
                
                buy_spy_option(strike_price, expiry_date)
            
            check_and_sell_positions()
            
            time.sleep(60)
            
        except Exception as e:
            print(f"Error in main loop: {str(e)}")
            print("Stack trace:", e.__traceback__)
            time.sleep(60)

if __name__ == "__main__":
    main()