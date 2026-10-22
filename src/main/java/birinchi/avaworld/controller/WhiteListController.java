package birinchi.avaworld.controller;


import birinchi.avaworld.model.entity.WhiteListCounterparty;
import birinchi.avaworld.service.WhiteListService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController()
@RequestMapping("/test")
@RequiredArgsConstructor
public class WhiteListController {
    private final WhiteListService service;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> create(@RequestParam("name") String name,
                                       @RequestParam("front") MultipartFile front,
                                       @RequestParam("back") MultipartFile back) throws IOException {
        Long id = service.create(name, front, back);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

//    @GetMapping("/{id}/document")
//    public ResponseEntity<byte[]> getDocument(@PathVariable Long id) {
//        WhiteListCounterparty entity = service.get(id);
//        byte[] data = entity.getDocument();
//
//        return ResponseEntity.ok()
//                .contentType(detectType(data))
//                .body(data);
//    }

    @GetMapping("/{id}/document")
    public ResponseEntity<InputStreamResource> getDocument(@PathVariable Long id) {
        return service.get(id);
    }

    private MediaType detectType(byte[] data) {
        if (data.length > 3 && data[0] == (byte) 0xFF && data[1] == (byte) 0xD8) {
            return MediaType.IMAGE_JPEG;
        }
        if (data.length > 4 && data[0] == (byte) 0x89 && data[1] == 'P') {
            return MediaType.IMAGE_PNG;
        }
        if (data.length > 4 && data[0] == '%' && data[1] == 'P') {
            return MediaType.APPLICATION_PDF;
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}