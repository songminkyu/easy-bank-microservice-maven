package io.github.songminkyu.account

import io.github.songminkyu.account.dto.AccountContactInfoDTO
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@EnableDiscoveryClient
@EnableConfigurationProperties(value = [AccountContactInfoDTO::class])
@SpringBootApplication
class AccountApplication
fun main(args: Array<String>) {
    runApplication<AccountApplication>(*args)
}
