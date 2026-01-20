# Media Ratings Platform (MRP)

FH Technikum Wien - Semesterprojekt

## GitHub Repository

https://github.com/m-sarikaya/mrp-media-ratings-platform

## Beschreibung

RESTful HTTP Server für eine Media-Bewertungsplattform. Benutzer können Filme, Serien und Spiele verwalten und bewerten.

## Technologien

- Java 22
- PostgreSQL (Docker)
- Jackson (JSON + JavaTime)
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
│   ├── AuthHandler.java      # Register/Login
│   ├── UserHandler.java      # Profil, History, Favorites, Recommendations
│   ├── MediaHandler.java     # Media-CRUD
│   ├── RatingHandler.java    # Ratings, Likes, Confirm
│   ├── FavoritesHandler.java # Favoriten
│   └── LeaderboardHandler.java
├── service/
│   ├── UserService.java      # User Business-Logik
│   ├── MediaService.java     # Media Business-Logik
│   ├── RatingService.java
│   ├── FavoriteService.java
│   └── RecommendationService.java
├── domain/
│   ├── User.java             # User Model
│   ├── MediaEntry.java       # Media Model
│   └── Rating.java
└── persistence/
    ├── DatabaseConnection.java
    ├── UserRepository.java
    ├── MediaRepository.java
    ├── RatingRepository.java
    └── FavoriteRepository.java
```

## API Endpoints

| Methode | Pfad | Beschreibung | Auth |
|---------|------|--------------|------|
| GET | /test | Server-Test | Nein |
| POST | /api/users/register | User registrieren | Nein |
| POST | /api/users/login | User einloggen | Nein |
| GET | /api/users/{id}/profile | Profil anzeigen | Ja |
| PUT | /api/users/{id}/profile | Profil ändern | Ja |
| GET | /api/users/{id}/ratings | Rating-Historie | Ja |
| GET | /api/users/{id}/favorites | Favoritenliste | Ja |
| GET | /api/users/{id}/recommendations | Empfehlungen | Ja |
| GET | /api/media | Medienliste / Filter | Ja |
| GET | /api/media/{id} | Ein Medium | Ja |
| POST | /api/media | Medium erstellen | Ja |
| PUT | /api/media/{id} | Medium bearbeiten | Ja |
| DELETE | /api/media/{id} | Medium löschen | Ja |
| POST | /api/media/{id}/rate | Medium bewerten | Ja |
| PUT | /api/ratings/{id} | Rating ändern | Ja |
| DELETE | /api/ratings/{id} | Rating löschen | Ja |
| POST | /api/ratings/{id}/like | Rating liken | Ja |
| POST | /api/ratings/{id}/confirm | Kommentar bestätigen | Ja |
| POST | /api/media/{id}/favorite | Favorit setzen | Ja |
| DELETE | /api/media/{id}/favorite | Favorit entfernen | Ja |
| GET | /api/leaderboard | Leaderboard | Ja |

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

Die Datei `TODO/MRP_Postman_Collection.json` enthält alle API-Requests zum Testen.

Import in Postman: File → Import → Datei auswählen

Wichtig: Nach dem Login den Token als Bearer Token für die Collection setzen.

## Datenbank Schema

```sql
-- Users
CREATE TABLE Users(
    ID SERIAL PRIMARY KEY,
    Username VARCHAR(256) UNIQUE NOT NULL,
    Password VARCHAR(256) NOT NULL,
    Token VARCHAR(256),
    Email VARCHAR(256),
    FavoriteGenre VARCHAR(100)
);

-- Media Entries
CREATE TABLE MediaEntries(
    ID SERIAL PRIMARY KEY,
    Title VARCHAR(256) NOT NULL,
    Description TEXT,
    MediaType VARCHAR(50) NOT NULL,
    ReleaseYear INT,
    AgeRestriction INT,
    CreatorId INT NOT NULL,
    FOREIGN KEY (CreatorId) REFERENCES Users(ID)
);

-- Media Genres (1:n)
CREATE TABLE Media_Genres(
    ID SERIAL PRIMARY KEY,
    MediaId INT NOT NULL,
    Genre VARCHAR(100) NOT NULL,
    UNIQUE (MediaId, Genre),
    FOREIGN KEY (MediaId) REFERENCES MediaEntries(ID) ON DELETE CASCADE
);

-- Ratings
CREATE TABLE Ratings(
    ID SERIAL PRIMARY KEY,
    MediaId INT NOT NULL,
    UserId INT NOT NULL,
    Stars INT NOT NULL CHECK (Stars BETWEEN 1 AND 5),
    Comment TEXT,
    CommentConfirmed BOOLEAN NOT NULL DEFAULT FALSE,
    CreatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (MediaId, UserId),
    FOREIGN KEY (MediaId) REFERENCES MediaEntries(ID) ON DELETE CASCADE,
    FOREIGN KEY (UserId) REFERENCES Users(ID) ON DELETE CASCADE
);

-- Rating Likes
CREATE TABLE Rating_Likes(
    ID SERIAL PRIMARY KEY,
    RatingId INT NOT NULL,
    UserId INT NOT NULL,
    UNIQUE (RatingId, UserId),
    FOREIGN KEY (RatingId) REFERENCES Ratings(ID) ON DELETE CASCADE,
    FOREIGN KEY (UserId) REFERENCES Users(ID) ON DELETE CASCADE
);

-- Favorites
CREATE TABLE Favorites(
    ID SERIAL PRIMARY KEY,
    MediaId INT NOT NULL,
    UserId INT NOT NULL,
    UNIQUE (MediaId, UserId),
    FOREIGN KEY (MediaId) REFERENCES MediaEntries(ID) ON DELETE CASCADE,
    FOREIGN KEY (UserId) REFERENCES Users(ID) ON DELETE CASCADE
);
```

## Tests

Die Unit-Tests liegen unter `src/test/java`.

Tests ausführen:
- IntelliJ: Rechtsklick auf `src/test/java` → Run Tests
- Maven (falls installiert): `mvn test`

## Autor

Mustafa Sarikaya - FH Technikum Wien
