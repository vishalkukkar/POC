# SPY Options Trading Dashboard

A real-time dashboard for monitoring SPY options trading with auto-trade capabilities. The dashboard provides live market data, options chain information, and automated trading signals based on market conditions.

## Features

- Real-time SPY price monitoring with bid/ask spread
- Dynamic options chain display with filtering
- Live account overview with portfolio value and buying power
- Auto-trade monitoring system with proximity alerts
- 52-week high tracking with visual proximity indicator
- Real-time data updates at configurable intervals

## Project Setup

### 1. Backend Setup

#### Create Python Virtual Environment
```bash
# Navigate to project directory
cd /Users/vishalkukkar/Documents/POC/Trae-test

# Create virtual environment
python -m venv venv

# Activate virtual environment (MacOS/Linux)
source venv/bin/activate

# For Windows users
.\venv\Scripts\activate
```

## Install required packages

pip install flask python-dotenv alpaca-py requests flask-cors

3. Configure environment variables:

Create .env file in the project root:


# Install all required packages
pip install flask
pip install python-dotenv
pip install alpaca-py
pip install requests
pip install flask-cors

# Alternative: Install all at once
pip install flask python-dotenv alpaca-py requests flask-cors


# Create .env file
echo "ALPACA_API_KEY=your_api_key
ALPACA_SECRET_KEY=your_secret_key" > .env

# Set file permissions (Unix/MacOS)
chmod 600 .env


# Run Flask application
# Make sure virtual environment is activated
source venv/bin/activate  # for MacOS/Linux
.\venv\Scripts\activate   # for Windows

# Run the Flask application
python api.py

# Stop the Flask application with Ctrl+C

# Deactivate virtual environment when done
deactivate


# Navigate to frontend directory
cd /Users/vishalkukkar/Documents/POC/Trae-test/spy-dashboard

# Install all dependencies
npm install

# Start React application
npm start


# Activate virtual environment
source venv/bin/activate  # Unix/MacOS
.\venv\Scripts\activate   # Windows

# Install new package
pip install package_name

# Save requirements
pip freeze > requirements.txt

# Install from requirements
pip install -r requirements.txt

# Deactivate virtual environment
deactivate


# Install new package
npm install package_name

# Run tests
npm test

# Build for production
npm run build

# Check for updates
npm outdated

# Verify Python version
python --version

# Check environment variables
echo $ALPACA_API_KEY
echo $ALPACA_SECRET_KEY

# Verify Flask installation
pip list | grep Flask

# Check logs
tail -f backend.log


# Clear npm cache
npm cache clean --force

# Remove and reinstall dependencies
rm -rf node_modules
npm install

# Check for port conflicts
lsof -i :3000

cd /Users/vishalkukkar/Documents/POC/Trae-test/spy-dashboard
npm run build

# Install production server
pip install gunicorn

# Start production server
gunicorn api:app


# Update dependencies
pip install --upgrade -r requirements.txt
npm update

# Backup environment
cp .env .env.backup

# Check logs
tail -f api.log