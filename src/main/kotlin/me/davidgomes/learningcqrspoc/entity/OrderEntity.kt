package me.davidgomes.learningcqrspoc.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import me.davidgomes.learningcqrspoc.dto.OrderStatus
import java.util.UUID

@Entity
@Table(name = "orders")
class OrderEntity(
    val orderID: UUID,
    val buyerCitizenID: UUID,
    val itemName: String,
    val quantity: Int,
    @Enumerated(EnumType.STRING)
    val status: OrderStatus,
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Int? = null,
)
