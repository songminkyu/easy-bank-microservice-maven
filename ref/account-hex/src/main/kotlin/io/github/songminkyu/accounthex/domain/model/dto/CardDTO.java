package io.github.songminkyu.accounthex.domain.model.dto;

import java.util.Objects;

/**
 * Data Transfer Object for Card information.
 * This is a pure domain DTO without any framework-specific annotations.
 */
public class CardDTO {

    private String mobileNumber;
    private String cardNumber;
    private String cardType;
    private int totalLimit;
    private int amountUsed;
    private int availableAmount;

    public CardDTO() {
    }

    public CardDTO(String mobileNumber, String cardNumber, String cardType, 
                  int totalLimit, int amountUsed, int availableAmount) {
        this.mobileNumber = mobileNumber;
        this.cardNumber = cardNumber;
        this.cardType = cardType;
        this.totalLimit = totalLimit;
        this.amountUsed = amountUsed;
        this.availableAmount = availableAmount;
    }

    // Getters
    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardType() {
        return cardType;
    }

    public int getTotalLimit() {
        return totalLimit;
    }

    public int getAmountUsed() {
        return amountUsed;
    }

    public int getAvailableAmount() {
        return availableAmount;
    }

    // Setters
    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public void setTotalLimit(int totalLimit) {
        this.totalLimit = totalLimit;
    }

    public void setAmountUsed(int amountUsed) {
        this.amountUsed = amountUsed;
    }

    public void setAvailableAmount(int availableAmount) {
        this.availableAmount = availableAmount;
    }

    // Builder pattern implementation
    public static CardDTOBuilder builder() {
        return new CardDTOBuilder();
    }

    public static class CardDTOBuilder {
        private String mobileNumber;
        private String cardNumber;
        private String cardType;
        private int totalLimit;
        private int amountUsed;
        private int availableAmount;

        public CardDTOBuilder mobileNumber(String mobileNumber) {
            this.mobileNumber = mobileNumber;
            return this;
        }

        public CardDTOBuilder cardNumber(String cardNumber) {
            this.cardNumber = cardNumber;
            return this;
        }

        public CardDTOBuilder cardType(String cardType) {
            this.cardType = cardType;
            return this;
        }

        public CardDTOBuilder totalLimit(int totalLimit) {
            this.totalLimit = totalLimit;
            return this;
        }

        public CardDTOBuilder amountUsed(int amountUsed) {
            this.amountUsed = amountUsed;
            return this;
        }

        public CardDTOBuilder availableAmount(int availableAmount) {
            this.availableAmount = availableAmount;
            return this;
        }

        public CardDTO build() {
            return new CardDTO(mobileNumber, cardNumber, cardType, totalLimit, amountUsed, availableAmount);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardDTO cardDTO = (CardDTO) o;
        return totalLimit == cardDTO.totalLimit &&
               amountUsed == cardDTO.amountUsed &&
               availableAmount == cardDTO.availableAmount &&
               Objects.equals(mobileNumber, cardDTO.mobileNumber) &&
               Objects.equals(cardNumber, cardDTO.cardNumber) &&
               Objects.equals(cardType, cardDTO.cardType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mobileNumber, cardNumber, cardType, totalLimit, amountUsed, availableAmount);
    }

    @Override
    public String toString() {
        return "CardDTO{" +
                "mobileNumber='" + mobileNumber + '\'' +
                ", cardNumber='" + cardNumber + '\'' +
                ", cardType='" + cardType + '\'' +
                ", totalLimit=" + totalLimit +
                ", amountUsed=" + amountUsed +
                ", availableAmount=" + availableAmount +
                '}';
    }
}
