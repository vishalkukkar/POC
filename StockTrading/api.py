from flask import Flask, jsonify
from flask_cors import CORS
import requests
import uuid
from datetime import datetime

app = Flask(__name__)
CORS(app)

# Add API credentials
API_KEY = "PKBI3NFXXW1OIC0W7O1H"
SECRET_KEY = "SIbL1e17hrKtdbw9OvmvtOEkYEopTpInS5DKY5KS"

from check_account import check_account, get_spy_price, get_spy_52_week_high, get_spy_options

app = Flask(__name__)
CORS(app)

@app.route('/', methods=['GET'])
def home():
    return jsonify({
        "message": "Welcome to SPY Options Trading API",
        "version": "1.0"
    })

@app.route('/api/account', methods=['GET'])
def get_account():
    try:
        account = check_account()
        if account is None:
            return jsonify({"error": "Failed to fetch account data"}), 500
            
        return jsonify(account)
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/spy/data', methods=['GET'])
def get_spy_data():
    try:
        current_price = get_spy_price()
        week_52_high = get_spy_52_week_high()
        return jsonify({
            "current_price": current_price,
            "week_52_high": week_52_high
        })
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/spy/options', methods=['GET'])
def get_options_data():
    try:
        # First get current SPY price
        spy_quote_url = "https://data.alpaca.markets/v2/stocks/quotes/latest"
        spy_params = {"symbols": "SPY", "feed": "iex"}
        headers = {
            "accept": "application/json",
            "APCA-API-KEY-ID": API_KEY,
            "APCA-API-SECRET-KEY": SECRET_KEY
        }
        
        spy_response = requests.get(spy_quote_url, headers=headers, params=spy_params)
        if spy_response.status_code != 200:
            return jsonify({"error": "Failed to fetch SPY price"}), 500
            
        spy_data = spy_response.json()
        current_spy_price = spy_data['quotes']['SPY']['ap']
        min_strike_price = current_spy_price - 20
        
        # Now fetch options
        options_url = "https://paper-api.alpaca.markets/v2/options/contracts"
        options_params = {
            "underlying_symbols": "SPY",
            "status": "active",
            "type": "put",
            "style": "american",
            "expiration_date": "2025-06-20",
            "limit": 100
        }
        
        response = requests.get(options_url, headers=headers, params=options_params)
        if response.status_code == 200:
            data = response.json()
            # Filter options based on strike price range
            filtered_options = [
                opt for opt in data['option_contracts']
                if min_strike_price <= float(opt['strike_price']) <= current_spy_price
            ]
            # Sort by strike price ascending
            filtered_options.sort(key=lambda x: float(x['strike_price']))
            return jsonify({"option_contracts": filtered_options})
            
        return jsonify({"error": "Failed to fetch options data"}), 500
            
    except Exception as e:
        print("Exception:", str(e))
        return jsonify({"error": str(e)}), 500

@app.route('/api/spy/auto-trade', methods=['GET'])
def check_and_place_trade():
    try:
        # Get current SPY data
        url = "https://data.alpaca.markets/v2/stocks/quotes/latest"
        params = {"symbols": "SPY", "feed": "iex"}
        headers = {
            "accept": "application/json",
            "APCA-API-KEY-ID": API_KEY,
            "APCA-API-SECRET-KEY": SECRET_KEY
        }
        
        response = requests.get(url, headers=headers, params=params)
        if response.status_code != 200:
            return jsonify({"error": "Failed to fetch SPY data"}), 500
            
        data = response.json()
        current_price = data['quotes']['SPY']['ap']
        week_52_high = get_spy_52_week_high()
        
        # Check if we have valid data
        if week_52_high is None:
            return jsonify({
                'should_trade': False,
                'current_price': current_price,
                'week_52_high': None,
                'error': 'Could not fetch 52-week high'
            })

        # Rest of your trading logic
        should_trade = current_price >= (week_52_high - 1)
        
        if should_trade:
            # Calculate strike price (current price - 10, rounded to nearest 5)
            strike_price = round((current_price - 10) / 5) * 5

            # Get options data
            options_url = "https://paper-api.alpaca.markets/v2/options/contracts"
            options_params = {
                "underlying_symbols": "SPY",
                "status": "active",
                "type": "put",
                "style": "american",
                "expiration_date": "2025-06-20",
                "limit": 100
            }
            
            options_response = requests.get(options_url, headers=headers, params=options_params)
            if options_response.status_code != 200:
                return jsonify({"error": "Failed to fetch options data"}), 500

            options_data = options_response.json()
            target_option = next(
                (opt for opt in options_data['option_contracts'] 
                 if opt['type'] == 'put' 
                 and float(opt['strike_price']) == strike_price
                 and opt['expiration_date'] == '2025-06-20'),
                None
            )

            if target_option:
                order = {
                    'id': str(uuid.uuid4()),
                    'symbol': target_option['symbol'],
                    'type': 'put',
                    'strike_price': strike_price,
                    'expiration_date': '2025-06-20',
                    'order_time': datetime.now().isoformat(),
                    'current_spy_price': current_price,
                    'week_52_high': week_52_high
                }
                
                return jsonify({
                    'should_trade': True,
                    'order': order,
                    'current_price': current_price,
                    'week_52_high': week_52_high
                })

        return jsonify({
            'should_trade': False,
            'current_price': current_price,
            'week_52_high': week_52_high
        })

    except Exception as e:
        print("Auto-trade error:", str(e))
        return jsonify({'error': str(e)}), 500

@app.route('/api/spy/live', methods=['GET'])
def get_spy_live_data():
    try:
        url = "https://data.alpaca.markets/v2/stocks/quotes/latest"
        params = {
            "symbols": "SPY",
            "feed": "iex"
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
                week_52_high = get_spy_52_week_high()  # Add this line
                return jsonify({
                    "current_price": quote['ap'],
                    "bid_price": quote['bp'],
                    "ask_price": quote['ap'],
                    "timestamp": quote['t'],
                    "week_52_high": week_52_high  # Add this line
                })
        return jsonify({"error": "Unable to fetch SPY data"}), 500
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/positions', methods=['GET'])
def get_positions_data():
    try:
        positions = get_positions()
        return jsonify({"positions": positions})
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/orders', methods=['GET'])
def get_orders_data():
    try:
        orders = get_orders()
        return jsonify({"orders": orders})
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True, port=5002)  # Changed port to 5002