package K.K.Clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "anonymous-order-service", url = "http://localhost:8090/api")
public interface AnonymousOrderClient {
    @GetMapping("bye")
    public String bye();
}
