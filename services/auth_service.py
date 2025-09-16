import os
import jwt
import uuid
import datetime
import psycopg2
from psycopg2.extras import RealDictCursor
from typing import Optional, Dict, Any
from werkzeug.security import generate_password_hash, check_password_hash
from dotenv import load_dotenv, find_dotenv

load_dotenv(find_dotenv())

JWT_SECRET = os.getenv("JWT_SECRET", "dev_secret_key")
JWT_ALGORITHM = "HS256"
JWT_EXPIRATION_DELTA = datetime.timedelta(days=1)

class AuthService:
    def __init__(self):
        try:
            self.conn = psycopg2.connect(
                host=os.getenv("DB_HOST", "localhost"),
                port=os.getenv("DB_PORT", "5432"),
                database=os.getenv("DB_NAME", "cargo_db"),
                user=os.getenv("DB_USER", "smitpatel"),
                password=os.getenv("DB_PASSWORD", ""),
                cursor_factory=RealDictCursor
            )
            self.cursor = self.conn.cursor()
            print("Connected to PostgreSQL database!")
        except Exception as e:
            print(f"Database connection failed: {e}")
            self.cursor = None
        
        self._ensure_demo_user_exists()

    def _ensure_demo_user_exists(self):
        try:
            self.cursor.execute(
                "SELECT * FROM users WHERE email = %s",
                ("demo@cargolive.com",)
            )
            demo_user = self.cursor.fetchone()
            
            if not demo_user:
                demo_id = str(uuid.uuid4())
                password_hash = generate_password_hash("demo123")
                
                self.cursor.execute(
                    """
                    INSERT INTO users (id, email, password_hash, full_name, is_google_account)
                    VALUES (%s, %s, %s, %s, %s)
                    """,
                    (demo_id, "demo@cargolive.com", password_hash, "Demo User", False)
                )
                self.conn.commit()
            elif demo_user and demo_user["password_hash"] == "placeholder_hash":
                password_hash = generate_password_hash("demo123")
                
                self.cursor.execute(
                    """
                    UPDATE users 
                    SET password_hash = %s
                    WHERE email = %s
                    """,
                    (password_hash, "demo@cargolive.com")
                )
                self.conn.commit()
        except Exception as e:
            print(f"Error with demo user: {e}")

    def register_user(self, email: str, password: str, full_name: str, is_google_account: bool = False) -> Optional[Dict[str, Any]]:
        try:
            email = email.lower()
            self.cursor.execute(
                "SELECT * FROM users WHERE email = %s",
                (email,)
            )
            if self.cursor.fetchone():
                return None
            
            user_id = str(uuid.uuid4())
            password_hash = generate_password_hash(password) if not is_google_account else "google_auth"
            
            self.cursor.execute(
                """
                INSERT INTO users (id, email, password_hash, full_name, is_google_account)
                VALUES (%s, %s, %s, %s, %s)
                RETURNING id, email, full_name, is_google_account, created_at, updated_at
                """,
                (user_id, email, password_hash, full_name, is_google_account)
            )
            self.conn.commit()
            
            user_data = self.cursor.fetchone()
            token = self._generate_token(user_data)
            
            return {
                "user": dict(user_data),
                "token": token
            }
        except Exception as e:
            print(f"Registration error: {e}")
            if hasattr(self, 'conn') and self.conn:
                self.conn.rollback()
            return None

    def login_user(self, email: str, password: str) -> Optional[Dict[str, Any]]:
        try:
            email = email.lower()
            self.cursor.execute(
                "SELECT * FROM users WHERE email = %s",
                (email,)
            )
            user = self.cursor.fetchone()
            
            if not user:
                return None
            
            if not user["is_google_account"]:
                if not check_password_hash(user["password_hash"], password):
                    return None
            
            token = self._generate_token(user)
            
            user_data = dict(user)
            del user_data["password_hash"]
            
            return {
                "user": user_data,
                "token": token
            }
        except Exception as e:
            print(f"Login error: {e}")
            return None

    def get_user_by_id(self, user_id: str) -> Optional[Dict[str, Any]]:
        try:
            self.cursor.execute(
                "SELECT * FROM users WHERE id = %s",
                (user_id,)
            )
            user = self.cursor.fetchone()
            
            if user:
                user_data = dict(user)
                del user_data["password_hash"]
                return user_data
            
            return None
        except Exception as e:
            print(f"User lookup error: {e}")
            return None

    def verify_token(self, token: str) -> Optional[Dict[str, Any]]:
        try:
            payload = jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALGORITHM])
            
            exp = payload.get("exp", 0)
            if datetime.datetime.fromtimestamp(exp) < datetime.datetime.utcnow():
                return None
            
            user_id = payload.get("sub")
            if not user_id:
                return None
            
            return self.get_user_by_id(user_id)
        except Exception as e:
            print(f"Token verification error: {e}")
            return None

    def _generate_token(self, user: Dict[str, Any]) -> str:
        payload = {
            "sub": user["id"],
            "exp": datetime.datetime.utcnow() + JWT_EXPIRATION_DELTA,
            "iat": datetime.datetime.utcnow(),
            "email": user["email"]
        }
        
        return jwt.encode(payload, JWT_SECRET, algorithm=JWT_ALGORITHM)

