package com.rentify.common.response;

import java.util.List;
import org.springframework.data.domain.Page;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private int currentPage;
    private int pageSize;
    private int totalPages;
    private long totalElements;
    private List<T> items;

    // Hàm tiện ích giúp chuyển đổi từ Page của Spring chỉ bằng 1 dòng code
    public static <T> PageResponse<T> fromPage(Page<T> pageData) {
        return PageResponse.<T>builder()
                .currentPage(pageData.getNumber() + 1) // Cộng 1 vì Spring tính trang đầu là 0
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .items(pageData.getContent())
                .build();
    }
}