package org.framefork.typedIds;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
    classes = {
        Application.class,
    },
    webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
class DummyControllerTest
{

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void listIds() throws Exception
    {
        mockMvc.perform(
                get("/api/ints")
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.items[0].title").value("one"))
            .andExpect(jsonPath("$.items[0].id").isNumber())
            .andExpect(jsonPath("$.items[1].title").value("two"))
            .andExpect(jsonPath("$.items[1].id").isNumber());
    }

    @Test
    public void listUuids() throws Exception
    {
        mockMvc.perform(
                get("/api/uuids")
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.items[0].title").value("one"))
            .andExpect(jsonPath("$.items[0].id").isString())
            .andExpect(jsonPath("$.items[1].title").value("two"))
            .andExpect(jsonPath("$.items[1].id").isString());
    }

}
