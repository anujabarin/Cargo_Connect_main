import sys
import os
sys.path.append(os.path.abspath(os.path.dirname(__file__)))
from flask import Flask, jsonify, request
from dotenv import load_dotenv
from flask_cors import CORS
from services.cargo_scheduler import CargoScheduler
from services.auth_routes import auth_bp, token_required

load_dotenv()

cargo_scheduler = CargoScheduler()

def create_app():
    app = Flask(__name__)
    
    # Allow frontend calls
    CORS(app, resources={r"/*": {"origins": "*"}})
    
    # Authentication Components
    app.register_blueprint(auth_bp)

    # Get Schedules data
    @app.route("/schedule", methods=["GET"])
    def get_schedule_data():
        items = cargo_scheduler.get_schedule_items()
        return jsonify(items)

    # Get the Cargo data
    @app.route("/cargo/<string:cargo_id>", methods=["GET"])
    def get_cargo_detail(cargo_id):
        result = cargo_scheduler.get_cargo_detail(cargo_id)
        if result:
            return result
            
        return jsonify({"error": "Cargo not found"}), 404
    
    # Secure endpoint example
    @app.route("/protected", methods=["GET"])
    @token_required
    def protected_route():
        return jsonify({
            "message": "This is a protected route",
            "user": request.user
        })

    return app

if __name__ == "__main__":
    app = create_app()
    app.run(debug=True, port=8800)