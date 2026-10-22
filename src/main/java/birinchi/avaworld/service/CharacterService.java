package birinchi.avaworld.service;

import birinchi.avaworld.model.dto.WebResponse;
import org.springframework.http.ResponseEntity;

public interface CharacterService {
    ResponseEntity<WebResponse> saveCharacter();
}
