from flask import Blueprint, request, jsonify
from .auth_service import AuthService
from functools import wraps

auth_bp = Blueprint('auth', __name__, url_prefix='/auth')

try:
    auth_service = AuthService()
except Exception as e:
    auth_service = None
    print(f"Error initializing AuthService: {e}")
    
def token_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        token = None
        auth_header = request.headers.get('Authorization')
        
        if auth_header and auth_header.startswith('Bearer '):
            token = auth_header[7:]
        
        if not token:
            return jsonify({"error": "Authentication token is missing"}), 401
        
        user = auth_service.verify_token(token)
        if not user:
            return jsonify({"error": "Invalid or expired token"}), 401
        
        request.user = user
        
        return f(*args, **kwargs)
    
    return decorated

@auth_bp.route('/register', methods=['POST'])
def register():
    data = request.get_json()
    
    if not data:
        return jsonify({"error": "No input data provided"}), 400
    
    for field in ['email', 'password', 'fullName']:
        if field not in data:
            return jsonify({"error": f"Missing required field: {field}"}), 400
    
    if '@' not in data['email']:
        return jsonify({"error": "Invalid email format"}), 400
    
    if len(data['password']) < 6:
        return jsonify({"error": "Password must be at least 6 characters long"}), 400
    
    result = auth_service.register_user(
        email=data['email'],
        password=data['password'],
        full_name=data['fullName']
    )
    
    if not result:
        return jsonify({"error": "Email already registered"}), 409
    
    return jsonify(result), 201

@auth_bp.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    
    if not data:
        return jsonify({"error": "No input data provided"}), 400
    
    if 'email' not in data or 'password' not in data:
        return jsonify({"error": "Email and password are required"}), 400
    
    result = auth_service.login_user(
        email=data['email'],
        password=data['password']
    )
    
    if not result:
        return jsonify({"error": "Invalid email or password"}), 401
    
    return jsonify(result), 200

@auth_bp.route('/me', methods=['GET'])
@token_required
def get_user_profile():
    return jsonify({"user": request.user}), 200

