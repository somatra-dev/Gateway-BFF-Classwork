package com.pesexpo.orderservice.config;

import com.pesexpo.orderservice.client.ProductClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class RestClientConfig {

    @Bean
    public ProductClient productClient(LoadBalancerClient loadBalancerClient) {
        RestClient restClient = RestClient.builder()
                .baseUrl("http://product-service")
                .requestInterceptor(new LoadBalancerInterceptor(loadBalancerClient))
                .requestInterceptor((request, body, execution) -> {
                    // Propagate JWT token for service-to-service calls
                    var authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                        String token = jwtAuth.getToken().getTokenValue();
                        request.getHeaders().setBearerAuth(token);
                    }
                    return execution.execute(request, body);
                })
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();

        return factory.createClient(ProductClient.class);
    }

}
