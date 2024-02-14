package me.davidgomes.learningcqrspoc.service

import me.davidgomes.learningcqrspoc.dto.Order
import me.davidgomes.learningcqrspoc.event.OrderEvent
import me.davidgomes.learningcqrspoc.event.OrderEventEnvelope
import me.davidgomes.learningcqrspoc.event.OrderPlaced
import me.davidgomes.learningcqrspoc.repository.OrderRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.util.UUID

const val ORDER_TOPIC = "order-event"

@Service
class OrderService(
    private val template: KafkaTemplate<ByteArray, Any>,
    private val personService: PersonService,
    private val repository: OrderRepository
) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun placeOrder(buyerCitizenID: UUID, itemName: String, quantity: ULong): PlaceOrderResult {
        if (personService.getPerson(buyerCitizenID) == null) return PlaceOrderResult.BuyerNotFound

        val event = OrderPlaced(buyerCitizenID, itemName, quantity)

        val eventID = produceEvent(event)

        logger.info("Order was placed (event id = {})", eventID)

        return PlaceOrderResult.Success(event.orderID)
    }

    fun getOrder(orderID: UUID): Order? {
        val entity = repository.findByOrderID(orderID) ?: return null

        return Order(entity.orderID, entity.buyerCitizenID, entity.itemName, entity.quantity.toULong(), entity.status)
    }

    private fun produceEvent(event: OrderEvent): UUID {
        val eventEnvelope = OrderEventEnvelope(event)

        template.send(PERSON_TOPIC, event.buyerCitizenID.toString().encodeToByteArray(), eventEnvelope).get()

        return eventEnvelope.eventId
    }
}

sealed class PlaceOrderResult {
    data class Success(val orderID: UUID) : PlaceOrderResult()
    data object BuyerNotFound : PlaceOrderResult()
}
