package me.davidgomes.learningcqrspoc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication

@SpringBootApplication
@EntityScan(basePackages = ["me.davidgomes.learningcqrspoc.entity"])
class LearningCqrsPocApplication

fun main(args: Array<String>) {
	runApplication<LearningCqrsPocApplication>(*args)
}
