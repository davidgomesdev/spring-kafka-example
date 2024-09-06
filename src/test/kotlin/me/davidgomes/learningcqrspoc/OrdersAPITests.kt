package me.davidgomes.learningcqrspoc

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.davidgomes.learningcqrspoc.dto.NewPerson
import me.davidgomes.learningcqrspoc.dto.Person
import me.davidgomes.learningcqrspoc.dto.PersonCreated
import me.davidgomes.learningcqrspoc.dto.PlaceOrder
import me.davidgomes.learningcqrspoc.dto.PlaceOrderResponse
import me.davidgomes.learningcqrspoc.service.ORDER_TOPIC
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.Duration
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class OrdersAPITests {

    private val json = jacksonObjectMapper()

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `when an order is created, the get endpoint should return that order`() {
        val citizenID = createPerson()

        val createdResponse = json.readValue<PlaceOrderResponse.OrderPlaced>(
            mockMvc.perform(
                MockMvcRequestBuilders.post("/orders")
                    .content(json.writeValueAsBytes(PlaceOrder(citizenID, "Food", 1337)))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            )
                .andExpect(MockMvcResultMatchers.status().isAccepted)
                .andReturn().response.contentAsString
        )

         mockMvc.perform(
                    MockMvcRequestBuilders.get("/orders/${createdResponse.orderID}")
                )
                    .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `when an order is created for an non existing person, the get endpoint should return an error`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/orders")
                .content(json.writeValueAsBytes(PlaceOrder(UUID.randomUUID(), "Feed", 404)))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    fun createPerson(): UUID {
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

        return createdResponse.citizenID
    }
}
