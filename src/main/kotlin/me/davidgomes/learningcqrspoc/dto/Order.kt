package me.davidgomes.learningcqrspoc.dto

import java.util.UUID

data class Order(val id: UUID, val buyerCitizenID: UUID, val itemName: String, val quantity: Int, val status: OrderStatus)

enum class OrderStatus {
    Placed, Processed
}
