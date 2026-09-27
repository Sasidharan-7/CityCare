# CityCare – AI-Powered Smart City Issue Management
> **Report. Track. Resolve.**  
> *A Crowdsourced Civic Issue Reporting & Resolution System (SIH25031)*

---

## 📌 Problem Statement
Rapid urbanization has intensified municipal challenges, leading to recurrent infrastructure failure—including hazardous potholes, overflowing garbage bins, water pipeline bursts, malfunctioning streetlights, and perilous open manholes. Citizens frequently encounter significant hurdles in reporting these grievances due to fragmented communication channels, absence of real-time tracking, and manual routing delays.

**CityCare** addresses this challenge by providing an end-to-end platform connecting citizens with municipal administration. Citizens capture and geotag civic issues; the built-in AI classifies the severity, calculates priority, and automatically routes the complaint to the designated department. Officers manage repairs with transparent resolution proofs, keeping citizens informed every step of the way.

---

## 🏗️ Architecture Diagram

```
+---------------------------------------------------------------------------------+
|                                 CITIZEN CLIENT                                  |
|   (Responsive Web: HTML5 / CSS3 / JavaScript / Leaflet.js / OpenStreetMap)      |
+---------------------------------------------------------------------------------+
          |                                                       ^
     1. Submit Image, GPS, Description                    8. In-App Notifications
          v                                                       |
+---------------------------------------------------------------------------------+
|                               SPRING BOOT REST API                              |
|                          (Java 17+, Spring Security, JWT)                       |
+---------------------------------------------------------------------------------+
     |                     |                            |                   |
 2. Image (Local/Cloud)    | 3. Classify / Priority     | 4. Deduplicate    | 5. Store / Query
     v                     v                            v                   v
+----------------+  +----------------------+  +------------------+  +-------------+
| Local Storage  |  | AI Microservice      |  | Haversine        |  | MySQL / H2  |
| /uploads       |  | (Flask / Heuristic / |  | Spatial Distance |  | JPA Entities|
|                |  | YOLO Fallback)       |  | & Temporal Window|  |             |
+----------------+  +----------------------+  +------------------+  +-------------+
                                                                            |
                                                                   6. Route to Officer
                                                                            v
+---------------------------------------------------------------------------------+
|                           OFFICER & ADMIN CONSOLES                              |
|      (Live Incident Maps, KPI Charts, Status Updates, Resolution Proof Upload)   |
+---------------------------------------------------------------------------------+
```

---

## 🚀 Key Features

1. **Citizen Portal & Issue Reporting:**
   - One-tap GPS geolocation and interactive map pin adjustment via Leaflet.js + OpenStreetMap.
   - Device camera capture (`accept="image/*" capture="environment"`) and local file upload.
   - Real-time client-side image preview and validation.

2. **AI-Powered Image Classification & Priority Prediction:**
   - Automated categorization: `POTHOLE`, `GARBAGE`, `WATER_LEAKAGE`, `BROKEN_STREETLIGHT`, `OPEN_DRAIN`, `ROAD_DAMAGE`, `OTHER`.
   - Priority engine factoring in severity and environmental hazards (`CRITICAL`, `HIGH`, `MEDIUM`, `LOW`).
   - Transparent architecture supporting seamless replacement with trained YOLO / PyTorch / ONNX models.

3. **Automated Department Routing:**
   - Potholes & Road Damage $\rightarrow$ **Road Department**
   - Garbage & Solid Waste $\rightarrow$ **Sanitation Department**
   - Water Leakage & Pipe Bursts $\rightarrow$ **Water Department**
   - Broken Streetlights $\rightarrow$ **Electricity Department**
   - Open Drains & Manholes $\rightarrow$ **Public Works Department (PWD)**
   - Other Civic Inconveniences $\rightarrow$ **General Civic Department**

4. **Duplicate Incident Detection (Haversine Formula):**
   - Spatial proximity analysis ($\le 100\text{ meters}$) combined with temporal windowing ($48\text{ hours}$).
   - Transparent duplicate flagging without premature deletion, preventing redundant municipal dispatch.

5. **Officer Resolution Console & Proof Verification:**
   - Filterable workbench by priority, status, and department.
   - Field navigation with one-click Google Maps destination routing.
   - Mandatory resolution proof upload (Before-and-After photo verification) before marking issues as resolved.

6. **Admin Dashboard & Smart City Analytics:**
   - City-wide geographic incident map with categorized status pins.
   - Real-time workload distribution by category, status pipeline, and department.

7. **In-App Notification Stream:**
   - Real-time milestone updates: Submission $\rightarrow$ Routing $\rightarrow$ In-Progress $\rightarrow$ Resolved.

---

## 💻 Tech Stack

| Layer | Technologies |
| :--- | :--- |
| **Frontend** | HTML5, Vanilla CSS3 (Custom Design System), JavaScript (ES6+), Leaflet.js, OpenStreetMap |
| **Backend** | Java 17+, Spring Boot 3.3.x, Spring Data JPA, Spring Security, JWT (JJWT 0.11.5), Maven |
| **Database** | MySQL 8.0+ (with seamless auto-fallback support for H2 in standalone dev) |
| **AI Microservice** | Python 3.10+, Flask 3.0, Flask-CORS, Pillow |
| **Storage** | Local Multipart Storage (`/uploads`), ready for AWS S3 / Cloudinary adapters |

---

## 📂 Project Structure

```
CityCare/
├── frontend/                     # Modern responsive UI
│   ├── index.html                # Landing page
│   ├── login.html                # JWT Authentication
│   ├── register.html             # Citizen Registration
│   ├── citizen-dashboard.html    # Citizen Overview & Metrics
│   ├── report-issue.html         # Camera, GPS, Leaflet, AI Preview
│   ├── complaints.html           # Complaint history & search/filters
│   ├── complaint-details.html    # Status timeline & resolution proof
│   ├── officer-dashboard.html    # Municipal officer operations console
│   ├── officer-complaint-details.html # Officer status updates & proof upload
│   ├── admin-dashboard.html      # City-wide KPI charts & incident map
│   ├── notifications.html        # In-app notifications
│   ├── profile.html              # User profile & credentials
│   ├── css/
│   │   ├── style.css             # Core design system & tokens
│   │   ├── dashboard.css         # Analytics & status styles
│   │   └── responsive.css        # Mobile/tablet media queries
│   └── js/
│       ├── app.js                # Core API wrapper, JWT, Toasts
│       ├── auth.js               # Form validation & auth routing
│       ├── map.js                # Leaflet & OpenStreetMap helpers
│       ├── complaints.js         # REST client & timeline rendering
│       └── dashboard.js          # Chart generation & dashboards
│
├── backend/                      # Spring Boot 3.3 REST API
│   ├── src/main/java/com/citycare/
│   │   ├── CityCareApplication.java
│   │   ├── config/               # CorsConfig, WebMvcConfig, DataInitializer
│   │   ├── controller/           # Auth, Complaint, Department, Admin, Officer
│   │   ├── dto/                  # Requests, Responses, Analytics, AI payloads
│   │   ├── entity/               # User, Department, Complaint, Update, Notification
│   │   ├── exception/            # GlobalExceptionHandler & custom exceptions
│   │   ├── repository/           # Spring Data JPA Repositories
│   │   ├── security/             # SecurityConfig, JwtUtil, JwtAuthFilter
│   │   └── service/              # Business logic, FileStorage, AI, Duplicates
│   ├── src/main/resources/
│   │   └── application.properties# Config, DB, JWT secret, AI URL
│   └── pom.xml                   # Maven dependencies
│
├── ai-service/                   # Python Flask AI Microservice
│   ├── app.py                    # Classification & Priority Engine
│   ├── requirements.txt          # Python dependencies
│   └── model/                    # Weights directory (YOLO / ONNX drop-in)
│
├── database/
│   └── schema.sql                # Complete MySQL DDL & Seed Data
│
├── uploads/                      # Local uploaded complaint and proof photos
├── tools/                        # Portable tools (e.g. Apache Maven)
├── README.md                     # Documentation
└── .gitignore                    # Version control ignore rules
```

---

## 🛠️ Step-by-Step Setup Guide

### 1. Database Setup (MySQL)
1. Ensure MySQL is running on `localhost:3306`.
2. Open MySQL CLI or MySQL Workbench and execute:
   ```sql
   SOURCE database/schema.sql;
   ```
3. Alternatively, the application will automatically create all tables and seed data upon startup!

### 2. AI Microservice Setup
Open a terminal in the root directory:
```bash
cd ai-service
pip install -r requirements.txt
python app.py
```
*The AI service will start on `http://localhost:5000` with `/health` and `/classify` endpoints.*

### 3. Spring Boot Backend Setup
Open a separate terminal:
```bash
cd backend
mvn spring-boot:run
```
*(If running on a system with portable tools, use `tools\apache-maven-3.9.9\bin\mvn.cmd spring-boot:run`)*  
*The backend API will start on `http://localhost:8080`.*

### 4. Frontend Launch
You can serve the `frontend/` directory using any local HTTP static server or simply open `frontend/index.html` in your browser.
Using Python:
```bash
cd frontend
python -m http.server 3000
```
Then visit: `http://localhost:3000` or `http://localhost:3000/index.html`.

---

## 🔑 Demo Credentials

All test accounts come pre-configured out of the box with the password `password123`:

| Role | Email | Password | Scope |
| :--- | :--- | :--- | :--- |
| **System Admin** | `admin@citycare.gov.in` | `password123` | City analytics, all complaints, all departments |
| **Road Officer** | `officer.road@citycare.gov.in` | `password123` | Road Department console, Potholes & Road Damage |
| **Sanitation Officer** | `officer.sanitation@citycare.gov.in` | `password123` | Sanitation Department console, Garbage clearance |
| **Citizen (Demo)** | `citizen@gmail.com` | `password123` | Complaint submission, tracking, notifications |

---

## 📡 REST API Reference

### Authentication
- `POST /api/auth/register` — Register a citizen account
- `POST /api/auth/login` — Authenticate and receive JWT Bearer token

### Complaints
- `POST /api/complaints` — Submit a complaint (Multipart: photo, category, description, GPS lat/lng)
- `GET /api/complaints` — Retrieve complaints (role-filtered; supports `?status=`, `?category=`, `?priority=`, `?search=`)
- `GET /api/complaints/{id}` — Get single complaint details with timeline updates
- `PUT /api/complaints/{id}/status` — Update status & remarks (`ASSIGNED`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`)
- `POST /api/complaints/{id}/resolution-proof` — Upload resolution proof photo (Multipart: photo, remarks)

### Departments & Notifications
- `GET /api/departments` — List municipal departments
- `GET /api/notifications` — Get in-app alerts for logged-in user
- `PUT /api/notifications/{id}/read` — Mark notification as read

### Admin & Officer Operations
- `GET /api/admin/analytics` — Smart city overview, category/status/department stats, incident coordinates
- `GET /api/officer/complaints` — Fetch assigned complaints for the authenticated officer

---

## 🌟 Future Enhancements
- Integration with edge-deployed YOLOv11 for real-time mobile camera inference.
- WhatsApp / SMS integration via Twilio or government SMS gateways.
- Citizen feedback ratings & municipal SLA resolution timers.
