package me.davidgomes.learningcqrspoc

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import me.davidgomes.learningcqrspoc.dto.Person
import me.davidgomes.learningcqrspoc.dto.request.NewPerson
import me.davidgomes.learningcqrspoc.dto.response.PersonCreated
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.Network
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration

private const val MAX_TIME_OUT = 5_000L

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
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

        await.atMost(Duration.ofMillis(MAX_TIME_OUT))
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

        await.atMost(Duration.ofMillis(MAX_TIME_OUT))
            .untilAsserted {
                mockMvc.perform(
                    get("/persons/${createdResponse.citizenID}")
                )
                    .andExpect(status().isOk)
            }

        Thread.sleep(2_000)

        await
            .atMost(Duration.ofMillis(MAX_TIME_OUT))
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

    companion object {
        private val network: Network = Network.newNetwork()

        @Container
        val kafka: KafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.2.1"))
            .withNetwork(network)
            .withNetworkAliases("kafka")

        @Container
        val schemaRegistry: GenericContainer<*> =
            GenericContainer(DockerImageName.parse("confluentinc/cp-schema-registry:7.4.6"))
                .withNetwork(network)
                .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
                .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081")
                .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "kafka:9092")
                .dependsOn(kafka)
                .withExposedPorts(8081)

        @DynamicPropertySource
        @JvmStatic
        fun kafkaProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers)
            registry.add("spring.kafka.producer.properties.schema.registry.url") {
                "http://${schemaRegistry.host}:${
                    schemaRegistry.getMappedPort(8081)
                }"
            }
            registry.add("spring.kafka.consumer.properties.schema.registry.url") {
                "http://${schemaRegistry.host}:${
                    schemaRegistry.getMappedPort(8081)
                }"
            }
        }
    }
}
