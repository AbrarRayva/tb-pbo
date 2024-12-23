-- Query untuk pembuatan table pada database
CREATE TABLE songs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT, 
    author TEXT, 
    genre TEXT, 
    durationSeconds INTEGER NOT NULL
);