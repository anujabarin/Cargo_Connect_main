# Cargo Connect Backend

A Flask-based backend service for the Cargo Connect application, providing cargo scheduling and user authentication functionality.

## Table of Contents
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Project Setup](#project-setup)
- [Database Setup](#database-setup)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [Development Guidelines](#development-guidelines)

## Features

- **Authentication System**: User registration, and login
- **Cargo Scheduling**: Retrieve cargo schedules and details
- **PostgreSQL Database Integration**: Full database support with seeding capabilities
- **Test Coverage**: Unit, integration, and end-to-end tests

## Prerequisites

- Python 3.8+ installed
- PostgreSQL 12+ installed and running
- pip (Python package manager)
- Git (optional, for version control)

## Project Setup

1. Clone the repository (if using Git):
   ```
   git clone <repository-url>
   cd cargo_connect_backend
   ```

2. Create and activate a virtual environment (recommended):
   ```
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   ```

3. Install required packages:
   ```
   pip install -r requirements.txt
   ```

## Database Setup

The project uses PostgreSQL for data storage. You can set up the database in two ways:

### Option 1: Automatic Setup (Recommended)

1. Create a `.env` file with your PostgreSQL credentials (see [Configuration](#configuration))

2. Run the database setup script:
   ```
   python script.py
   ```
   
   This will:
   - Create the database if it doesn't exist
   - Set up all required tables (users, cargo_schedule)
   - Create necessary indexes
   - Add a demo user for testing

3. Seed the database with data to use Mobile Application:
   ```
   python seed_data.py
   ```

### Option 2: Manual Setup

1. Connect to PostgreSQL:
   ```
   psql -U your_username -d postgres
   ```

2. Create the database:
   ```sql
   CREATE DATABASE cargo_db;
   ```

3. Connect to the new database:
   ```
   \c cargo_db
   ```

4. Execute the SQL files in this order:
   ```
   \i sql/users.sql
   \i sql/cargo_schedule.sql
   ```

## Configuration

Create a `.env` file in the root directory with the following variables:

```
DB_HOST=localhost
DB_PORT=5432
DB_NAME=cargo_db
DB_USER=your_postgres_username
DB_PASSWORD=your_postgres_password
JWT_SECRET=some_secure_random_string
```

## Running the Application

Start the server with:
```
python app.py
```

The API will be available at `http://localhost:8800`.

## API Documentation

### Authentication Endpoints

- **POST /auth/register** - Register a new user
  - Body: `{"email": "user@example.com", "password": "securepass", "fullName": "User Name"}`
  - Response: User data and JWT token

- **POST /auth/login** - Login with credentials
  - Body: `{"email": "user@example.com", "password": "securepass"}`
  - Response: User data and JWT token

- **GET /auth/me** - Get current user profile
  - Header: `Authorization: Bearer your_jwt_token`
  - Response: User data

### Cargo Endpoints

- **GET /schedule** - Get all cargo schedule items
  - Response: Array of cargo schedule items

- **GET /cargo/<cargo_id>** - Get details for specific cargo
  - Response: Cargo details

- **GET /protected** - Example protected route
  - Header: `Authorization: Bearer your_jwt_token`
  - Response: Confirmation message and user data

## Project Structure

- `app.py` - Main application entry point and route definitions
- `script.py` - Database setup script
- `seed_data.py` - Sample data generator for development
- `services/` - Core backend services
  - `auth_service.py` - Authentication logic
  - `auth_routes.py` - Authentication endpoints
  - `cargo_scheduler.py` - Cargo scheduling functionality
  - `connection.py` - Database connection handling
- `sql/` - SQL definition files
  - `users.sql` - User table schema
  - `cargo_schedule.sql` - Cargo schedule table schema
- `tests/` - Test suites
  - `unit/` - Unit tests

## Development Guidelines

### Database Access

You can connect to the database directly for debugging:

```
psql -h localhost -p 5432 -U your_username -d cargo_db
```

Useful psql commands:
- `\dt` - List all tables
- `\d table_name` - Describe table structure
- `\q` - Quit psql
- `DELETE FROM table_name;` - Clear a specific table

### Adding New Features

1. Create appropriate service modules in `services/` directory
2. Add routes in `app.py` or create a new blueprint
3. Update database schema in SQL files if needed
4. Create tests for your new features
