import os
from dotenv import load_dotenv

# Load environment variables from .env file
load_dotenv()

# Configuration class
class Config:
    ALPACA_API_KEY = os.getenv('ALPACA_API_KEY')
    ALPACA_SECRET_KEY = os.getenv('ALPACA_SECRET_KEY')

    @staticmethod
    def validate():
        if not all([Config.ALPACA_API_KEY, Config.ALPACA_SECRET_KEY]):
            raise ValueError("Missing required environment variables. Please check your .env file.")