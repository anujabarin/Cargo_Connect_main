#  Setup Database

import os
import sys
import psycopg2
from dotenv import load_dotenv
from pathlib import Path

# Ensure the script can find other modules in the project
sys.path.append(os.path.abspath(os.path.dirname(__file__)))

# Direct path to .env file to ensure it's loaded correctly
env_path = Path(".") / ".env"
print(f"Loading environment from: {os.path.abspath(env_path)}")
load_dotenv(dotenv_path=env_path)

def create_database_if_not_exists():
    """Create the database if it doesn't exist"""
    db_name = os.getenv("DB_NAME")
    if not db_name:
        print("ERROR: DB_NAME environment variable not set")
        sys.exit(1)
        
    # Connect to postgres to check/create database
    try:
        conn = psycopg2.connect(
            host=os.getenv("DB_HOST", "localhost"),
            port=os.getenv("DB_PORT", "5432"),
            user=os.getenv("DB_USER", "postgres"),
            password=os.getenv("DB_PASSWORD", ""),
            database="postgres"
        )
        conn.autocommit = True
        cursor = conn.cursor()
        
        # Check if the database exists
        cursor.execute(f"SELECT 1 FROM pg_database WHERE datname = %s", (db_name,))
        if not cursor.fetchone():
            print(f"Creating database '{db_name}'...")
            cursor.execute(f"CREATE DATABASE {db_name}")
            print(f"Database '{db_name}' created successfully.")
        else:
            print(f"Database '{db_name}' already exists.")
            
        cursor.close()
        conn.close()
        return True
    except Exception as e:
        print(f"Error checking/creating database: {e}")
        return False

def setup_database():
    """Sets up the Cargo Connect database by executing SQL files"""
    print("Setting up Cargo Connect database...")
    
    if not create_database_if_not_exists():
        sys.exit(1)
    
    # Database connection parameters - get directly from env
    db_name = os.getenv("DB_NAME")
    db_host = os.getenv("DB_HOST", "localhost")
    db_port = os.getenv("DB_PORT", "5432")
    db_user = os.getenv("DB_USER", "postgres")
    db_pass = os.getenv("DB_PASSWORD", "")
    
    print(f"Connecting to database '{db_name}' on {db_host}:{db_port} as user '{db_user}'...")
    
    try:
        # Connect to the database
        conn = psycopg2.connect(
            host=db_host,
            port=db_port,
            user=db_user,
            password=db_pass,
            database=db_name
        )
        conn.autocommit = True
        cursor = conn.cursor()
        
        # Execute SQL files
        sql_dir = Path("sql")
        
        # First create users table
        users_sql_path = sql_dir / "users.sql"
        if users_sql_path.exists():
            print(f"Executing {users_sql_path}...")
            with open(users_sql_path, 'r') as f:
                cursor.execute(f.read())
            print("Users table setup complete.")
        else:
            print(f"Warning: {users_sql_path} not found.")
        
        # Then create cargo_schedule table
        cargo_sql_path = sql_dir / "cargo_schedule.sql"
        if cargo_sql_path.exists():
            print(f"Executing {cargo_sql_path}...")
            with open(cargo_sql_path, 'r') as f:
                cursor.execute(f.read())
            print("Cargo schedule table setup complete.")
        else:
            print(f"Warning: {cargo_sql_path} not found.")
        
        cursor.close()
        conn.close()
        
        print("Database setup completed successfully.")
        
    except Exception as e:
        print(f"Database setup failed: {e}")
        sys.exit(1)

if __name__ == "__main__":
    setup_database()
    
    print("""
Database setup complete!
    
You can now run the application with:
    python app.py
    """)