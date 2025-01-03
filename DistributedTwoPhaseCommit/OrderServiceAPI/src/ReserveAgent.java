import com.sun.net.httpserver.HttpServer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.sql.*;
import java.util.Properties;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ReserveAgent {

    private static HikariDataSource dataSource;

    // copy path for dbproperties file
    private final static String dbproperties = "";
    
    public static void main(String[] args) throws Exception {

        // Start HTTP Servers
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
        System.out.println("Reserve Agent is running on port 8081...");
        server.createContext("/reserveAgent", new ReserveAgentHandler());
        server.setExecutor(null);
        server.start();

        HttpServer server2 = HttpServer.create(new InetSocketAddress(8082), 0);
        System.out.println("Book Agent Service is running on port 8082...");
        server2.createContext("/bookAgent", new BookAgentHandler());
        server2.setExecutor(null);
        server2.start();

        HttpServer server3 = HttpServer.create(new InetSocketAddress(8083), 0);
        System.out.println("FoodService is running on port 8083...");
        server3.createContext("/reserveFood", new ReserveFoodHandler());
        server3.setExecutor(null);
        server3.start();

        HttpServer server4 = HttpServer.create(new InetSocketAddress(8084), 0);
        System.out.println("BookFoodService is running on port 8084...");
        server4.createContext("/bookFood", new BookFoodHandler());
        server4.setExecutor(null);
        server4.start();

        // Concurrently place orders
        processOrders(20);

        // String bookAgentResponse = callApi("http://localhost:8082/bookAgent", "123");
        // System.out.println("Book Agent Response: " + bookAgentResponse);
        
    }

    public static void processOrders(int concurrentOrders) throws InterruptedException, ExecutionException {
        // Executor service for concurrency
        ExecutorService executorService = Executors.newFixedThreadPool(5); // Limit concurrent threads
        CompletionService<Boolean> completionService = new ExecutorCompletionService<>(executorService);

        // Submit tasks
        for (int i = 1; i <= concurrentOrders; i++) {
            final String orderId = String.valueOf(i); // Unique order ID
            completionService.submit(() -> executeApiWorkflow(orderId));
        }

        // Collect results
        for (int i = 0; i < concurrentOrders; i++) {
            Future<Boolean> future = completionService.take(); // Wait for task completion
            if (future.get()) {
                System.out.println("Order workflow completed successfully.");
            } else {
                System.out.println("Order workflow failed.");
            }
        }

        executorService.shutdown();
    }

    private static boolean executeApiWorkflow(String orderId) {
        System.out.println("-------------" + orderId + "-----------------------");
        
        try {
            // Call Reserve Agent API
            String reserveAgentResponse = callApi("http://localhost:8081/reserveAgent", orderId);
            System.out.println("Reserve Agent Response: " + reserveAgentResponse);
            if (!isSuccess(reserveAgentResponse)) {
                System.out.println("Reserve Agent failed for Order ID: " + orderId);
                return false;
            }
    
            // Call Book Agent API
            String bookAgentResponse = callApi("http://localhost:8082/bookAgent", orderId);
            System.out.println("Book Agent Response: " + bookAgentResponse);
            if (!isSuccess(bookAgentResponse)) {
                System.out.println("Book Agent failed for Order ID: " + orderId);
                return false;
            }
    
            // Call Reserve Food API
            String reserveFoodResponse = callApi("http://localhost:8083/reserveFood", orderId);
            System.out.println("Reserve Food Response: " + reserveFoodResponse);
            if (!isSuccess(reserveFoodResponse)) {
                System.out.println("Reserve Food failed for Order ID: " + orderId);
                return false;
            }
    
            // Call Book Food API
            String bookFoodResponse = callApi("http://localhost:8084/bookFood", orderId);
            System.out.println("Book Food Response: " + bookFoodResponse);
            if (!isSuccess(bookFoodResponse)) {
                System.out.println("Book Food failed for Order ID: " + orderId);
                return false;
            }
    
            // If all calls succeed
            return true;
    
        } catch (Exception e) {
            // Handle any exceptions that may occur
            e.printStackTrace();
            return false;
        }
    }
    
    // Helper method to determine if a response is successful (you can adjust this based on your API response format)
    private static boolean isSuccess(String response) {
        // Assuming the response contains "success" or "200 OK" in case of success
        return response != null && response.contains("success") || response.contains("200 OK");
    }
    

    private static String callApi(String apiUrl, String orderId) throws Exception {
        // Make POST request to the API
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");

        // Send order ID in the request body
        try (OutputStream os = connection.getOutputStream()) {
            os.write(orderId.getBytes());
            os.flush();
        }

        // Read response
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            try (InputStream is = connection.getInputStream()) {
                return new String(is.readAllBytes());
            }
        } else {
            System.err.println(connection.getResponseMessage());
            return "Failed with HTTP error code: " + responseCode;
        }
    }

    static class BookFoodHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response;
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                try {
                    // Parse the input order ID from the request body
                    BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
                    String requestBody = reader.readLine();
                    int orderId = Integer.parseInt(requestBody); // Order ID from API input

                    // Book the food packet
                    boolean success = bookFood(orderId);

                    if (success) {
                        response = "Food packet booked successfully for order ID: " + orderId;
                        exchange.sendResponseHeaders(200, response.getBytes().length);
                    } else {
                        response = "No available food packet found.";
                        exchange.sendResponseHeaders(404, response.getBytes().length);
                    }
                } catch (Exception e) {
                    response = "Error: " + e.getMessage();
                    exchange.sendResponseHeaders(500, response.getBytes().length);
                }
            } else {
                response = "Only POST method is supported.";
                exchange.sendResponseHeaders(405, response.getBytes().length);
            }

            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private boolean bookFood(int orderId) throws Exception {
            // Load database configuration
            Properties properties = new Properties();
            try (InputStream input = new FileInputStream(dbproperties)) {
                properties.load(input);
            } catch (IOException ex) {
                throw new RuntimeException("Failed to load database configuration.", ex);
            }
        
            String dbUrl = properties.getProperty("db.url");
            String username = properties.getProperty("db.username");
            String password = properties.getProperty("db.password");
        
            try (Connection connection = dataSource.getConnection()) {
                connection.setAutoCommit(false); // Start a transaction
                
                String query = "SELECT id FROM Packets WHERE is_reserved = true AND order_id IS NULL AND food_id = 1 LIMIT 1 FOR UPDATE";
                String updateQuery = "UPDATE Packets SET is_reserved = false, order_id = ? WHERE id = ?";
        
                try (PreparedStatement selectStmt = connection.prepareStatement(query);
                     ResultSet rs = selectStmt.executeQuery()) {
        
                    if (rs.next()) {
                        int packetId = rs.getInt("id");
        
                        // Update the packet
                        try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                            updateStmt.setInt(1, orderId);
                            updateStmt.setInt(2, packetId);
        
                            int rowsUpdated = updateStmt.executeUpdate();
                            if (rowsUpdated > 0) {
                                connection.commit(); // Commit the transaction
                                return true;
                            }
                        }
                    }
                }
                connection.rollback(); // Rollback transaction if no packet was booked
                return false;
            }
        }
    }

    static class ReserveFoodHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response;
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                try {
                    // Reserve food with foodId = 1
                    int foodId = 1; // Fixed foodId
                    boolean success = reserveFood(foodId);

                    if (success) {
                        response = "Food packet reserved successfully!";
                        exchange.sendResponseHeaders(200, response.getBytes().length);
                    } else {
                        response = "No available packet found.";
                        exchange.sendResponseHeaders(404, response.getBytes().length);
                    }
                } catch (Exception e) {
                    response = "Error: " + e.getMessage();
                    exchange.sendResponseHeaders(500, response.getBytes().length);
                }
            } else {
                response = "Only POST method is supported.";
                exchange.sendResponseHeaders(405, response.getBytes().length);
            }

            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private boolean reserveFood(int foodId) throws Exception {
            // Load database configuration
            Properties properties = new Properties();
            try (InputStream input = new FileInputStream("C:\\Users\\shrushti\\Documents\\MyGithub\\POC\\DistributedTwoPhaseCommit\\OrderServiceAPI\\src\\db.properties")) {
                properties.load(input);
            } catch (IOException ex) {
                ex.printStackTrace();
                throw new RuntimeException("Failed to load database configuration.", ex);
            }

            String dbUrl = properties.getProperty("db.url");
            String username = properties.getProperty("db.username");
            String password = properties.getProperty("db.password");

            // Connect to database and perform transaction
            try (Connection connection = dataSource.getConnection()) {
                connection.setAutoCommit(false); // Start a transaction
                

                String query = "SELECT id FROM Packets WHERE is_reserved = 0 AND order_id IS NULL AND food_id = ? LIMIT 1 FOR UPDATE";
                String updateQuery = "UPDATE Packets SET is_reserved = 1 WHERE id = ?";

                try (PreparedStatement selectStmt = connection.prepareStatement(query)) {
                    selectStmt.setInt(1, foodId);

                    try (ResultSet rs = selectStmt.executeQuery()) {
                        if (rs.next()) {
                            int packetId = rs.getInt("id");

                            // Update the packet
                            try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                                updateStmt.setInt(1, packetId);

                                int rowsUpdated = updateStmt.executeUpdate();
                                if (rowsUpdated > 0) {
                                    connection.commit(); // Commit the transaction
                                    return true;
                                }
                            }
                        }
                    }
                }

                connection.rollback(); // Rollback transaction if no packet was reserved
                return false;
            }
        }
    }

    static class BookAgentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response;
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                try {
                    // Parse the input order ID from the request body
                    BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
                    String requestBody = reader.readLine();
                    int orderId = Integer.parseInt(requestBody); // Example: Order ID from API input
    
                    // Book the agent
                    boolean success = bookAgent(orderId);
    
                    if (success) {
                        response = "Agent booked successfully for order ID: " + orderId;
                        exchange.sendResponseHeaders(200, response.getBytes().length);
                    } else {
                        response = "No available agent found.";
                        exchange.sendResponseHeaders(404, response.getBytes().length);
                    }
                } catch (Exception e) {
                    // Print full stack trace to console for debugging
                    e.printStackTrace();
    
                    // Send detailed error response to API caller
                    response = "Error occurred: " + e.getClass().getName() + " - " + e.getMessage() + "\n";
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    response += "Stack Trace:\n" + sw.toString();
                    
                    exchange.sendResponseHeaders(500, response.getBytes().length);
                }
            } else {
                response = "Only POST method is supported.";
                exchange.sendResponseHeaders(405, response.getBytes().length);
            }
    
            // Log the response (for debugging purposes)
            System.err.println("Book Agent response : " + response);
    
            // Send the response body back to the client
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    
        private boolean bookAgent(int orderId) throws Exception {
            // Load database configuration
            Properties properties = new Properties();
            try (InputStream input = new FileInputStream("C:\\Users\\shrushti\\Documents\\MyGithub\\POC\\DistributedTwoPhaseCommit\\OrderServiceAPI\\src\\db.properties")) {
                // Load properties file
                properties.load(input);
            } catch (IOException ex) {
                ex.printStackTrace();
                throw new RuntimeException("Failed to load database configuration.", ex);
            }
    
            String dbUrl = properties.getProperty("db.url");
            String username = properties.getProperty("db.username");
            String password = properties.getProperty("db.password");
    
            // Connect to database
            try (Connection connection = DriverManager.getConnection(dbUrl, username, password)) {
                // Start a transaction
                connection.setAutoCommit(false);
    
                // Lock and fetch the first available agent with `is_reserved = true` and `order_id IS NULL`
                String selectQuery = "SELECT id FROM Agent WHERE is_reserved = true AND order_id IS NULL LIMIT 1 FOR UPDATE";
                String updateQuery = "UPDATE Agent SET is_reserved = false, order_id = ? WHERE id = ?";
    
                try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
                     ResultSet rs = selectStmt.executeQuery()) {
    
                    if (rs.next()) {
                        int agentId = rs.getInt("id");
    
                        // Update the agent with the provided order ID
                        try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                            updateStmt.setInt(1, orderId);
                            updateStmt.setInt(2, agentId);
                            int rowsUpdated = updateStmt.executeUpdate();
    
                            // Commit the transaction if the update was successful
                            if (rowsUpdated > 0) {
                                connection.commit();
                                return true;
                            }
                        }
                    }
                } catch (Exception e) {
                    connection.rollback();
                    throw e;
                }
    
                // Rollback the transaction if no agent was available or update failed
                connection.rollback();
                return false;
            } catch (SQLException e) {
                // Log the SQLException stack trace and rethrow
                e.printStackTrace();
                throw new RuntimeException("Database connection or query error", e);
            }
        }
    }
    

    static class ReserveAgentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response;
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                try {
                    // Reserve an agent
                    int orderId = 1; // Example: Order ID to reserve the agent
                    boolean success = reserveAgent(orderId);

                    if (success) {
                        response = "Agent reserved successfully!";
                        exchange.sendResponseHeaders(200, response.getBytes().length);
                    } else {
                        response = "No available agent found.";
                        exchange.sendResponseHeaders(404, response.getBytes().length);
                    }
                } catch (Exception e) {
                    response = "Error: " + e.getMessage();
                    exchange.sendResponseHeaders(500, response.getBytes().length);
                }
            } else {
                response = "Only POST method is supported.";
                exchange.sendResponseHeaders(405, response.getBytes().length);
            }

            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private boolean reserveAgent(int orderId) throws Exception {
            // Load database configuration
            Properties properties = new Properties();
            try (InputStream input = new FileInputStream("C:\\Users\\shrushti\\Documents\\MyGithub\\POC\\DistributedTwoPhaseCommit\\OrderServiceAPI\\src\\db.properties")) {
                // Load properties file
                properties.load(input);
    
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            String dbUrl = properties.getProperty("db.url");
            String username = properties.getProperty("db.username");
            String password = properties.getProperty("db.password");

            // Connect to database
            try (Connection connection = dataSource.getConnection()) {
                // Start a transaction
                connection.setAutoCommit(false); 

                // Lock and fetch the first available agent with the same query
                String query = "SELECT id FROM Agent WHERE is_reserved = 0 AND order_id is null LIMIT 1 FOR UPDATE";
                String updateQuery = "UPDATE Agent SET is_reserved = 1 WHERE id = ?";

                try (PreparedStatement selectStmt = connection.prepareStatement(query);
                     ResultSet rs = selectStmt.executeQuery()) {

                    if (rs.next()) {
                        int agentId = rs.getInt("id");

                        // Now update the agent in the same transaction
                        try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                        
                            updateStmt.setInt(1, agentId);
                            int rowsUpdated = updateStmt.executeUpdate();

                            // Commit the transaction if the update was successful
                            if (rowsUpdated > 0) {
                                connection.commit();
                                return true;
                            }
                        }
                    }
                }

                // Rollback transaction if no agent was available or update failed
                connection.rollback();
                return false;
            }
        }
    }

    // Initialize the HikariCP connection pool
    static {
        HikariConfig config = new HikariConfig();
        try {
             Properties properties = new Properties();
            try (InputStream input = new FileInputStream("C:\\Users\\shrushti\\Documents\\MyGithub\\POC\\DistributedTwoPhaseCommit\\OrderServiceAPI\\src\\db.properties")) {
                properties.load(input);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load database configuration.", e);
            }

            // Set up the HikariCP connection pool with the properties from db.properties
            config.setJdbcUrl(properties.getProperty("db.url"));
            config.setUsername(properties.getProperty("db.username"));
            config.setPassword(properties.getProperty("db.password"));
            config.setMaximumPoolSize(10); // Configure pool size (can be adjusted based on needs)

            // Create the data source for the connection pool
            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load database configuration.", e);
        }
    }

}
