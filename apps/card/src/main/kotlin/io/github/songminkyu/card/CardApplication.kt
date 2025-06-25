package io.github.songminkyu.card

import io.github.songminkyu.card.dto.CardContactInfoDTO
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@EnableDiscoveryClient
@EnableConfigurationProperties(value = [CardContactInfoDTO::class])
@SpringBootApplication
class CardApplication
fun main(args: Array<String>) {
    runApplication<CardApplication>(*args)
}
