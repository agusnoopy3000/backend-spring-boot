package cl.huertohogar.huertohogar_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health", description = "Health check endpoints")
public class HelloController {

    @GetMapping("/hello")
    @Operation(summary = "Health check endpoint")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("OK");
    }
}
