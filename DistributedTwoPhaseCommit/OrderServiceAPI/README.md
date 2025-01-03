Steps to Set Up and Execute the Application:
Set Up SQL Workbench Locally:

Install MySQL Workbench on your local machine if you haven't already.
Update Database Credentials:

Open the db.properties file and update the database username and password to match your local MySQL setup.
Run SQL Commands in MySQL Workbench:

Open MySQL Workbench and run the necessary SQL commands to set up the database schema (e.g., tables for agents and food).
Start the Reserve Agent Class:

Run the ReserveAgent class. This will initialize four API servers locally.
Invoke API Calls Concurrently:

The API servers will handle concurrent requests to book the agent and reserve food.


--------------------------------- SQL to execute in mysql workbench -----------------------------------
create database fooddelivery

-- Create Agent table
CREATE TABLE Agent (
    id INT AUTO_INCREMENT PRIMARY KEY,
    is_reserved BOOLEAN DEFAULT FALSE,
    order_id INT NULL
);

-- Create Food table
CREATE TABLE Food (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

-- Create Packets table
CREATE TABLE Packets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    food_id INT NOT NULL,
    is_reserved BOOLEAN DEFAULT FALSE,
    order_id INT NULL,
    FOREIGN KEY (food_id) REFERENCES Food(id)
);


-- Insert records

INSERT INTO Agent (is_reserved, order_id)
VALUES
(FALSE, NULL),
(FALSE, NULL),
(FALSE, NULL),
(FALSE, NULL),
(FALSE, NULL),
(FALSE, NULL),
(FALSE, NULL),
(FALSE, NULL),
(FALSE, NULL),
(FALSE, NULL);


INSERT INTO Food (id, name)
VALUES
(1, 'Burger');

INSERT INTO Packets (food_id, is_reserved, order_id)
VALUES
(1, FALSE, NULL),
(1, FALSE, NULL),
(1, FALSE, NULL),
(1, FALSE, NULL),
(1, FALSE, NULL),
(1, FALSE, NULL),
(1, FALSE, NULL),
(1, FALSE, NULL),
(1, FALSE, NULL),
(1, FALSE, NULL),
(1, FALSE, NULL),
(1, FALSE, NULL),
(1, FALSE, NULL),
(1, FALSE, NULL),
(1, FALSE, NULL);

-- Handy Queries

 -- SELECT * FROM Agent; 
-- SELECT id FROM Agent WHERE is_reserved = 0 AND order_id is null LIMIT 1 FOR UPDATE
-- Describe agent;
-- Revert changes in Agent table
 --  UPDATE Agent SET is_reserved = false, order_id = NULL WHERE id > 0;

 --  select * from packets;
 -- UPDATE packets SET is_reserved = false, order_id = NULL WHERE id > 0;
-- SELECT id FROM Packets WHERE is_reserved = true AND order_id IS NULL AND food_id = 1 LIMIT 1 FOR UPDATE;





