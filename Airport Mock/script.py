from flask import Flask, request, jsonify, render_template_string, redirect

app = Flask(__name__)

# Dummy cargo delay status
CARGO_DELAY_STATUS = {
    "d0bea113-d248-457e-80b4-f4a2bf8703a5": True,
    "fc6c885c-f897-412d-a408-9ba9108a5887": False,
    "7752d9bb-4baa-4781-aeeb-d754924b81b6": True,
    "349e514c-7ae5-4d45-992f-d0167b202254": False,
    "97f91c16-700a-420d-8fec-04fb8dd89927": True,
    "00168a2b-2593-4ec1-8623-64a27f6c897f": False,
    "4fde4c75-d97c-41c2-8735-6adfe2c7f11c": True,
    "3e77150a-2fc3-4fdb-a211-13948aa69abd": False,
    "72ec6484-33e9-4f20-b42a-703ade53e00d": True,
}

# Terminal -> Parking Availability
TERMINAL_PARKING = {
    "A": 10,
    "B": 0,
    "C": 1,
    "D": 100,
    "E": 5
}

# Generate message logic
def generate_status(cargo_id, terminal):
    delayed = CARGO_DELAY_STATUS.get(cargo_id, False)
    terminal = terminal.upper()
    parking_available = TERMINAL_PARKING.get(terminal, 0) > 0

    if not delayed and parking_available:
        return {
            "action": "Bay_Area",
            "message": f"Aircraft is on time. Proceed to Bay Area at Terminal {terminal}."
        }
    elif not delayed and not parking_available:
        return {
            "action": "Parking",
            "message": f"Aircraft is on time but no bay available. Parking available at Terminal {terminal}."
        }
    elif delayed and parking_available:
        return {
            "action": "Parking",
            "message": f"Aircraft delayed. Parking available at Terminal {terminal}."
        }
    else:
        return {
            "action": "Pull_Up",
            "message": "Aircraft delayed & no parking available. Pull over near rest area!"
        }

# Admin Interface Template
TEMPLATE = """
<!DOCTYPE html>
<html>
<head>
    <title>Airport Mock Admin</title>
    <style>
        body { font-family: Arial; padding: 30px; background: #f8f9fa; }
        h2 { color: #2c3e50; }
        table { width: 60%; margin-bottom: 30px; border-collapse: collapse; background: white; }
        th, td { padding: 12px; border: 1px solid #ccc; text-align: left; }
        th { background: #2c3e50; color: white; }
        input[type="number"] { width: 80px; padding: 6px; }
        input[type="checkbox"] { transform: scale(1.3); }
        button { background: #2980b9; color: white; border: none; padding: 10px 20px; font-weight: bold; border-radius: 5px; cursor: pointer; }
        button:hover { background: #1c5980; }
        form { background: #fff; padding: 20px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
    </style>
</head>
<body>
    <h2>🅿️ Terminal Parking Spots</h2>
    <form method="POST" action="/update-parking">
        <table>
            <tr><th>Terminal</th><th>Available Spots</th></tr>
            {% for terminal, count in terminal_parking.items() %}
            <tr>
                <td>{{ terminal }}</td>
                <td><input type="number" name="parking_{{ terminal }}" value="{{ count }}"></td>
            </tr>
            {% endfor %}
        </table>
        <button type="submit">Save Parking Info</button>
    </form>

    <h2>🚚 Cargo Delay Toggles</h2>
    <form method="POST" action="/update-delay">
        <table>
            <tr><th>Cargo ID</th><th>Delayed?</th></tr>
            {% for cid, delayed in cargo_delay_status.items() %}
            <tr>
                <td>{{ cid }}</td>
                <td><input type="checkbox" name="delay_{{ cid }}" {% if delayed %}checked{% endif %}></td>
            </tr>
            {% endfor %}
        </table>
        <button type="submit">Save Delay Status</button>
    </form>
</body>
</html>
"""

# Admin Dashboard
@app.route('/')
def dashboard():
    return render_template_string(
        TEMPLATE,
        terminal_parking=TERMINAL_PARKING,
        cargo_delay_status=CARGO_DELAY_STATUS
    )

# POST: Update Parking Spots
@app.route('/update-parking', methods=['POST'])
def update_parking():
    for terminal in TERMINAL_PARKING:
        val = request.form.get(f'parking_{terminal}')
        if val and val.isdigit():
            TERMINAL_PARKING[terminal] = int(val)
    return redirect('/')

# POST: Update Cargo Delay Status
@app.route('/update-delay', methods=['POST'])
def update_delay():
    for cid in CARGO_DELAY_STATUS:
        CARGO_DELAY_STATUS[cid] = f'delay_{cid}' in request.form
    return redirect('/')

# ✅ GET Endpoint for External System
@app.route('/get-cargo-status', methods=['GET'])
def get_cargo_status():
    cargo_id = request.args.get('cargo_id')
    terminal = request.args.get('terminal')

    if not cargo_id or not terminal:
        return jsonify({"error": "Missing cargo_id or terminal parameter"}), 400

    result = generate_status(cargo_id, terminal)
    return jsonify(result), 200

if __name__ == '__main__':
    app.run(debug=True, port=5010)
