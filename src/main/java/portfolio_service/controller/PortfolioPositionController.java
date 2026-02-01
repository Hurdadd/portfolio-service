package portfolio_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import portfolio_service.dto.CreatePositionRequest;
import portfolio_service.dto.PositionResponse;
import portfolio_service.dto.UpdatePositionRequest;
import portfolio_service.service.PortfolioPositionService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PortfolioPositionController {

    private final PortfolioPositionService service;

    @PostMapping("/positions")
    @ResponseStatus(HttpStatus.CREATED)
    public PositionResponse create(@Valid @RequestBody CreatePositionRequest req) {
        return service.create(req);
    }

    @GetMapping("/positions/{id}")
    public PositionResponse getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @GetMapping("/clients/{clientId}/positions")
    public List<PositionResponse> listByClient(@PathVariable String clientId) {
        return service.listByClient(clientId);
    }

    @PutMapping("/positions/{id}")
    public PositionResponse update(@PathVariable UUID id, @Valid @RequestBody UpdatePositionRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/positions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
