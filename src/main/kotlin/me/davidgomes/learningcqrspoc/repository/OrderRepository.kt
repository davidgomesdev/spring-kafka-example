package me.davidgomes.learningcqrspoc.repository

import me.davidgomes.learningcqrspoc.dto.OrderStatus
import me.davidgomes.learningcqrspoc.entity.OrderEntity
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

interface OrderRepository : CrudRepository<OrderEntity, ULong> {
    fun findByOrderID(orderID: UUID): OrderEntity?

    @Transactional
    @Modifying
    @Query("update OrderEntity o set o.status = ?2 where o.orderID = ?1")
    fun updateOrderStatus(orderID: UUID, newOrderStatus: OrderStatus)
}
