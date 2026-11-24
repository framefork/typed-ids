package org.framefork.typedIds.web;

import org.jspecify.annotations.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TypedIdsWebBindingController
{

    @GetMapping("/orders/{id}")
    public String order(@PathVariable("id") final WebIds.OrderId id)
    {
        return id.toNativeUuid().toString();
    }

    @GetMapping("/items")
    public String item(@RequestParam("id") final WebIds.ItemId id)
    {
        return String.valueOf(id.toLong());
    }

    @GetMapping("/items/optional")
    public String optionalItem(@RequestParam(value = "id", required = false) final WebIds.@Nullable ItemId id)
    {
        return describe(id);
    }

    @GetMapping("/items/list")
    public String itemList(@RequestParam("ids") final List<WebIds.@Nullable ItemId> ids)
    {
        return ids.stream().map(TypedIdsWebBindingController::describe).toList().toString();
    }

    private static String describe(final WebIds.@Nullable ItemId id)
    {
        return (id == null) ? "null" : String.valueOf(id.toLong());
    }

}
