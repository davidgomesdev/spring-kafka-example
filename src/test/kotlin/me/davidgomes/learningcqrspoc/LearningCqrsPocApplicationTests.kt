package me.davidgomes.learningcqrspoc

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import me.davidgomes.learningcqrspoc.dto.request.NewPerson
import me.davidgomes.learningcqrspoc.dto.response.PersonCreated
import org.apache.kafka.clients.producer.ProducerConfig
import org.awaitility.kotlin.await
import org.junit.jupiter.api.AfterAll
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

    companion object {
        private val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.2.1"))
            .withReuse(true)
        private val json = JsonMapper().registerModule(
            KotlinModule.Builder()
                .withReflectionCacheSize(512)
                .configure(KotlinFeature.NullToEmptyCollection, false)
                .configure(KotlinFeature.NullToEmptyMap, false)
                .configure(KotlinFeature.NullIsSameAsDefault, false)
                .configure(KotlinFeature.SingletonSupport, false)
                .configure(KotlinFeature.StrictNullChecks, false)
                .build()
        )

        @JvmStatic
        @BeforeAll
        fun setup() {
            kafka.start()
        }

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka::getBootstrapServers)
        }
    }

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

        await.atMost(Duration.ofMillis(5500))
            .untilAsserted {
                mockMvc.perform(
                    get("/persons/${createdResponse.citizenID}")
                )
                    .andExpect(status().isOk)
            }
    }
}
