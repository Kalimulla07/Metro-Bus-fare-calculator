# Metro & Bus Fare Calculator with Dynamic Pricing

A modern, responsive, and robust full-stack web application designed to calculate transit fares across urban metro and bus networks. The application features **Strategy Design Pattern** fare calculation algorithms, **Dijkstra's Algorithm** for shortest route optimization, and **Dynamic Peak Surge Pricing** based on passenger demand curves.

---

## 1. Technology Stack

- **Frontend:** HTML5, CSS3 (Modern Blue & White Transit Theme), Vanilla JavaScript (ES6+ `fetch` API, HTML5 Canvas)
- **Backend:** Java 17+, Spring Boot 3.3.x (Spring Web, Spring Data JPA, Hibernate, Bean Validation)
- **Database:** MySQL 8.x
- **Architecture:** Layered Architecture (`controller`, `service`, `model`, `repository`, `strategy`)
- **IDE:** Visual Studio Code

---

## 2. Key Features

1. **Interactive Dashboard:**
   - 4 Live metric summary cards: **Total Journeys**, **Average Fare**, **Peak Hour Fare**, and **Off-Peak Fare**.
   - Real-time demand status indicator and quick actions.
2. **Fare Calculator:**
   - Multi-modal support: **Metro** (Subway) & **Bus** (City Transit).
   - Passenger concession types: **Normal**, **Student** (25% discount), and **Senior Citizen** (40% discount).
   - Real-time station auto-fill for distance.
   - Comprehensive cost breakdown: Base Fare, Distance Charge, Dynamic Surge/Saver adjustment, Concession Discount, and Final Fare.
3. **Strategy Design Pattern:**
   - `DistanceBasedFareStrategy`: Standard linear per-kilometer fare.
   - `ZoneBasedFareStrategy`: Zonal boundary transit charges (Zones 1, 2, and 3).
   - `PeakOffPeakFareStrategy`: Demand-responsive dynamic surcharge (+30%) and off-peak discount (-10%).
4. **Route Information & Dijkstra's Algorithm:**
   - Graph network connecting 13 stations across 3 Metro lines and 3 Bus routes.
   - Calculates the shortest route path, total distance, travel time, and intermediate stops.
5. **Dynamic Pricing Module:**
   - 24-hour passenger load distribution visualized on an interactive Canvas chart.
   - Peak hour surge alerts (&ge; 6,000 passengers/hour).
   - Smart travel recommendations to incentivize off-peak travel.
6. **Fare History (MySQL Persistence):**
   - Stores every calculation into MySQL with timestamp, route, distance, mode, passenger category, and final cost.
   - Instant search, deletion, and full history clearing.

---

## 3. Project Directory Structure

```text
metro-bus-fare-calculator/
├── pom.xml
├── database.sql
├── README.md
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── transit/
        │           └── farecalculator/
        │               ├── MetroFareCalculatorApplication.java
        │               ├── controller/
        │               │   ├── DynamicPricingController.java
        │               │   ├── FareController.java
        │               │   └── RouteController.java
        │               ├── model/
        │               │   ├── DashboardStats.java
        │               │   ├── DemandData.java
        │               │   ├── FareHistory.java
        │               │   ├── FareRequest.java
        │               │   ├── FareResponse.java
        │               │   ├── RouteInfo.java
        │               │   ├── RoutePathResponse.java
        │               │   └── Station.java
        │               ├── repository/
        │               │   └── FareHistoryRepository.java
        │               ├── service/
        │               │   ├── DynamicPricingService.java
        │               │   ├── FareCalculationService.java
        │               │   └── RouteService.java
        │               └── strategy/
        │                   ├── DistanceBasedFareStrategy.java
        │                   ├── FareCalculationStrategy.java
        │                   ├── FareStrategyFactory.java
        │                   ├── PeakOffPeakFareStrategy.java
        │                   └── ZoneBasedFareStrategy.java
        └── resources/
            ├── application.properties
            └── static/
                ├── index.html
                ├── css/
                │   └── style.css
                └── js/
                    └── app.js
```

---

## 4. Database Setup (MySQL)

1. Open **MySQL Workbench**, **Command Prompt (mysql)**, or **phpMyAdmin**.
2. Run the provided `database.sql` script or execute:

```sql
CREATE DATABASE IF NOT EXISTS transit_fare_db;
USE transit_fare_db;
```

3. Configure your MySQL credentials in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/transit_fare_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
```
*(Replace `root` with your actual MySQL password)*.

---

## 5. Running in Visual Studio Code

1. **Install Recommended VS Code Extensions:**
   - **Extension Pack for Java** (by Microsoft)
   - **Spring Boot Extension Pack** (by VMware)
2. **Open the Project Folder:**
   - In VS Code, go to `File` &rarr; `Open Folder...` &rarr; Select:
     `C:\Users\dell\.gemini\antigravity\scratch\metro-bus-fare-calculator`
3. **Launch the Application:**
   - Open `src/main/java/com/transit/farecalculator/MetroFareCalculatorApplication.java`.
   - Click the **Run** or **Debug** button above `public static void main(String[] args)`.
4. **Access the Application:**
   - Open your browser and navigate to:
     **`http://localhost:8080`**

---

## 6. REST API Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/fare/calculate` | Calculates fare using selected strategy & saves to MySQL |
| `GET` | `/api/fare/history` | Fetches all stored calculation records |
| `DELETE` | `/api/fare/history/{id}` | Deletes a calculation record |
| `DELETE` | `/api/fare/history` | Clears all calculation history |
| `GET` | `/api/fare/stats` | Fetches aggregated stats for top 4 cards |
| `GET` | `/api/routes/stations` | Returns all stations with zones |
| `GET` | `/api/routes/all` | Returns all metro and bus routes |
| `GET` | `/api/routes/shortest-path` | Calculates Dijkstra shortest route |
| `GET` | `/api/dynamic-pricing/demand-data` | Returns 24-hour demand profile & pricing surge |
