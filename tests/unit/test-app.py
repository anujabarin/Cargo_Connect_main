from flask import Flask, jsonify, request
import os

def create_app():
    app = Flask(__name__)
    app.config['SECRET_KEY'] = os.getenv('SECRET_KEY', 'some_default_secret')

    # Add a fallback/catch-all route
    @app.route('/', defaults={'path': ''})
    @app.route('/<path:path>')
    def catch_all(path):
        body = request.get_json()
        print("Received data:", body)
        return jsonify({"message": "Hello, Truck Scheduler!"})

    return app

if __name__ == '__main__':
    app = create_app()
    app.run(debug=True)
