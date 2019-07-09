package com.github.danilkozyrev.filestorageapi.web;

import com.github.danilkozyrev.filestorageapi.dto.form.UserForm;
import com.github.danilkozyrev.filestorageapi.dto.projection.UserInfo;
import com.github.danilkozyrev.filestorageapi.security.UserPrincipal;
import com.github.danilkozyrev.filestorageapi.service.UserService;
import com.github.danilkozyrev.filestorageapi.validation.ValidationGroups;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@Api(tags = "Users")
@ApiResponses({
        @ApiResponse(code = 401, message = "Unauthorized"),
        @ApiResponse(code = 500, message = "Internal server error")
})
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Create user", notes = "Available only for managers.")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Created", responseHeaders = @ResponseHeader(
                    name = HttpHeaders.LOCATION,
                    description = "A location uri of the created user",
                    response = String.class)),
            @ApiResponse(code = 400, message = "Invalid input"),
            @ApiResponse(code = 409, message = "An account with the specified email already exists")
    })
    public ResponseEntity<UserInfo> createUser(
            @RequestBody @Validated(ValidationGroups.Create.class) UserForm userForm,
            UriComponentsBuilder uriComponentsBuilder) {
        UserInfo userInfo = userService.createUser(userForm);
        URI locationUri = uriComponentsBuilder.path("/api/users/{userId}").buildAndExpand(userInfo.getId()).toUri();
        return ResponseEntity.created(locationUri).body(userInfo);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get information about all users", notes = "Available only for managers.")
    public List<UserInfo> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping(path = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get information about user")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid input"),
            @ApiResponse(code = 404, message = "The specified user doesn't exist or access to it is forbidden"),
    })
    public UserInfo getUserInfo(@PathVariable("userId") Long userId) {
        return userService.getUserInfo(userId);
    }

    @GetMapping(path = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get information about current user")
    public UserInfo getCurrentUserInfo(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return userService.getUserInfo(userPrincipal.getId());
    }

    @PatchMapping(
            path = "/{userId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Update information about user", notes = "Available only for managers.")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid input"),
            @ApiResponse(code = 404, message = "The specified user doesn't exist or access to it is forbidden"),
            @ApiResponse(code = 409, message = "An account with the specified email already exists")
    })
    public UserInfo updateUserInfo(
            @PathVariable("userId") Long userId,
            @RequestBody @Validated(ValidationGroups.Update.class) UserForm userForm) {
        return userService.updateUserInfo(userId, userForm);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "Delete user", notes = "Available only for managers.")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid input"),
            @ApiResponse(code = 404, message = "The specified user doesn't exist or access to it is forbidden")
    })
    public void deleteUser(@PathVariable("userId") Long userId) {
        userService.deleteUser(userId);
    }

}
