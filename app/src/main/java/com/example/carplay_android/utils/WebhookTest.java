package com.example.carplay_android.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebhookTest {
    /**
     * Sends data to a Discord webhook URL.
     *
     * @param webhookUrl The URL of the Discord webhook.
     * @param data       The data to send to the webhook.
     * @throws IOException If an error occurs while sending the data.
     */
    private static void sendToWebhook(String webhookUrl, String data) throws IOException {
        HttpURLConnection connection = null;

        try {
            // Create a URL object from the webhook URL string
            URL url = new URL(webhookUrl);

            // Open a connection to the webhook URL
            connection = (HttpURLConnection) url.openConnection();

            // Set the request method to POST
            connection.setRequestMethod("POST");

            // Set the content type header to application/json
            connection.setRequestProperty("Content-Type", "application/json");

            // Enable output for sending data
            connection.setDoOutput(true);

            // Write the data to the connection's output stream
            connection.getOutputStream().write(data.getBytes());

            // Get the response code
            int responseCode = connection.getResponseCode();

            // If the response code is not 204 (No Content), throw an exception
            if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
                throw new IOException("POST request failed with response code: " + responseCode);
            }
        } finally {
            // Close the connection
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
