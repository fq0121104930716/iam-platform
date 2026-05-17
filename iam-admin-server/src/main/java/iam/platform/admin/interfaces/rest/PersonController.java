package iam.platform.admin.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import iam.platform.common.dto.request.CreatePersonRequest;
import iam.platform.common.dto.request.UpdatePersonRequest;
import iam.platform.common.dto.response.PersonResponse;
import iam.platform.admin.application.service.PersonApplicationService;
import iam.platform.common.api.ApiResponse;
import iam.platform.common.api.PageResponse;

@RestController
@RequestMapping("/v1/persons")
@RequiredArgsConstructor
@Tag(name = "Person", description = "Person (natural person) management API")
public class PersonController {

    private final PersonApplicationService personApplicationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new person")
    public ApiResponse<PersonResponse> create(@Valid @RequestBody CreatePersonRequest request) {
        return ApiResponse.created(personApplicationService.createPerson(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get person by ID")
    public ApiResponse<PersonResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(personApplicationService.getPerson(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update person")
    public ApiResponse<PersonResponse> update(@PathVariable Long id,
            @Valid @RequestBody UpdatePersonRequest request) {
        return ApiResponse.success(personApplicationService.updatePerson(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete person")
    public void delete(@PathVariable Long id) {
        personApplicationService.deletePerson(id);
    }

    @GetMapping
    @Operation(summary = "List persons")
    public ApiResponse<PageResponse<PersonResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(personApplicationService.listPersons(page, size));
    }
}
