import java.util.Scanner;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserInputWithApiRequestsAndStreams {
    public static void main(String[] args) {
        // Create a Scanner object to read user input
        Scanner scanner = new Scanner(System.in);

        // Prompt the user to enter their name
        System.out.print("Enter your name: ");
        String name = scanner.nextLine(); // Read user's name

        // Get a set of valid city names
        Set<String> validCities = getValidCities();

        String departureCity;
        while (true) {
            // Prompt the user to enter their departure city
            System.out.print("Enter your departure city: ");
            departureCity = scanner.nextLine(); // Read user's departure city

            // Check if the entered city is valid
            if (validCities.contains(departureCity)) {
                break; // Exit the loop if the city is valid
            } else {
                System.out.println("Unrecognized city. Please enter a valid city.");
            }
        }

        // Display user's name and departure city
        System.out.println("Name: " + name);
        System.out.println("Departure City: " + departureCity);

        // Close the scanner to release resources
        scanner.close();

        // Use streams and CompletableFuture for concurrent API requests
        List<CompletableFuture<String>> apiRequests = Stream.generate(() -> CompletableFuture.supplyAsync(() ->
            performApiRequest(departureCity), getExecutorService())
        )
        .limit(1000)
        .collect(Collectors.toList()); // Generate a list of CompletableFuture objects for API requests

        List<String> apiResponses;
        try {
            // Wait for all API requests to complete and collect their results
            apiResponses = CompletableFuture.allOf(apiRequests.toArray(new CompletableFuture[0]))
                .thenApply(v -> apiRequests.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList())
                )
                .get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return;
        }

        // Process the API responses using streams
        apiResponses.stream()
            .forEach(response -> System.out.println("API Response: " + response));

        // Filter valid cities using streams
        List<String> validApiResponses = apiResponses.stream()
            .filter(response -> response.contains(departureCity))
            .collect(Collectors.toList());

        // Display valid API responses
        System.out.println("Valid API Responses: " + validApiResponses);
    }

    // Define a set of valid city names
    private static Set<String> getValidCities() {
        Set<String> validCities = new HashSet<>();
        validCities.add("Los Angeles");
        validCities.add("New York");
        validCities.add("Chicago");
        // Add more valid cities here
        return validCities;
    }

    // Simulate an API request and return a response
    private static String performApiRequest(String city) {
        // Simulate API request with a delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "API response for " + city;
    }

    // Create an ExecutorService for concurrent execution of tasks
    private static ExecutorService getExecutorService() {
        return Executors.newFixedThreadPool(10); // Customize pool size as needed
    }
}

