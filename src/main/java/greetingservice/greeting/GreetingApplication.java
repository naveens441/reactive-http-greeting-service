package greetingservice.greeting;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication
public class GreetingApplication {

    public static void main(String[] args) {
        SpringApplication.run(GreetingApplication.class, args);
    }

    @Bean
    RouterFunction<ServerResponse> routes(GreetingService greetingService) {
        return route()
                .GET("/greeting/{name}",
                        serverRequest -> ok()
                                .body(greetingService.greetOnce(new GreetingRequest(serverRequest.pathVariable("name"))), GreetingResponse.class))
                .GET("/greetings/{name}",
                        serverRequest -> ok()
                                .contentType(MediaType.TEXT_EVENT_STREAM)
                                .body(greetingService.greetMany(new GreetingRequest(serverRequest.pathVariable("name"))), GreetingResponse.class))
                .build();

    }
}

@Service
class GreetingService {
    Mono<GreetingResponse> greetOnce(GreetingRequest greetingRequest) {
        return Mono.just(greet(greetingRequest.getName()));
    }

    Flux<GreetingResponse> greetMany(GreetingRequest greetingRequest) {
        return Flux.fromStream(Stream.generate(() -> greet(greetingRequest.getName()))).delayElements(Duration.ofSeconds(1));
    }

    private GreetingResponse greet(String name) {
        return new GreetingResponse("Hello" + name + "@" + Instant.now());
    }
}


@Data
@NoArgsConstructor
@AllArgsConstructor
class GreetingRequest {
    private String name;

}

@Data
@NoArgsConstructor
@AllArgsConstructor
class GreetingResponse {
    private String message;
}
