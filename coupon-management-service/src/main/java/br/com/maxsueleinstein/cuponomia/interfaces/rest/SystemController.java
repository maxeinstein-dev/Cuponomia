package br.com.maxsueleinstein.cuponomia.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Tag(name = "System", description = "Service navigation and health helpers.")
public class SystemController {

    @GetMapping("/")
    @Operation(summary = "Redirect to Swagger UI", description = "Opens the management service Swagger UI from the service root URL.")
    public String swagger() {
        return "redirect:/swagger-ui.html";
    }
}
