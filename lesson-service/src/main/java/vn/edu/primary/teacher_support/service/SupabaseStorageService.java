package vn.edu.primary.teacher_support.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String bucketName;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public byte[] downloadFile(String filename) throws IOException, InterruptedException {
        String url = objectUrl(filename);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer " + supabaseKey)
                .GET()
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to download file from Supabase. Status: " + response.statusCode());
        }
        return response.body();
    }

    public void uploadFile(String filename, byte[] content, String contentType) throws IOException, InterruptedException {
        String url = objectUrl(filename);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer " + supabaseKey)
                .header("Content-Type", contentType != null ? contentType : "application/octet-stream")
                .header("x-upsert", "true")
                .POST(HttpRequest.BodyPublishers.ofByteArray(content))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new IOException("Failed to upload file to Supabase. Status: " + response.statusCode() + ", Body: " + response.body());
        }
    }

    public long getFileSize(String filename) throws IOException, InterruptedException {
        String url = supabaseUrl + "/storage/v1/object/list/" + bucketName;
        String jsonBody = "{\"prefix\":\"" + filename + "\",\"limit\":1,\"offset\":0,\"search\":\"" + filename + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer " + supabaseKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            JsonNode root = objectMapper.readTree(response.body());
            if (root.isArray()) {
                for (JsonNode node : root) {
                    if (node.has("name") && filename.equals(node.get("name").asText())) {
                        JsonNode metadata = node.get("metadata");
                        if (metadata != null && metadata.has("size")) {
                            return metadata.get("size").asLong();
                        }
                    }
                }
            }
        }
        return 0;
    }

    private String objectUrl(String filename) {
        return supabaseUrl + "/storage/v1/object/" + bucketName + "/" +
                URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
