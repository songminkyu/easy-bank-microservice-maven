package io.github.susimsek.card.consumer;

import static io.github.susimsek.card.debezium.data.Operation.DELETE;
import static io.github.susimsek.card.debezium.data.Operation.UPDATE;

import io.github.susimsek.card.debezium.data.DebeziumEventDetails;
import io.github.susimsek.card.dto.CardDTO;
import io.github.susimsek.card.entity.Card;
import io.github.susimsek.card.mapper.CardMapper;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.observables.ConnectableObservable;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class CardPublisher {

    @Getter
    private final Flowable<CardDTO> publisher;
    private ObservableEmitter<CardDTO> emitter;
    private final CardMapper cardMapper;

    @PersistenceUnit
    private EntityManagerFactory emf;

    public CardPublisher(CardMapper cardMapper) {
        this.cardMapper = cardMapper;
        Observable<CardDTO> ratingObservable = Observable.create(emitter -> this.emitter = emitter);
        ConnectableObservable<CardDTO> connectableObservable = ratingObservable.share().publish();
        connectableObservable.connect();
        this.publisher = connectableObservable.toFlowable(BackpressureStrategy.BUFFER);
    }

    @Bean
    public Consumer<DebeziumEventDetails<Card>> processCardDebeziumEvent() {
        return debeziumEvent -> {
            if (emitter != null) {
                var payload = debeziumEvent.payload();
                var entity = switch (payload.operation()) {
                    case DELETE -> payload.before();
                    default -> payload.after();
                };
                var dto = cardMapper.toDto(entity);
                emitter.onNext(dto);
                if (payload.operation() == DELETE || payload.operation() == UPDATE) {
                    log.info("Evicting item {} from 2nd-level cache as TX was not started by this application",
                        entity.getCardId());
                    emf.getCache().evict(entity.getClass(), entity.getCardId());
                }
            }
        };
    }
}