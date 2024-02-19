package me.davidgomes.learningcqrspoc

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.davidgomes.learningcqrspoc.dto.NewPerson
import me.davidgomes.learningcqrspoc.dto.Person
import me.davidgomes.learningcqrspoc.dto.PersonCreated
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.Duration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class PersonAPITests {

    private val json = jacksonObjectMapper()

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `when a person is created, the get endpoint should return that person`() {
        val createdResponse = json.readValue<PersonCreated>(
            mockMvc.perform(
                MockMvcRequestBuilders.post("/persons")
                    .content(json.writeValueAsBytes(NewPerson("jammy")))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            )
                .andExpect(MockMvcResultMatchers.status().isAccepted)
                .andReturn().response.contentAsString
        )

        await.atMost(Duration.ofMillis(1000))
            .untilAsserted {
                mockMvc.perform(
                    MockMvcRequestBuilders.get("/persons/${createdResponse.citizenID}")
                )
                    .andExpect(MockMvcResultMatchers.status().isOk)
            }
    }

    @Test
    fun `should age person, after roughly 2 seconds`() {
        val createdResponse = json.readValue<PersonCreated>(
            mockMvc.perform(
                MockMvcRequestBuilders.post("/persons")
                    .content(json.writeValueAsBytes(NewPerson("jammy")))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            )
                .andExpect(MockMvcResultMatchers.status().isAccepted)
                .andReturn().response.contentAsString
        )

        await.atMost(Duration.ofMillis(1000))
            .untilAsserted {
                mockMvc.perform(
                    MockMvcRequestBuilders.get("/persons/${createdResponse.citizenID}")
                )
                    .andExpect(MockMvcResultMatchers.status().isOk)
            }

        Thread.sleep(2_000)

        await
            .atMost(Duration.ofMillis(1000))
            .untilAsserted {
                val person = json.readValue<Person>(
                    mockMvc.perform(
                        MockMvcRequestBuilders.get("/persons/${createdResponse.citizenID}")
                    )
                        .andExpect(MockMvcResultMatchers.status().isOk)
                        .andReturn().response.contentAsString
                )

                Assertions.assertNotEquals(0, person.age)
            }
    }
}
