package me.davidgomes.learningcqrspoc.event

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.UUID

data class OrderEventEnvelope(val event: OrderEvent, val eventId: UUID = UUID.randomUUID())

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(OrderPlaced::class, name = "placed"),
    JsonSubTypes.Type(OrderProcessed::class, name = "processed")
)
interface OrderEvent {
    val orderID: UUID
    val buyerCitizenID: UUID
}

data class OrderPlaced(
    override val buyerCitizenID: UUID,
    val itemName: String,
    val quantity: Int,
    override val orderID: UUID = UUID.randomUUID(),
) : OrderEvent

data class OrderProcessed(
    override val orderID: UUID,
    override val buyerCitizenID: UUID,
    val itemName: String,
    val quantity: Int
) : OrderEvent
