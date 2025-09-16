# Cargo Connect Airport Mock

A mock server for simulating airport cargo management systems to test cargo tracking applications.

## Overview

This project provides a simple Flask web server that simulates airport cargo tracking functionality with:

- Admin dashboard to manage cargo delay status
- Admin controls to set terminal parking availability
- REST API endpoint for external systems to query cargo status
- Dynamic response generation based on configured conditions

## Features

- **Web Admin Interface**: Control cargo delays and terminal parking availability
- **REST API**: Provides status information to external applications
- **Simulated Logic**: Generates different responses based on cargo delay status and parking availability
- **Easy Configuration**: Simple web interface to modify simulation parameters

## Getting Started

### Prerequisites

- Python 3.6 or higher
- pip (Python package manager)

### Running the Server

Start the Flask server by running:

```bash
python script.py
```

The server will start on port 5010 by default.

### Exposing to the Internet (Optional)

To make your local server accessible from the internet, you can use ngrok:

```bash
ngrok http 5010
```

## Usage

### Admin Dashboard

Access the admin dashboard by opening a web browser and navigating to:
```
http://localhost:5010/
```

Here you can:
- Update the number of available parking spots for each terminal
- Toggle the delay status for different cargo IDs

### API Endpoint

The mock server provides a REST API endpoint for external systems:

**Get Cargo Status:**
```
GET /get-cargo-status?cargo_id=CARGO_ID&terminal=TERMINAL_ID
```

Parameters:
- `cargo_id`: The ID of the cargo to check (must be one of the predefined IDs in the system)
- `terminal`: The terminal code (A, B, C, D, or E)

Example Response:
```json
{
  "action": "Bay_Area",
  "message": "Aircraft is on time. Proceed to Bay Area at Terminal A."
}
```

Possible responses:
- Aircraft on time + parking available → Go to Bay Area
- Aircraft on time + no parking → Go to Terminal Parking
- Aircraft delayed + parking available → Go to Terminal Parking
- Aircraft delayed + no parking → Pull up to rest area

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Author
Smit Patel