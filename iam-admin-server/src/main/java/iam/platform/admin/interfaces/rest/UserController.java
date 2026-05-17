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
import iam.platform.common.dto.request.CreateUserRequest;
import iam.platform.common.dto.request.UpdateUserRequest;
import iam.platform.common.dto.response.UserResponse;
import iam.platform.admin.application.service.UserApplicationService;
import iam.platform.common.api.ApiResponse;
import iam.platform.common.api.PageResponse;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User management API")
public class UserController {

    private final UserApplicationService userApplicationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new user")
    public ApiResponse<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.created(userApplicationService.createUser(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ApiResponse<UserResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(userApplicationService.getUser(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user")
    public ApiResponse<UserResponse> update(@PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ApiResponse.success(userApplicationService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete user")
    public void delete(@PathVariable Long id) {
        userApplicationService.deleteUser(id);
    }

    @GetMapping
    @Operation(summary = "List users")
    public ApiResponse<PageResponse<UserResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(userApplicationService.listUsers(page, size));
    }
}
