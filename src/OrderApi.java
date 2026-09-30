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
