package me.davidgomes.learningcqrspoc.service

import me.davidgomes.learningcqrspoc.dto.Order
import me.davidgomes.learningcqrspoc.dto.OrderStatus
import me.davidgomes.learningcqrspoc.entity.OrderEntity
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

    fun placeOrder(buyerCitizenID: UUID, itemName: String, quantity: Int): PlaceOrderResult {
        if (personService.getPerson(buyerCitizenID) == null) return PlaceOrderResult.BuyerNotFound

        val orderID = UUID.randomUUID()
        repository.save(OrderEntity(orderID, buyerCitizenID, itemName, quantity, OrderStatus.Placed))

        logger.info("Order was placed (by citizen = {})", buyerCitizenID)

        return PlaceOrderResult.Success(orderID)
    }

    fun getOrder(orderID: UUID): Order? {
        val entity = repository.findByOrderID(orderID) ?: return null

        return Order(entity.orderID, entity.buyerCitizenID, entity.itemName, entity.quantity, entity.status)
    }
}

sealed class PlaceOrderResult {
    data class Success(val orderID: UUID) : PlaceOrderResult()
    data object BuyerNotFound : PlaceOrderResult()
}
