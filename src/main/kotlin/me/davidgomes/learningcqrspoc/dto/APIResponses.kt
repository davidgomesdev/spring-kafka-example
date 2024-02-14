package me.davidgomes.learningcqrspoc.dto

import java.util.UUID

sealed class PlaceOrderResponse {
    data class OrderPlaced(val orderID: UUID) : PlaceOrderResponse()

    data class BadRequestDetails(val reason: String) : PlaceOrderResponse()
}

data class PersonCreated(val citizenID: UUID, val name: String)
