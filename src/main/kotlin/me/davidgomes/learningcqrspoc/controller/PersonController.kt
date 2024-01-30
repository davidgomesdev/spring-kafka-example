package me.davidgomes.learningcqrspoc.controller

import me.davidgomes.learningcqrspoc.dto.NewPerson
import me.davidgomes.learningcqrspoc.repository.PersonRepository
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class PersonController {

    fun createPerson(@RequestBody body: NewPerson) {

    }
}
