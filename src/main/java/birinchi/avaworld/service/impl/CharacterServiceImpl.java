package birinchi.avaworld.service.impl;

import birinchi.avaworld.model.dto.WebResponse;
import birinchi.avaworld.service.CharacterService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class CharacterServiceImpl implements CharacterService {
    @Override
    public ResponseEntity<WebResponse> saveCharacter() {
        return null;
    }
}