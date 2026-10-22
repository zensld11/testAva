package birinchi.avaworld.model.dto;

import lombok.Builder;

@Builder
public record WebResponse(
        String message,
        Object data,
        Integer totalPage,
        Integer totalElement
) {
}