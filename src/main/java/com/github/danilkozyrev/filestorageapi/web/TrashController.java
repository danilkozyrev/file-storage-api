package com.github.danilkozyrev.filestorageapi.web;

import com.github.danilkozyrev.filestorageapi.dto.projection.Metadata;
import com.github.danilkozyrev.filestorageapi.security.UserPrincipal;
import com.github.danilkozyrev.filestorageapi.service.TrashService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Api(tags = "Trash")
@ApiResponses({
        @ApiResponse(code = 401, message = "Unauthorized"),
        @ApiResponse(code = 500, message = "Internal server error")
})
@RequiredArgsConstructor
public class TrashController {

    private final TrashService trashService;

    @GetMapping(path = "/trash/items", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get items in trash", notes = "Move file or folder to restore it from the trash.")
    public List<Metadata> getTrashItems(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return trashService.getTrashItems(userPrincipal.getId());
    }

    @DeleteMapping("/trash/items")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation("Empty trash")
    public void emptyTrash(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        trashService.emptyTrash(userPrincipal.getId());
    }

}
