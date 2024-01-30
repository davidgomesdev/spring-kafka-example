package me.davidgomes.learningcqrspoc

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.streams.StreamsConfig
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName

@SpringBootTest
class LearningCqrsPocApplicationTests {

	companion object {
		private val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.2.1"))

		@JvmStatic
		@BeforeAll
		fun setup() {
			kafka.start()
		}

		@JvmStatic
		@AfterAll
		fun teardown(): Unit {
			kafka.stop()
		}

		@DynamicPropertySource
		@JvmStatic
		fun configureProperties(registry: DynamicPropertyRegistry) {
			registry.add(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka::getBootstrapServers)
			registry.add(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka::getBootstrapServers)
			registry.add(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafka::getBootstrapServers)
		}
	}

	@Test
	fun contextLoads() {
	}
}
