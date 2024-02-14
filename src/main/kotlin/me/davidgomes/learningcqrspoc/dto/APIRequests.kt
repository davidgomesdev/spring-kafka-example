package me.davidgomes.learningcqrspoc.dto

import java.util.UUID

data class NewPerson(val name: String)

data class PlaceOrder(val buyerCitizenID: UUID, val itemName: String, val quantity: ULong)
