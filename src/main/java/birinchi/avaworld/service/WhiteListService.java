package birinchi.avaworld.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface WhiteListService {
    Long create(String name, MultipartFile front, MultipartFile back) throws IOException;

    ResponseEntity<Map<String, String>> get(Long id);
}
