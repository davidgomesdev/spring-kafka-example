package me.davidgomes.learningcqrspoc.controller

import me.davidgomes.learningcqrspoc.dto.request.NewPerson
import me.davidgomes.learningcqrspoc.dto.Person
import me.davidgomes.learningcqrspoc.dto.response.PersonCreated
import me.davidgomes.learningcqrspoc.service.PersonService
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
@RequestMapping("persons")
class PersonController(private val service: PersonService) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun createPerson(@RequestBody body: NewPerson): PersonCreated {
        val name = body.name
        val uuid = service.createPerson(name, body.initialAge)

        return PersonCreated(uuid, name)
    }

    @GetMapping("/{citizenID}")
    fun getPerson(@PathVariable citizenID: UUID): ResponseEntity<Person> {
        log.info("Querying '{}'", citizenID)

        val person = service.getPerson(citizenID) ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(person)
    }
}
