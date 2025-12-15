-- Users Tabelle für MRP (Media Ratings Platform)
CREATE TABLE IF NOT EXISTS Users(
    ID SERIAL PRIMARY KEY,
    Username VARCHAR(256) UNIQUE NOT NULL,
    Password VARCHAR(256) NOT NULL,
    Token VARCHAR(256)
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
