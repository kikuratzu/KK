package K.K.Controllers;

import K.K.K.DTOs.AnonymousOrderDTO;
import K.K.K.DTOs.OrderDTO;
import K.K.K.DTOs.ProductDTO;
import K.K.Services.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5500"}, allowCredentials = "true")
@RestController
@RequestMapping("api/")
@Slf4j
public class OrderController {

    private final OrderService service;

    @Autowired
    public OrderController(OrderService service) {
        this.service = service;
    }

    // ==========================================
    //          REDIS SHOPPING CART ENDPOINTS
    // ==========================================

    /**
     * Fetch active cart items from Redis.
     * Pass 'isAnonymous=true' for guests using their browser-generated UUID.
     */
    @GetMapping("cartItems/{id}")
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public List<ProductDTO> getCartItems(
            @PathVariable("id") final UUID id,
            @RequestParam(value = "isAnonymous", defaultValue = "false") final boolean isAnonymous) {
        log.info("Fetching cart items. ID: {}, Anonymous: {}", id, isAnonymous);
        return service.getCartItems(id, isAnonymous);
    }

    /**
     * Add or update an item in a Redis cart.
     * Pass 'isAnonymous=true' if the user isn't logged in yet.
     */
    @PostMapping("addCartItem/{id}")
    public ResponseEntity<String> addCartItem(
            @PathVariable final UUID id,
            @RequestParam(value = "isAnonymous", defaultValue = "false") final boolean isAnonymous,
            @RequestBody final ProductDTO dto) {
        service.addCartItem(dto, id, isAnonymous);
        log.info("Cart item added to Redis. ID: {}, Anonymous: {}", id, isAnonymous);
        return ResponseEntity.ok("Cart item added successfully");
    }

    /**
     * Delete an item from a Redis cart in real time.
     */
    @DeleteMapping("deleteCartItem/{id}/{productId}")
    public ResponseEntity<String> deleteCartItem(
            @PathVariable final UUID id,
            @PathVariable final UUID productId,
            @RequestParam(value = "isAnonymous", defaultValue = "false") final boolean isAnonymous) {
        service.deleteCartItem(productId, id, isAnonymous);
        log.info("Cart item dropped instantly from Redis hash map.");
        return ResponseEntity.ok("Item removed successfully");
    }

    /**
     * NEW ENDPOINT: Call this right after a user successfully logs in or registers.
     * This migrates all items from their guest browser UUID into their real user profile.
     */
    @PostMapping("cart/merge/{guestId}/{userId}")
    public ResponseEntity<String> mergeCart(
            @PathVariable final UUID guestId,
            @PathVariable final UUID userId) {
        service.mergeGuestCartToUser(guestId, userId);
        log.info("Guest cart migrated to permanent profile account.");
        return ResponseEntity.ok("Carts successfully merged");
    }

    // ==========================================
    //          DATABASE CHECKOUT ENDPOINTS
    // ==========================================

    /**
     * Checkout for registered users.
     * Pulls verified items out of Redis automatically using the {userId}.
     */
    @PostMapping("createOrder/{userId}")
    public ResponseEntity<String> createOrder(
            @PathVariable final UUID userId,
            @RequestBody final OrderDTO orderDTO) {
        log.info("Processing checkout for user profile: {}", userId);
        service.createOrder(userId, orderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("Order processed successfully");
    }

    /**
     * Checkout for anonymous guests.
     * Pulls items from Redis guest space and saves them into your permanent 'AnonymousOrder' table.
     */
    @PostMapping("createAnonymousOrder/{guestId}")
    public ResponseEntity<String> createAnonymousOrder(
            @PathVariable final UUID guestId,
            @RequestBody final AnonymousOrderDTO dto) {
        log.info("Processing guest checkout session: {}", guestId);
        service.createAnonymousOrder(guestId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Anonymous order processed successfully");
    }
}
