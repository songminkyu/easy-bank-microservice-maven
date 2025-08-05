package io.github.songminkyu.account.service;

import io.github.songminkyu.account.dto.AccountDTO;
import io.github.songminkyu.account.dto.CustomerDTO;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AccountService {

    CompletableFuture<Void> createAccount(CustomerDTO customer);

    CustomerDTO fetchAccount(String mobileNumber);

    List<AccountDTO> getAccountRevisions(Long accountNumber);

    String geCreatorUsername(Long accountNumber);

    boolean updateAccount(Long accountNumber, CustomerDTO customer);

    void deleteAccount(String mobileNumber);

    boolean updateCommunicationStatus(Long accountNumber);

    void test();
}
