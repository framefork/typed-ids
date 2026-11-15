package org.framefork.typedIds.web;

import org.framefork.typedIds.Application;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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

}
