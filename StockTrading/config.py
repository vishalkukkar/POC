import os
from dotenv import load_dotenv

# Load environment variables from .env file
load_dotenv()

# Configuration class
class Config:
    API_KEY = os.getenv('API_KEY')
    SECRET_KEY = os.getenv('SECRET_KEY')

    @staticmethod
    def validate():
        if not all([Config.API_KEY, Config.SECRET_KEY]):
            raise ValueError("Missing required environment variables. Please check your .env file.")