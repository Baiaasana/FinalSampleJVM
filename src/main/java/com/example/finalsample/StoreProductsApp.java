package com.example.finalsample;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StoreProductsApp extends Application {

    private PieChart pieChart;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        // Set up UI components
        pieChart = new PieChart();
        TextField nameTextField = new TextField();
        nameTextField.setPromptText("Name");
        TextField quantityTextField = new TextField();
        quantityTextField.setPromptText("Quantity");
        Button addButton = new Button("Add");
        addButton.setOnAction(event -> addProduct(nameTextField.getText(), quantityTextField.getText()));

        VBox vbox = new VBox(pieChart, nameTextField, quantityTextField, addButton);

        // Set up stage and scene
        Scene scene = new Scene(vbox);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Store Products App");
        primaryStage.show();

        // Load data
        loadProductsFromDatabase();
    }

    private void addProduct(String name, String quantityString) {

        // Validate inputs
        if (name.isEmpty() || quantityString.isEmpty()) {
            System.out.println("Please enter both name and quantity.");
            return;
        }
        int quantity;
        try {
            quantity = Integer.parseInt(quantityString);
        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity format. Please enter a valid integer value.");
            return;
        }

        // Perform database insertion using JDBC
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/store", "baiaasana", "baia@123");
             PreparedStatement statement = connection.prepareStatement("INSERT INTO products (name, quantity) VALUES (?, ?)")) {

            statement.setString(1, name);
            statement.setInt(2, quantity);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Product added successfully!");
            } else {
                System.out.println("Failed to add product.");
            }

            // After adding the product, update piechart.
            loadProductsFromDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void loadProductsFromDatabase() {

        // Use JDBC to connect to the MySQL database
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/store", "baiaasana", "baia@123"); PreparedStatement statement = connection.prepareStatement("SELECT * FROM products"); ResultSet resultSet = statement.executeQuery()) {

            ObservableList<Product> products = FXCollections.observableArrayList();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                int quantity = resultSet.getInt("quantity");
                Product product = new Product(id, name, quantity);
                products.add(product);
            }
            updatePieChart(products);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

//    private void updatePieChart(List<Product> products) {
//
//        // Group products by quantity and update the pie chart
//        Map<String, Long> nameMap = products.stream().collect(Collectors.groupingBy(Product::getName, Collectors.counting()));
//        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
//
//        nameMap.forEach((name, count) -> {
//            String label = name + " (" + count + ")"; // Concatenate name and count
//            pieChartData.add(new PieChart.Data(label, count));
//        });
//        pieChart.setData(pieChartData);
//    }

    private void updatePieChart(List<Product> products) {

        // Group products by name and sum the quantities
        Map<String, Integer> nameMap = products.stream()
                .collect(Collectors.groupingBy(Product::getName, Collectors.summingInt(Product::getQuantity)));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        nameMap.forEach((name, quantity) -> {
            String label = name + " (" + quantity + ")"; // Concatenate name and quantity
            pieChartData.add(new PieChart.Data(label, quantity));
        });

        pieChart.setData(pieChartData);
    }
}
