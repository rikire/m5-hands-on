import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
public class OrderApi {
    private final OrderService service;

    public OrderApi(OrderService service) {
        this.service = service;
    }

    @GetMapping
    public List<OrderDto> list() {
        return service.listAll().stream().map(OrderDto::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> get(@PathVariable("id") String id) {
        Optional<Order> o = service.findById(id);
        if (o.isEmpty()) {
            return ResponseEntity.notFound();
        }
        return ResponseEntity.ok(OrderDto.from(o.get()));
    }

    /**
     * Create a new order for a customer and return it.
     *
     * <p>Only {@code customerId} and {@code amount} are read from the request.
     * Any {@code id} or {@code status} the client sends is ignored: the server
     * always assigns a new unique id and the initial status {@code "NEW"}.
     * The call is not idempotent, so each successful call creates a separate
     * order, even when the request is identical to an earlier one.
     *
     * <p>{@code customerId} is not checked against any customer registry and
     * is stored exactly as sent, without trimming.
     *
     * <p>This method throws no exceptions: invalid input gets a 400 response.
     * TODO: errors from request-body deserialization (malformed JSON, a
     * non-numeric amount) happen before this method runs; how they are mapped
     * to HTTP responses is not visible in this code.
     *
     * @param req the order to create ({@link OrderDto}); must not be
     *            {@code null}. {@code req.customerId()} must be non-null and
     *            not blank. {@code req.amount()} must be non-null and strictly
     *            greater than zero. There is no upper limit or scale limit
     *            on the amount.
     * @return a response with status {@code 201 Created} whose body is the
     *         created order as an {@link OrderDto}, including its generated
     *         {@code id} and status {@code "NEW"}; or {@code 400 Bad Request}
     *         with a {@code null} body if any of the conditions on
     *         {@code req} is not met. No {@code Location} header is set.
     */
    @PostMapping
    public ResponseEntity<OrderDto> create(@RequestBody OrderDto req) {
        if (req == null || req.customerId() == null || req.customerId().isBlank()
                || req.amount() == null || req.amount().compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest();
        }
        Order o = service.create(req.customerId(), req.amount());
        return ResponseEntity.status(201, OrderDto.from(o));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderDto> cancel(@PathVariable("id") String id) {
        Optional<Order> o = service.findById(id);
        if (o.isEmpty()) {
            return ResponseEntity.notFound();
        }
        if (!service.cancel(id)) {
            return ResponseEntity.status(409, null);
        }
        return ResponseEntity.ok(OrderDto.from(o.get()));
    }
}
