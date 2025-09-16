import os
import psycopg2
from dotenv import load_dotenv, find_dotenv

load_dotenv(find_dotenv())

class CargoScheduler:
    def __init__(self):
        try:
            self.conn = psycopg2.connect(
                host=os.getenv("DB_HOST", "localhost"),
                port=os.getenv("DB_PORT", "5432"),
                database=os.getenv("DB_NAME", "cargo_db"),
                user=os.getenv("DB_USER", "smitpatel"),
                password=os.getenv("DB_PASSWORD", "")
            )
            self.cursor = self.conn.cursor()
            print("Connected to PostgreSQL for cargo scheduling")
        except Exception as e:
            print(f"Error connecting to PostgreSQL: {e}")
            self.cursor = None

    def get_schedule_items(self):
        try:
            self.cursor.execute("""
                SELECT id, cargo_id, pickup_time, criticality, 
                        pickup_location, dropoff_location
                FROM cargo_schedule
            """)
            rows = self.cursor.fetchall()
            return [
                {
                    "id": str(row[0]),
                    "cargoId": str(row[1]),
                    "pickupTime": row[2].isoformat(),
                    "criticality": row[3],
                    "pickupLocation": row[4],
                    "dropoffLocation": row[5],
                }
                for row in rows
            ]
        except Exception as e:
            print(f"Error fetching schedule items: {e}")
            return []

    def get_cargo_detail(self, cargo_id):
        try:
            self.cursor.execute("""
                SELECT cargo_id, description, weight, pickup_location,
                        dropoff_location, criticality, pickup_time
                FROM cargo_schedule
                WHERE cargo_id = %s
            """, (cargo_id,))
            row = self.cursor.fetchone()
            if row:
                return {
                    "cargoId": row[0],
                    "description": row[1],
                    "weight": row[2],
                    "pickupLocation": row[3],
                    "dropoffLocation": row[4],
                    "criticality": row[5],
                    "pickupTime": row[6].isoformat()
                }
            else:
                return None
        except Exception as e:
            # Handle the case where cargo_id is not found
            print(f"Error fetching cargo detail: {e}")
            return None