package birinchi.avaworld.service.impl;

import birinchi.avaworld.model.entity.WhiteListCounterparty;
import birinchi.avaworld.repository.WhiteListCounterpartyRepository;
import birinchi.avaworld.service.WhiteListService;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

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

//    @Transactional(readOnly = true)
//    public ResponseEntity<InputStreamResource> get(Long id) {
//        Optional<WhiteListCounterparty> result = Optional.of(repository.findById(id)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
//        try {
//            S3Object s3Object = amazonS3.getObject(bucketName, result.get().getFrontUri());
//            return ResponseEntity.ok()
//                    .contentType(MediaType.parseMediaType(s3Object.getObjectMetadata().getUserMetadata().get("contentType")
//                    ))
//                    .header("Content-Disposition", "inline: fileName=\"" +
//                            s3Object.getObjectMetadata().getUserMetadata().get("fileName") + "\"")
//                    .body(new InputStreamResource((InputStreamSource) s3Object.getObjectMetadata().getUserMetadata()));
//        } catch (Exception e) {
//            throw new RuntimeException("Error", e);
//        }
//    }

    public ResponseEntity<InputStreamResource> get(Long id) {
        WhiteListCounterparty entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        S3Object s3Object;
        try {
            s3Object = amazonS3.getObject(bucketName, entity.getFrontUri());
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 404) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found in storage");
            }
            throw e;
        }

        ObjectMetadata meta = s3Object.getObjectMetadata();

        String contentType = meta.getContentType() != null
                ? meta.getContentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        String encodedName = meta.getUserMetadata().get("filename");
        String fileName = encodedName != null
                ? URLDecoder.decode(encodedName, StandardCharsets.UTF_8)
                : "file";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(meta.getContentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(fileName, StandardCharsets.UTF_8).build().toString())
                .body(new InputStreamResource(s3Object.getObjectContent()));
    }
}