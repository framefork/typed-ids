package org.framefork.typedIds.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

}
