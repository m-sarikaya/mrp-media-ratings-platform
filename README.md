# Media Ratings Platform (MRP)

FH Technikum Wien - Semesterprojekt

## GitHub Repository

https://github.com/m-sarikaya/mrp-media-ratings-platform

## Beschreibung

RESTful HTTP Server für eine Media-Bewertungsplattform. Benutzer können Filme, Serien und Spiele verwalten und bewerten.

## Technologien

- Java 22
- PostgreSQL (Docker)
- Jackson (JSON)
- com.sun.net.httpserver (HTTP Server)

## Architektur

Das Projekt folgt dem **SOLID-Prinzip** mit einer 3-Schichten-Architektur:

```
┌─────────────┐
│   Handler   │  ← HTTP Anfragen/Antworten
└──────┬──────┘
       │
┌──────▼──────┐
│   Service   │  ← Business-Logik
└──────┬──────┘
       │
┌──────▼──────┐
│ Repository  │  ← Datenbank-Operationen
└─────────────┘
```

### Struktur

```
src/main/java/org/example/
├── Application.java          # Einstiegspunkt
├── server/
│   └── MrpServer.java        # HTTP Server
├── handler/
│   ├── TestHandler.java      # Test-Endpoint
│   ├── UserHandler.java      # User-Endpoints
│   └── MediaHandler.java     # Media-Endpoints
├── service/
│   ├── UserService.java      # User Business-Logik
│   └── MediaService.java     # Media Business-Logik
├── domain/
│   ├── User.java             # User Model
│   └── MediaEntry.java       # Media Model
└── persistence/
    ├── DatabaseConnection.java
    ├── UserRepository.java
    └── MediaRepository.java
```

## API Endpoints

| Methode | Pfad | Beschreibung | Auth |
|---------|------|--------------|------|
| GET | /test | Server-Test | Nein |
| POST | /api/users | User registrieren | Nein |
| POST | /api/users/login | User einloggen | Nein |
| GET | /api/media | Alle Medien | Nein |
| GET | /api/media/{id} | Ein Medium | Nein |
| POST | /api/media | Medium erstellen | Ja |
| PUT | /api/media/{id} | Medium bearbeiten | Ja |
| DELETE | /api/media/{id} | Medium löschen | Ja |

## Installation & Start

### Voraussetzungen

- Java 22
- Maven
- Docker

### Datenbank starten

```bash
docker-compose up -d
```

### Server starten

```bash
mvn compile
mvn exec:java -Dexec.mainClass="org.example.Application"
```

Oder in IntelliJ: `Application.java` → Run

### Server testen

```bash
curl http://localhost:8080/test
```

## Postman Collection

Die Datei `MRP API.postman_collection.json` enthält alle API-Requests zum Testen.

Import in Postman: File → Import → Datei auswählen

## Datenbank Schema

```sql
-- Users
CREATE TABLE Users(
    ID SERIAL PRIMARY KEY,
    Username VARCHAR(256) UNIQUE NOT NULL,
    Password VARCHAR(256) NOT NULL,
    Token VARCHAR(256)
);

-- Media Entries
CREATE TABLE MediaEntries(
    ID SERIAL PRIMARY KEY,
    Title VARCHAR(256) NOT NULL,
    Description TEXT,
    MediaType VARCHAR(50) NOT NULL,
    ReleaseYear INT,
    Genre VARCHAR(100),
    AgeRestriction INT,
    CreatorId INT NOT NULL,
    FOREIGN KEY (CreatorId) REFERENCES Users(ID)
);
```

## Autor

Mustafa Sarikaya - FH Technikum Wien
