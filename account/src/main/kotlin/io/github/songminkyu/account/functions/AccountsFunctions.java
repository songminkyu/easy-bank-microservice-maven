package io.github.songminkyu.account.functions;

import io.github.songminkyu.account.debezium.data.DebeziumEventDetails;
import io.github.songminkyu.account.entity.Account;
import io.github.songminkyu.account.service.AccountService;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class AccountsFunctions {

    @Bean
    public Consumer<Long> updateCommunication(AccountService accountsService) {
        return accountNumber -> {
            log.info("Updating Communication status for the account number : " + accountNumber.toString());
            accountsService.updateCommunicationStatus(accountNumber);
        };
    }

    @Bean
    public Consumer<DebeziumEventDetails<Account>> processAccountDebeziumEvent() {
        return debeziumEvent -> {
            var payload = debeziumEvent.payload();
            switch (payload.operation()) {
                case CREATE ->  log.info("created account on debezium event: " + payload.after());
                case UPDATE ->  log.info("updated account on debezium event: " + payload.after());
                case DELETE ->  log.info("deleted account on debezium event: " + payload.before());
            }
        };
    }

}