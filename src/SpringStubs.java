import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Minimal stand-ins for the Spring Web annotations and ResponseEntity,
// so OrderApi compiles without Spring on the classpath.

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE)
@interface RestController {}

@Retention(RetentionPolicy.RUNTIME) @Target({ElementType.TYPE, ElementType.METHOD})
@interface RequestMapping { String value() default ""; }

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
@interface GetMapping { String value() default ""; }

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
@interface PostMapping { String value() default ""; }

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.PARAMETER)
@interface PathVariable { String value() default ""; }

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.PARAMETER)
@interface RequestBody {}

final class ResponseEntity<T> {
    private final int status;
    private final T body;

    private ResponseEntity(int status, T body) {
        this.status = status;
        this.body = body;
    }

    static <T> ResponseEntity<T> ok(T body) { return new ResponseEntity<>(200, body); }
    static <T> ResponseEntity<T> status(int status, T body) { return new ResponseEntity<>(status, body); }
    static <T> ResponseEntity<T> notFound() { return new ResponseEntity<>(404, null); }
    static <T> ResponseEntity<T> badRequest() { return new ResponseEntity<>(400, null); }

    int getStatusCode() { return status; }
    T getBody() { return body; }
}
