package me.davidgomes.learningcqrspoc

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.davidgomes.learningcqrspoc.dto.Person
import me.davidgomes.learningcqrspoc.dto.request.NewPerson
import me.davidgomes.learningcqrspoc.dto.response.PersonCreated
import org.apache.kafka.clients.producer.ProducerConfig
import org.awaitility.kotlin.await
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class LearningCqrsPocApplicationTests {

    private val json = jacksonObjectMapper()

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun contextLoads() {
    }

    @Test
    fun `when a person is created, the get endpoint should return that person`() {
        val createdResponse = json.readValue<PersonCreated>(
            mockMvc.perform(
                post("/persons")
                    .content(json.writeValueAsBytes(NewPerson("jammy")))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            )
                .andExpect(status().isAccepted)
                .andReturn().response.contentAsString
        )

        await.atMost(Duration.ofMillis(1000))
            .untilAsserted {
                mockMvc.perform(
                    get("/persons/${createdResponse.citizenID}")
                )
                    .andExpect(status().isOk)
            }
    }

    @Test
    fun `should age person, after roughly 2 seconds`() {
        val createdResponse = json.readValue<PersonCreated>(
            mockMvc.perform(
                post("/persons")
                    .content(json.writeValueAsBytes(NewPerson("jammy")))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            )
                .andExpect(status().isAccepted)
                .andReturn().response.contentAsString
        )

        await.atMost(Duration.ofMillis(1000))
            .untilAsserted {
                mockMvc.perform(
                    get("/persons/${createdResponse.citizenID}")
                )
                    .andExpect(status().isOk)
            }

        Thread.sleep(2_000)

        await
            .atMost(Duration.ofMillis(1000))
            .untilAsserted {
                val person = json.readValue<Person>(
                    mockMvc.perform(
                        get("/persons/${createdResponse.citizenID}")
                    )
                        .andExpect(status().isOk)
                        .andReturn().response.contentAsString
                )

                assertNotEquals(0, person.age)
            }
    }
}
