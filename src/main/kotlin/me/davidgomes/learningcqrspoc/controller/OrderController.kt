package me.davidgomes.learningcqrspoc.controller

import me.davidgomes.learningcqrspoc.dto.Order
import me.davidgomes.learningcqrspoc.dto.PlaceOrder
import me.davidgomes.learningcqrspoc.dto.PlaceOrderResponse
import me.davidgomes.learningcqrspoc.service.OrderService
import me.davidgomes.learningcqrspoc.service.PlaceOrderResult
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("orders")
class OrderController(private val service: OrderService) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @PostMapping
    fun createPerson(@RequestBody body: PlaceOrder): ResponseEntity<PlaceOrderResponse> {
        return when (val result = service.placeOrder(body.buyerCitizenID, body.itemName, body.quantity)) {
            PlaceOrderResult.BuyerNotFound -> ResponseEntity.badRequest()
                .body(PlaceOrderResponse.BadRequestDetails("The buyer needs to be registered first."))

            is PlaceOrderResult.Success -> ResponseEntity.accepted().body(PlaceOrderResponse.OrderPlaced(result.orderID))
        }
    }

    @GetMapping("/{orderID}")
    fun getOrder(@PathVariable orderID: UUID): ResponseEntity<Order> {
        log.info("Querying order '{}'", orderID)

        val order = service.getOrder(orderID) ?: return ResponseEntity.notFound().build()


        return ResponseEntity.ok(order)
    }
}
