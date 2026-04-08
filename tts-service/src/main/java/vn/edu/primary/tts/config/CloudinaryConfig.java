package vn.edu.primary.tts.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cloudinary")
@Data
public class CloudinaryConfig {
    private String cloudName;
    private String apiKey;
    private String apiSecret;
    private String folder;
}
