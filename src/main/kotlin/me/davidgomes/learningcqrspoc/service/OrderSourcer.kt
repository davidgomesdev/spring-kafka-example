package me.davidgomes.learningcqrspoc.service

import me.davidgomes.learningcqrspoc.dto.OrderStatus
import me.davidgomes.learningcqrspoc.entity.OrderEntity
import me.davidgomes.learningcqrspoc.entity.PersonEntity
import me.davidgomes.learningcqrspoc.event.OrderEvent
import me.davidgomes.learningcqrspoc.event.OrderEventEnvelope
import me.davidgomes.learningcqrspoc.event.OrderPlaced
import me.davidgomes.learningcqrspoc.event.OrderProcessed
import me.davidgomes.learningcqrspoc.event.PersonAged
import me.davidgomes.learningcqrspoc.event.PersonBorn
import me.davidgomes.learningcqrspoc.event.PersonEventEnvelope
import me.davidgomes.learningcqrspoc.repository.OrderRepository
import me.davidgomes.learningcqrspoc.repository.PersonRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.DltHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.kafka.retrytopic.DltStrategy
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Service

@Service
class OrderSourcer(
    private val repository: OrderRepository
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @RetryableTopic(attempts = "2", kafkaTemplate = "kafkaTemplate")
    @KafkaListener(topics = [ORDER_TOPIC])
    fun consume(envelope: OrderEventEnvelope) {
        when (val event = envelope.event) {
            is OrderPlaced -> {
                log.info("Creating new order ({})", event.orderID)
                repository.save(
                    OrderEntity(
                        event.orderID,
                        event.buyerCitizenID,
                        event.itemName,
                        event.quantity.toInt(),
                        OrderStatus.Placed
                    )
                )
            }

            is OrderProcessed -> {
                repository.updateOrderStatus(event.orderID, OrderStatus.Processed)

                log.info("Processed order ({})", event.orderID)
            }
        }
    }

    @DltHandler
    fun handleDeadletters(envelope: PersonEventEnvelope, @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String) {
        log.warn("Received event '${envelope.event}' of topic '$topic' (id = ${envelope.eventId})")
    }
}
