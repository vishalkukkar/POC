Food Delivery API Setup and Execution
Steps to Set Up and Execute the Application
1. Set Up SQL Workbench Locally
Install MySQL Workbench on your local machine if you haven’t done so already.
2. Update Database Credentials
Open the db.properties file and update the database username and password with your local MySQL credentials.
3. Run SQL Commands in MySQL Workbench
Open MySQL Workbench and execute the following SQL commands to set up the necessary database schema, including tables for agents, food, and packets.
4. Start the Reserve Agent Class
Run the ReserveAgent class. This will start four API servers locally to handle the requests.
5. Invoke API Calls Concurrently
The initialized API servers will process concurrent API calls for booking the agent and reserving food.


SQL to Execute in MySQL Workbench
Execute the following SQL commands in MySQL Workbench to set up the required database and tables:


-- Create the FoodDelivery database
CREATE DATABASE fooddelivery;

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


Insert Sample Records:


-- Insert sample data into Agent table
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

-- Insert sample data into Food table
INSERT INTO Food (id, name)
VALUES
(1, 'Burger');

-- Insert sample data into Packets table
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



Handy Queries
Use these queries to interact with the database and check the status of your tables:


-- View all records from Agent table
SELECT * FROM Agent;

-- Select an available agent for reservation
SELECT id FROM Agent WHERE is_reserved = 0 AND order_id IS NULL LIMIT 1 FOR UPDATE;

-- View the structure of the Agent table
DESCRIBE Agent;

-- Revert changes to the Agent table (e.g., unreserve agents)
UPDATE Agent SET is_reserved = false, order_id = NULL WHERE id > 0;

-- View all records from the Packets table
SELECT * FROM Packets;

-- Revert changes to the Packets table (e.g., unreserve packets)
UPDATE Packets SET is_reserved = false, order_id = NULL WHERE id > 0;

-- Select an available packet for reservation
SELECT id FROM Packets WHERE is_reserved = true AND order_id IS NULL AND food_id = 1 LIMIT 1 FOR UPDATE;
