package it.unina.bugboard26.dto.response;

import org.springframework.data.domain.Page;
import java.util.List;

public record PagedResponse<T>(
        List<T> data,
        long total,
        int page,
        int pageSize
) {
    public static <T> PagedResponse<T> of(Page<T> page) {
        return new PagedResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getNumber(),
                page.getSize()
        );
    }
}
