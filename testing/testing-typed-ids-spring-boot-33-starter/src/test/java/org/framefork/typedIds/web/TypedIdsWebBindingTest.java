package org.framefork.typedIds.web;

import org.framefork.typedIds.Application;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
    classes = {
        Application.class,
    },
    webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
class TypedIdsWebBindingTest
{

    private static final String ORDER_ID = "01936d8a-7b5e-7c3f-9a1b-2c3d4e5f6a7b";

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void bindsPathVariableToUuidId() throws Exception
    {
        mockMvc.perform(get("/orders/" + ORDER_ID))
            .andExpect(status().isOk())
            .andExpect(content().string(ORDER_ID));
    }

    @Test
    public void bindsRequestParameterToBigIntId() throws Exception
    {
        mockMvc.perform(get("/items").param("id", "42"))
            .andExpect(status().isOk())
            .andExpect(content().string("42"));
    }

    @Test
    public void rejectsMalformedIdsAsBadRequest() throws Exception
    {
        mockMvc.perform(get("/orders/not-a-uuid"))
            .andExpect(status().isBadRequest());
        mockMvc.perform(get("/items").param("id", "abc"))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void bindsEmptyOptionalParameterToNull() throws Exception
    {
        mockMvc.perform(get("/items/optional").param("id", ""))
            .andExpect(status().isOk())
            .andExpect(content().string("null"));
    }

    @Test
    public void bindsWhitespaceOnlyOptionalParameterToNull() throws Exception
    {
        mockMvc.perform(get("/items/optional").param("id", "  "))
            .andExpect(status().isOk())
            .andExpect(content().string("null"));
    }

    @Test
    public void rejectsEmptyRequiredParameterAsBadRequest() throws Exception
    {
        mockMvc.perform(get("/items").param("id", ""))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void rejectsBlankPathVariableAsBadRequest() throws Exception
    {
        mockMvc.perform(get("/orders/{id}", " "))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void trimsPaddedBigIntId() throws Exception
    {
        mockMvc.perform(get("/items").param("id", " 42"))
            .andExpect(status().isOk())
            .andExpect(content().string("42"));
    }

    @Test
    public void trimsPaddedUuidId() throws Exception
    {
        mockMvc.perform(get("/orders/{id}", " " + ORDER_ID + " "))
            .andExpect(status().isOk())
            .andExpect(content().string(ORDER_ID));
    }

    @Test
    public void bindsEmptyListElementToNull() throws Exception
    {
        mockMvc.perform(get("/items/list").param("ids", "1,,2"))
            .andExpect(status().isOk())
            .andExpect(content().string("[1, null, 2]"));
    }

    @Test
    public void trimsPaddedListElements() throws Exception
    {
        mockMvc.perform(get("/items/list").param("ids", "1, 2"))
            .andExpect(status().isOk())
            .andExpect(content().string("[1, 2]"));
    }

    @Test
    public void rejectsShortenedUuidAsBadRequest() throws Exception
    {
        // UUID.fromString accepts this shortened form as a version-4 UUID; the converter accepts a UUID only in the 36-character form
        mockMvc.perform(get("/orders/1-1-4001-1-1"))
            .andExpect(status().isBadRequest());
    }

}
