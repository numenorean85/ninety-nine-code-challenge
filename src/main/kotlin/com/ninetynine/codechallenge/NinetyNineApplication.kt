package com.ninetynine.codechallenge

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableJpaRepositories
@ConfigurationPropertiesScan("com.ninetynine.codechallenge.config.properties")
@EnableScheduling
class NinetyNineApplication

fun main(args: Array<String>) {
	runApplication<NinetyNineApplication>(*args)
}
