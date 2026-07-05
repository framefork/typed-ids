package org.framefork.typedIds;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.framefork.typedIds.bigint.BigIntAppGeneratedExplicitMappingEntity;
import org.framefork.typedIds.uuid.UuidAppGeneratedExplicitMappingEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "ids", description = "Operations related to IDs")
public class DummyController
{

    @Operation(summary = "List sample data with BigInt IDs", description = "List sample data with BigInt IDs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = IntsResponse.class))
        })
    })
    @GetMapping("/api/ints")
    public ResponseEntity<IntsResponse> listBigInt()
    {
        return ResponseEntity.ok(new IntsResponse(List.of(
            new BigIntAppGeneratedExplicitMappingEntity("one"),
            new BigIntAppGeneratedExplicitMappingEntity("two")
        )));
    }

    @Operation(summary = "List sample data with UUID IDs", description = "List sample data with UUID IDs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = UuidsResponse.class))
        })
    })
    @GetMapping("/api/uuids")
    public ResponseEntity<UuidsResponse> listUuids()
    {
        return ResponseEntity.ok(new UuidsResponse(List.of(
            new UuidAppGeneratedExplicitMappingEntity("one"),
            new UuidAppGeneratedExplicitMappingEntity("two")
        )));
    }

    public record UuidsResponse(List<UuidAppGeneratedExplicitMappingEntity> items)
    {

    }

    public record IntsResponse(List<BigIntAppGeneratedExplicitMappingEntity> items)
    {

    }

}
