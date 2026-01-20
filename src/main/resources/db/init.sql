-- Users Tabelle für MRP (Media Ratings Platform)
CREATE TABLE IF NOT EXISTS Users(
    ID SERIAL PRIMARY KEY,
    Username VARCHAR(256) UNIQUE NOT NULL,
    Password VARCHAR(256) NOT NULL,
    Token VARCHAR(256),
    Email VARCHAR(256),
    FavoriteGenre VARCHAR(100)
);

-- MediaEntry Tabelle (Filme, Serien, Spiele)
CREATE TABLE IF NOT EXISTS MediaEntries(
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

-- Genres pro Medium (eigenes Mapping, weil ein Medium mehrere Genres haben kann)
CREATE TABLE IF NOT EXISTS Media_Genres(
    ID SERIAL PRIMARY KEY,
    MediaId INT NOT NULL,
    Genre VARCHAR(100) NOT NULL,
    UNIQUE (MediaId, Genre),
    FOREIGN KEY (MediaId) REFERENCES MediaEntries(ID) ON DELETE CASCADE
);

-- Ratings für Medien (ein Rating pro User und Medium)
CREATE TABLE IF NOT EXISTS Ratings(
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

-- Likes pro Rating (pro User nur ein Like)
CREATE TABLE IF NOT EXISTS Rating_Likes(
    ID SERIAL PRIMARY KEY,
    RatingId INT NOT NULL,
    UserId INT NOT NULL,
    UNIQUE (RatingId, UserId),
    FOREIGN KEY (RatingId) REFERENCES Ratings(ID) ON DELETE CASCADE,
    FOREIGN KEY (UserId) REFERENCES Users(ID) ON DELETE CASCADE
);

-- Favoriten pro User (pro Medium nur einmal)
CREATE TABLE IF NOT EXISTS Favorites(
    ID SERIAL PRIMARY KEY,
    MediaId INT NOT NULL,
    UserId INT NOT NULL,
    UNIQUE (MediaId, UserId),
    FOREIGN KEY (MediaId) REFERENCES MediaEntries(ID) ON DELETE CASCADE,
    FOREIGN KEY (UserId) REFERENCES Users(ID) ON DELETE CASCADE
);
