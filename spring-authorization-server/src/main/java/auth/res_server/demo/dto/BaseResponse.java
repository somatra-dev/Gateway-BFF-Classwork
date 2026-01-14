package auth.res_server.demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {

    private boolean success;
    private int status;
    private String message;
    private T data;
    private Instant timestamp;
    private String path;

    // For pagination metadata
    private PageMeta meta;

    // For validation errors: field -> error message
    private Map<String, String> errors;

    // ==================== Success Responses ====================

    public static <T> BaseResponse<T> success(T data, String message, int status, String path) {
        return BaseResponse.<T>builder()
                .success(true)
                .status(status)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .path(path)
                .build();
    }

    public static <T> BaseResponse<T> success(T data, String path) {
        return success(data, "Success", 200, path);
    }

    public static <T> BaseResponse<T> created(T data, String path) {
        return success(data, "Created successfully", 201, path);
    }

    public static BaseResponse<Void> success(String message, int status, String path) {
        return BaseResponse.<Void>builder()
                .success(true)
                .status(status)
                .message(message)
                .timestamp(Instant.now())
                .path(path)
                .build();
    }

    public static BaseResponse<Void> ok(String message, String path) {
        return success(message, 200, path);
    }

    public static BaseResponse<Void> noContent(String path) {
        return success("No content", 204, path);
    }

    public static <T> BaseResponse<T> success(T data, PageMeta meta, String path) {
        return BaseResponse.<T>builder()
                .success(true)
                .status(200)
                .message("Success")
                .data(data)
                .meta(meta)
                .timestamp(Instant.now())
                .path(path)
                .build();
    }

    // ==================== Error Responses ====================

    public static BaseResponse<Void> error(String message, int status, String path) {
        return BaseResponse.<Void>builder()
                .success(false)
                .status(status)
                .message(message)
                .timestamp(Instant.now())
                .path(path)
                .build();
    }

    public static BaseResponse<Void> error(String message, int status, String path, Map<String, String> errors) {
        return BaseResponse.<Void>builder()
                .success(false)
                .status(status)
                .message(message)
                .errors(errors)
                .timestamp(Instant.now())
                .path(path)
                .build();
    }

    public static BaseResponse<Void> badRequest(String message, String path) {
        return error(message, 400, path);
    }

    public static BaseResponse<Void> badRequest(String message, String path, Map<String, String> errors) {
        return error(message, 400, path, errors);
    }

    public static BaseResponse<Void> unauthorized(String message, String path) {
        return error(message, 401, path);
    }

    public static BaseResponse<Void> forbidden(String message, String path) {
        return error(message, 403, path);
    }

    public static BaseResponse<Void> notFound(String message, String path) {
        return error(message, 404, path);
    }

    public static BaseResponse<Void> conflict(String message, String path) {
        return error(message, 409, path);
    }

    public static BaseResponse<Void> locked(String message, String path) {
        return error(message, 423, path);
    }

    public static BaseResponse<Void> internalError(String path) {
        return error("An unexpected error occurred", 500, path);
    }

    // ==================== Pagination Meta ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageMeta {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;

        public static PageMeta of(org.springframework.data.domain.Page<?> page) {
            return PageMeta.builder()
                    .page(page.getNumber())
                    .size(page.getSize())
                    .totalElements(page.getTotalElements())
                    .totalPages(page.getTotalPages())
                    .first(page.isFirst())
                    .last(page.isLast())
                    .build();
        }
    }
}
