import os
import psycopg2
from dotenv import load_dotenv, find_dotenv
from faker import Faker
 
load_dotenv(find_dotenv())
fake = Faker()

# Connect to PostgreSQL database
conn = psycopg2.connect(
    host=os.getenv("DB_HOST"),
    port=os.getenv("DB_PORT"),
    database=os.getenv("DB_NAME"),
    user=os.getenv("DB_USER"),
    password=os.getenv("DB_PASSWORD")
)
cur = conn.cursor()

for _ in range(10):
    criticality = fake.random_element(elements=["high", "medium", "low"])
    
    if criticality == "high":
        description = "Perishable goods/ medicines requiring immediate delivery."
    elif criticality == "medium":
        description = "Standard shipment. Handle with care as it contains delicate items."
    else:  # low
        description = "Low priority shipment. Non-perishable items. Can be delivered with no urgency."
 
    pickup_location = fake.random_element(elements=[
        "dfw-terminal-a_(32.90499459590296, -97.03632986050778)",
        "dfw-terminal-b_(32.90534203342366, -97.04491644516602)",
        "dfw-terminal-c_(32.89774624126012, -97.03576044516623)",
        "dfw-terminal-d_(32.89824196997102, -97.04478174516632)",
        "dfw-terminal-e_(32.890938252888326, -97.03569488515262)",
    ])
 
    dropoff_location = fake.random_element(elements=[
        "Ups-station_(32.99859322829199, -96.77238661872248)",
        "Fedex-store_(33.024281196004594, -96.79414555014056)",
        "Amazon-warehouse_(32.9801807167563, -96.73806753096646)",
        "Amazon-warehouse-2_(32.990626003517455, -96.78415098302702)",
        "Ups-store2_(32.82222393256571, -96.80966655288424)",
    ])
 
    cur.execute("""
        INSERT INTO cargo_schedule (
            id, cargo_id, pickup_time, criticality,
            pickup_location, dropoff_location, description, weight
        ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
    """, (
        fake.uuid4(),
        fake.uuid4(),
        fake.date_time_this_month(),
        criticality,
        pickup_location,
        dropoff_location,
        description,
        round(fake.pyfloat(min_value=5, max_value=50, right_digits=2), 2)
    ))
 
conn.commit()
cur.close()
conn.close()

print("Seeded data added to the database successfully.")