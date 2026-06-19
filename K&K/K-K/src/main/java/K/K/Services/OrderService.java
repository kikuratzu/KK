package K.K.Services;

import K.K.Entities.*;
import K.K.K.DTOs.AnonymousOrderDTO;
import K.K.K.DTOs.OrderDTO;
import K.K.K.DTOs.ProductDTO;
import K.K.Repositories.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final AnonymousOrderRepository anonymousOrderRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String USER_CART_PREFIX = "cart:user:";
    private static final String GUEST_CART_PREFIX = "cart:guest:";

    private static final long USER_CART_TTL_DAYS = 7;
    private static final long GUEST_CART_TTL_DAYS = 2;
    private final ObjectMapper objectMapper;

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        AnonymousOrderRepository anonymousOrderRepository,
                        UserRepository userRepository,
                        RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.anonymousOrderRepository = anonymousOrderRepository;
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    private String resolveCartKey(UUID identifier, boolean isAnonymous) {
        return isAnonymous ? GUEST_CART_PREFIX + identifier.toString() : USER_CART_PREFIX + identifier.toString();
    }

    public List<ProductDTO> getCartItems(final UUID identifier, boolean isAnonymous) {
        String key = resolveCartKey(identifier, isAnonymous);
        Map<Object, Object> rawCart = redisTemplate.opsForHash().entries(key);

        return rawCart.values().stream()
                .map(obj -> objectMapper.convertValue(obj, ProductDTO.class))
                .collect(Collectors.toList());
    }

    public void addCartItem(final ProductDTO productDTO, final UUID identifier, boolean isAnonymous) {
        String key = resolveCartKey(identifier, isAnonymous);
        String productIdStr = productDTO.getProductId().toString();

        Product existingProduct = productRepository.findById(productDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productDTO.getProductId()));

        Object existingItemObj = redisTemplate.opsForHash().get(key, productIdStr);

        if (existingItemObj != null) {
            ProductDTO existingItem = (ProductDTO) existingItemObj;
            existingItem.setQuantity(existingItem.getQuantity() + productDTO.getQuantity());
            existingItem.setPrice(existingProduct.getPrice());
            existingItem.setSize(productDTO.getSize());
            redisTemplate.opsForHash().put(key, productIdStr, existingItem);
        } else {
            productDTO.setName(existingProduct.getName());
            productDTO.setPrice(existingProduct.getPrice());
            redisTemplate.opsForHash().put(key, productIdStr, productDTO);
        }

        long ttl = isAnonymous ? GUEST_CART_TTL_DAYS : USER_CART_TTL_DAYS;
        redisTemplate.expire(key, ttl, TimeUnit.DAYS);
    }

    public void deleteCartItem(final UUID productId, final UUID identifier, boolean isAnonymous) {
        String key = resolveCartKey(identifier, isAnonymous);
        redisTemplate.opsForHash().delete(key, productId.toString());
    }

    public void clearCart(final UUID identifier, boolean isAnonymous) {
        String key = resolveCartKey(identifier, isAnonymous);
        redisTemplate.delete(key);
    }

    public void mergeGuestCartToUser(final UUID guestId, final UUID userId) {
        List<ProductDTO> guestItems = getCartItems(guestId, true);

        for (ProductDTO guestItem : guestItems) {
            addCartItem(guestItem, userId, false);
        }

        clearCart(guestId, true);
        log.info("Guest cart {} merged into User account {}", guestId, userId);
    }

    @Transactional
    public void createAnonymousOrder(final UUID guestId, final AnonymousOrderDTO anonymousOrderDTO) {
        List<ProductDTO> guestCartItems = getCartItems(guestId, true);
        if (guestCartItems.isEmpty()) {
            throw new IllegalStateException("Cannot checkout an empty guest shopping cart.");
        }

        AnonymousOrder anonymousOrder = new AnonymousOrder();
        BeanUtils.copyProperties(anonymousOrderDTO, anonymousOrder);
        anonymousOrder = anonymousOrderRepository.save(anonymousOrder);

        for (ProductDTO dto : guestCartItems) {
            Product existingProduct = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + dto.getProductId()));

            OrderProduct orderItem = new OrderProduct();
            orderItem.setAnonymousOrder(anonymousOrder);
            orderItem.setProduct(existingProduct);
            orderItem.setSize(dto.getSize());
            orderItem.setQuantity(dto.getQuantity());

            anonymousOrder.getOrderProducts().add(orderItem);
        }

        log.info("Anonymous database order created successfully from Redis guest cart!");

        clearCart(guestId, true);
    }

    @Transactional
    public Order createOrder(final UUID userId, final OrderDTO orderDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        List<ProductDTO> cartItems = getCartItems(userId, false);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Cannot checkout an empty user shopping cart.");
        }

        Order order = new Order();
        BeanUtils.copyProperties(orderDTO, order);
        order.setUser(user);
        order = orderRepository.save(order);
        user.getOrders().add(order);

        for (ProductDTO dto : cartItems) {
            Product existingProduct = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + dto.getProductId()));

            OrderProduct orderItem = new OrderProduct();
            orderItem.setOrder(order);
            orderItem.setProduct(existingProduct);
            orderItem.setSize(dto.getSize());
            orderItem.setQuantity(dto.getQuantity());

            order.getOrderProducts().add(orderItem);
        }

        log.info("User order created successfully from Redis cart!");
        clearCart(userId, false);
        return order;
    }
}
