package io.github.songminkyu.card.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Cache(region = "cardCache", usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Card extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SequenceGenerator")
    @SequenceGenerator(name = "SequenceGenerator", sequenceName = "seq_card", allocationSize = 1)
    @Column(name = "card_id", nullable = false)
    private Long cardId;


    @Column(name = "mobile_number", nullable = false, length = 15)
    private String mobileNumber;

    @Column(name = "card_number", nullable = false, length = 100)
    private String cardNumber;

    @Column(name = "card_type", nullable = false, length = 100)
    private String cardType;

    @Column(name = "total_limit", nullable = false)
    private int totalLimit;

    @Column(name = "amount_used", nullable = false)
    private int amountUsed;

    @Column(name = "available_amount", nullable = false)
    private int availableAmount;

}