package birinchi.avaworld.service;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface WhiteListService {
    Long create(String name, MultipartFile front, MultipartFile back) throws IOException;

    ResponseEntity<InputStreamResource> get(Long id);
}
