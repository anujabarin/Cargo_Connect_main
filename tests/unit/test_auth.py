import requests
import json

BASE_URL = "http://localhost:8800"  # Change if server runs on different port

def test_login_demo_user():
    """Test logging in with the demo user"""
    print("\n=== Testing Demo User Login ===")
    
    response = requests.post(
        f"{BASE_URL}/auth/login",
        json={
            "email": "demo@cargolive.com",
            "password": "demo123"
        }
    )
    
    print(f"Status Code: {response.status_code}")
    if response.status_code == 200:
        data = response.json()
        print("Login successful!")
        print(f"User: {data['user']['full_name']} ({data['user']['email']})")
        print(f"Token: {data['token'][:20]}...")
        return data['token']
    else:
        print(f"Login failed: {response.text}")
        return None

def test_register_user():
    """Test registering a new user"""
    print("\n=== Testing User Registration ===")
    
    response = requests.post(
        f"{BASE_URL}/auth/register",
        json={
            "email": "test@example.com", 
            "password": "password123",
            "fullName": "Test User" 
        }
    )
    
    print(f"Status Code: {response.status_code}")
    if response.status_code == 201:
        data = response.json()
        print("Registration successful!")
        print(f"User: {data['user']['full_name']} ({data['user']['email']})")
        print(f"Token: {data['token'][:20]}...")
        return data['token']
    else:
        print(f"Registration failed: {response.text}")
        return None

def test_protected_route(token):
    """Test accessing a protected route with a token"""
    print("\n=== Testing Protected Route ===")
    
    if not token:
        print("No token provided, skipping this test.")
        return
    
    response = requests.get(
        f"{BASE_URL}/protected",
        headers={
            "Authorization": f"Bearer {token}"
        }
    )
    
    print(f"Status Code: {response.status_code}")
    if response.status_code == 200:
        data = response.json()
        print("Protected route access successful!")
        print(f"Message: {data['message']}")
        print(f"User: {data['user']['full_name']} ({data['user']['email']})")
    else:
        print(f"Protected route access failed: {response.text}")

def run_all_tests():
    """Run all authentication tests"""
    # Test login with demo user
    token = test_login_demo_user()
    
    if token:
        # Test protected route with token
        test_protected_route(token)
    
    # Test registering a new user
    # Note: will fail if user already exists
    new_token = test_register_user()
    
    if new_token:
        test_protected_route(new_token)

if __name__ == "__main__":
    run_all_tests()