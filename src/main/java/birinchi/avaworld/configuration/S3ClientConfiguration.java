package birinchi.avaworld.configuration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class S3ClientConfiguration {

    @Value("${s3.access-key}")
    private String accessKey;

    @Value("${s3.secret-key}")
    private String secretKey;

    @Value("${s3.aws.region}")
    private String awsRegion;

    @Bean
    public AmazonS3 amazonS3() {
        try {
            BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);
            return AmazonS3ClientBuilder.standard()
                    .withRegion(awsRegion)
                    .withCredentials(new AWSStaticCredentialsProvider(creds))
                    .build();
        } catch (RuntimeException e) {
            throw new RuntimeException("Error during connect", e);
        }
    }
}