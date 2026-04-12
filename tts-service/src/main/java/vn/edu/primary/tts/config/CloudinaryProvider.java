package vn.edu.primary.tts.config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryProvider {

    @Bean
    public Cloudinary cloudinary(CloudinaryConfig config) {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("cloud_name", config.getCloudName());
        configMap.put("api_key", config.getApiKey());
        configMap.put("api_secret", config.getApiSecret());
        return new Cloudinary(configMap);
    }
}
