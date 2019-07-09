package com.github.danilkozyrev.filestorageapi.web;

import com.github.danilkozyrev.filestorageapi.dto.form.SearchForm;
import com.github.danilkozyrev.filestorageapi.dto.projection.Metadata;
import com.github.danilkozyrev.filestorageapi.security.UserPrincipal;
import com.github.danilkozyrev.filestorageapi.service.SearchService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "Search")
@ApiResponses({
        @ApiResponse(code = 400, message = "Invalid input"),
        @ApiResponse(code = 401, message = "Unauthorized"),
        @ApiResponse(code = 500, message = "Internal server error")
})
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/search")
    @ApiOperation(
            value = "Search for items using findItems parameters",
            notes = "Returns only files if any mime type is specified.")
    public List<Metadata> search(
            @Valid SearchForm searchForm,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return searchService.findItems(searchForm, userPrincipal.getId());
    }

    @GetMapping("/recent")
    @ApiOperation("Find recent items")
    public List<Metadata> findRecentItems(
            @RequestParam(name = "after", required = false) Instant afterDate,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Instant defaultDate = afterDate == null ? Instant.now().minus(7, ChronoUnit.DAYS) : afterDate;
        return searchService.findRecentItems(defaultDate, userPrincipal.getId());
    }

}
