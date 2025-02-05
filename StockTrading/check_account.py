from alpaca.trading.client import TradingClient
from alpaca.data.historical import StockHistoricalDataClient
from alpaca.data.requests import StockBarsRequest
from alpaca.data.timeframe import TimeFrame
import requests
from datetime import datetime, timedelta
from config import Config

# Initialize clients
trading_client = TradingClient(Config.API_KEY, Config.SECRET_KEY, paper=True)
data_client = StockHistoricalDataClient(Config.API_KEY, Config.SECRET_KEY)

def get_spy_52_week_high():
    try:
        url = "https://data.alpaca.markets/v2/stocks/auctions"
        
        # Calculate dates for 52-week range
        end = datetime.now()
        start = end - timedelta(days=365)
        
        params = {
            "symbols": "spy",
            "start": start.strftime("%Y-%m-%dT%H:%M:%SZ"),
            "end": end.strftime("%Y-%m-%dT%H:%M:%S.%fZ"),
            "limit": 10000,
            "feed": "sip",
            "sort": "asc"
        }
        
        headers = {
            "accept": "application/json",
            "APCA-API-KEY-ID": Config.API_KEY,
            "APCA-API-SECRET-KEY": Config.SECRET_KEY
        }

        print("Fetching 52-week data...")
        response = requests.get(url, headers=headers, params=params)
        print(f"Response status: {response.status_code}")
        print(f"SPY data: {response.text}")
        
        if response.status_code == 200:
            data = response.json()
            if 'auctions' in data and 'SPY' in data['auctions']:
                # Extract all prices from both opening and closing auctions
                prices = []
                for auction in data['auctions']['SPY']:
                    if auction.get('o'):  # Opening prices
                        prices.extend([o['p'] for o in auction['o']])
                    if auction.get('c'):  # Closing prices
                        prices.extend([c['p'] for c in auction['c']])
                
                if prices:
                    high_price = max(prices)
                    print(f"\nSPY 52-Week High: ${high_price:.2f}")
                    return high_price
                else:
                    print("No price data available")
            else:
                print("No auction data available for SPY")
        else:
            print(f"Error: {response.status_code} - {response.text}")
            
    except Exception as e:
        print(f"Error getting SPY 52-week high: {e}")
        return None

def get_spy_options():
    try:
        url = "https://paper-api.alpaca.markets/v2/options/contracts"
        
        params = {
            "underlying_symbols": "SPY",
            "status": "active",
            "type": "put",
            "style": "american",
            "expiration_date": "2025-06-20",
            "limit": 100,
            "show_deliverables": True
        }

        headers = {
            "accept": "application/json",
            "APCA-API-KEY-ID": Config.API_KEY,
            "APCA-API-SECRET-KEY": Config.SECRET_KEY
        }

        print("Making request to options API...")
        print(f"URL: {url}")
        print(f"Headers: {headers}")
        print(f"Params: {params}")
        
        response = requests.get(url, headers=headers, params=params)
        print(f"Options API response status: {response.status_code}")
        print(f"Options API response: {response.text}")
        
        if response.status_code == 200:
            options_data = response.json()
            print("\n=== SPY PUT Options for June 20, 2025 ===")
            for contract in options_data.get('option_contracts', []):
                print(f"\nContract: {contract['symbol']}")
                print(f"Name: {contract['name']}")
                print(f"Strike Price: ${contract['strike_price']}")
                print(f"Type: {contract['type'].upper()}")
                print(f"Style: {contract['style'].capitalize()}")
                if 'close_price' in contract:
                    print(f"Last Close Price: ${contract['close_price']}")
                print("-" * 50)
        else:
            print(f"Error: {response.status_code} - {response.text}")
            
    except Exception as e:
        print(f"Error fetching options data: {e}")

def get_spy_price():
    try:
        url = "https://data.alpaca.markets/v2/stocks/quotes/latest"
        
        params = {
            "symbols": "SPY",
            "feed": "iex"  # Add IEX feed parameter
        }
        
        headers = {
            "accept": "application/json",
            "APCA-API-KEY-ID": API_KEY,
            "APCA-API-SECRET-KEY": SECRET_KEY
        }

        response = requests.get(url, headers=headers, params=params)
        
        if response.status_code == 200:
            data = response.json()
            if 'quotes' in data and 'SPY' in data['quotes']:
                quote = data['quotes']['SPY']
                current_price = (quote['ap'] + quote['bp']) / 2
                print(f"\nSPY Current Price: ${current_price:.2f}")
                print(f"Bid Price: ${quote['bp']:.2f}")
                print(f"Ask Price: ${quote['ap']:.2f}")
                print(f"Last Update: {quote['t']}")
                return current_price
            else:
                print("No quote data available for SPY")
        else:
            print(f"Error: {response.status_code} - {response.text}")
            
    except Exception as e:
        print(f"Error getting SPY price: {e}")

def check_account():
    try:
        account = trading_client.get_account()
        return {
            "status": account.status,
            "buying_power": account.buying_power,
            "cash": account.cash,
            "portfolio_value": account.portfolio_value,
            "trading_blocked": account.trading_blocked,
            "equity": account.equity,
            "last_equity": account.last_equity,
            "balance_change": float(account.equity) - float(account.last_equity)
        }
    except Exception as e:
        print(f"Error getting account: {str(e)}")
        return None

def get_positions():
    try:
        positions = trading_client.get_all_positions()
        return [{
            "symbol": position.symbol,
            "quantity": position.qty,
            "avg_entry_price": position.avg_entry_price,
            "current_price": position.current_price,
            "market_value": position.market_value,
            "unrealized_pl": position.unrealized_pl
        } for position in positions]
    except Exception as e:
        print(f"Error getting positions: {str(e)}")
        return []

def get_orders():
    try:
        get_orders_data = GetOrdersRequest(
            status=QueryOrderStatus.ALL,  # Get all orders including open ones
            limit=100,
            nested=True
        )
        orders = trading_client.get_orders(filter=get_orders_data)
        return [{
            "id": order.id,
            "symbol": order.symbol,
            "qty": order.qty,
            "side": order.side,
            "type": order.type,
            "status": order.status,
            "submitted_at": order.submitted_at,
            "filled_at": order.filled_at,
            "filled_qty": order.filled_qty,
            "filled_avg_price": order.filled_avg_price,
            "order_class": order.order_class
        } for order in orders]
    except Exception as e:
        print(f"Error getting orders: {str(e)}")
        return []

# Update main to include the new function
if __name__ == "__main__":
    check_account()
    get_spy_price()
    get_spy_52_week_high()
    get_spy_options()