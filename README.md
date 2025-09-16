# Cargo Connect - Improving Airport Freight Flow        Jan--May 2025 
A software only platform that reduces
freight truck idle time around airports by fusing real-time routing,
dock/parking assignments, and flight/cargo status without any
infrastructure changes. The system integrates an Android driver app with
a Flask backend, a PostgreSQL data layer, and external mocks for airport
ops and traffic (DALI).

## Problem

Freight trucks routinely stall in congested airport cargo
zones wasting fuel, missing delivery windows, and compounding
congestion. Airports rarely let you change physical infrastructure, so
effective solutions must be software-defined, adaptive, and integrate
with multiple stakeholders.

## Solution

Cargo Connect delivers a dynamic routing and scheduling platform:
- Mobile App (Android/Jetpack Compose): live assignments, route
guidance, entry/exit, dock, parking.
- Backend (Flask): auth, scheduling, routing, notifications, and
integrations.
- Data (PostgreSQL): operational and schedule stores.
- External Systems: Airport System (flight/dock/parking), DALI mock
(traffic), Google Maps (location/routing).

In simulation, we validated multiple logistics scenarios (accidents,
parking delays, docking failures) via mocks and Google Maps.

## Architecture (High Level)

Core subsystems: Manager (orchestration), Authentication, Truck
Scheduling, Routing, Notifications.
External interfaces: Airport System, Google Maps, Cargo Service, DALI
Mock.
Datastores: Main DB (operations), Schedule DB (truck scheduling).

Deployment: Android app ↔ Flask app server ↔ PostgreSQL; integrates with
Airport System & DALI; Google APIs for maps/location.

## Key Features

- Real-time routing & traffic guidance to and from the airport (via
DALI + Maps).
- Dock & parking assignment with updates and reassignments when
congested.
- Flight/cargo awareness for schedule-informed decisions.
- Role-appropriate UIs: driver app and admin visibility
(dashboards/ops).

Representative use cases:
• Schedule trucks to the airport
• At the airport (dock/parking)
• From the airport (departure routing)

## Tech Stack

Mobile: Kotlin, Jetpack Compose
Backend: Python, Flask (REST)
Database: PostgreSQL
Mocks/Integrations: Laravel (DALI mock), Python (Airport mock), Google
Maps APIs
Security/Infra: JWT, TLS, RBAC (planned)

## Screens, Flows & Requirements

Driver experience: arrival ETA, airport entry, assigned dock & time,
parking slot, next-destination routing, exit terminal.
Systems I/O: airport API for flight/dock/parking; DALI for traffic;
mobile reports status; backend synchronizes.
Non-functional targets: responsive updates (\<\~seconds), reliable
integration, encrypted comms, scalable stores.

## Validation & Test Summary

• Unit, integration, and scenario tests with 90%+ test coverage across
components.
• Known issues encountered during testing: delayed notifications,
malformed data from the airport mock, periodic trigger reliability.

Limitations (current):
- Evaluated primarily with mock systems (DALI/Airport); no live-ops
field trials yet.
- Simulated movement via Google Maps; real sensor/vehicle telemetry not
yet integrated.
- Scale not validated beyond small datasets; iOS app not implemented.

## Getting Started (Local)

Prerequisites: Python 3.10+, Android Studio, PostgreSQL 14+, Google Maps
API key.

Backend (Flask):
1. git clone <backend-repo\>
2. cd backend
3. python -m venv .venv && source .venv/bin/activate
4. pip install -r requirements.txt
5. export
DATABASE_URL=postgresql://user:pass@localhost:5432/cargo_connect
6. export MAPS_API_KEY=<your_key\>
7. flask run

Android App: open in Android Studio, set backend URL, run on
emulator/device.

Mocks:
- DALI mock (Laravel): php artisan serve
- Airport mock (Python): uvicorn airport_mock.app:app \--reload

## Demo & Documentation

- Final Presentation (slides)
- Final Video (end-to-end demo)
- Specs and requirements docs included under docs

## Roadmap

- Live integration pilots with airports & carriers.
- Scale tests (10k+ schedules/day), latency SLAs.
- iOS app & unified admin dashboard.
- Robust notification pipeline and data quality guards.

## Team

Team 1 (SE 6387, UT Dallas): FNU Anuja (TL), Smit S. Patel, Bradley E.
Stover, Chijioke Elisha-Wigwe.


