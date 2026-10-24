package birinchi.avaworld.service.impl;

import birinchi.avaworld.model.entity.WhiteListCounterparty;
import birinchi.avaworld.repository.WhiteListCounterpartyRepository;
import birinchi.avaworld.service.WhiteListService;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhiteListServiceImpl implements WhiteListService {

    @Value("${s3.bucket-name}")
    private String bucketName;
    private final Long personID = 1l;
    private final String clientName = "black-list";
    private final AmazonS3 amazonS3;

//    private static final Set<String> ALLOWED_TYPES =
//            Set.of("image/jpeg", "image/png", "application/pdf");

//        if (sentImageToS3(front)) log.info("Sent front image was successful");
//        if (sentImageToS3(back)) log.info("Sent back image was successful");


    private final WhiteListCounterpartyRepository repository;

    @Transactional
    public Long create(String name, MultipartFile front, MultipartFile back) throws IOException {
        if (front.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Файл пустой");
        }

        String frontKeyName = sentImageToS3(front);
        String backKeyName = sentImageToS3(back);
        WhiteListCounterparty entity = WhiteListCounterparty.builder()
                .frontUri(frontKeyName)
                .backUri(backKeyName)
                .name(name)
                .build();

        return repository.save(entity).getId();
    }

    private String sentImageToS3(MultipartFile file) throws IOException {
        String keyName = getKeyName(personID);
        PutObjectResult result;
        try (InputStream inputStream = file.getInputStream()) {
            result = amazonS3.putObject(new PutObjectRequest(bucketName, keyName, inputStream, createMetadata(file, clientName)));
            log.info("versionID : {}", result.getVersionId());
        } catch (RuntimeException e) {
            throw new IOException("Error: " + e.getMessage());
        }
        return keyName;
    }

    private ObjectMetadata createMetadata(MultipartFile file, String clientName) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        if (file.getContentType() != null) {
            metadata.setContentType(file.getContentType());
        }
        metadata.addUserMetadata("fileName", URLEncoder.encode(Objects.requireNonNull(file.getOriginalFilename()), StandardCharsets.UTF_8));
        metadata.addUserMetadata("createdDate", URLEncoder.encode(String.valueOf(Instant.now()), StandardCharsets.UTF_8));
        metadata.addUserMetadata("clientName", clientName);

        return metadata;
    }

    private String getKeyName(Long personID) {
        String keyName = "black-list/" + Objects.requireNonNull(personID) + "/" + UUID.randomUUID();
        return keyName;
    }


    public ResponseEntity<Map<String, String>> get(Long id) {
        WhiteListCounterparty entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Map<String, String> res = new HashMap<>();

        if (entity.getFrontUri() != null && entity.getBackUri() != null) {
            res.put("front", makeBase64FromDoc(getDocFromS3(entity.getFrontUri())));
            res.put("back", makeBase64FromDoc(getDocFromS3(entity.getBackUri())));
            return ResponseEntity.ok(res);
        }
        res.put("Empty Key", "Empty data");
        return ResponseEntity.ok(res);
    }

    @Override
    public S3Object getDocFromS3(String docUri) {
        S3Object s3Object;
        try {
            s3Object = amazonS3.getObject(bucketName, docUri);
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 404) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found in storage");
            }
            throw e;
        }
        return s3Object;
    }

    private String makeBase64FromDoc(S3Object s3Object) {
        try (S3ObjectInputStream in = s3Object.getObjectContent()) {
            return Base64.getEncoder().encodeToString(in.readAllBytes());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read document from S3", e);
        }
    }
}