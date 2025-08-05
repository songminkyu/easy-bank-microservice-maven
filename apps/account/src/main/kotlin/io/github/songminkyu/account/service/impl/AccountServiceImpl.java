package io.github.songminkyu.account.service.impl;

import static io.github.songminkyu.account.constants.Constants.RANDOM;

import io.github.songminkyu.account.constants.AccountConstants;
import io.github.songminkyu.account.dto.AccountDTO;
import io.github.songminkyu.account.dto.AccountsMsgDTO;
import io.github.songminkyu.account.dto.CustomerDTO;
import io.github.songminkyu.account.entity.Account;
import io.github.songminkyu.account.entity.Customer;
import io.github.songminkyu.account.entity.RevisionEntity;
import io.github.songminkyu.account.exception.CustomerAlreadyExistsException;
import io.github.songminkyu.account.exception.EntityNotFoundException;
import io.github.songminkyu.account.mapper.AccountMapper;
import io.github.songminkyu.account.mapper.CustomerMapper;
import io.github.songminkyu.account.repository.AccountRepository;
import io.github.songminkyu.account.repository.CustomerRepository;
import io.github.songminkyu.account.service.AccountService;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.history.Revision;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    private final AccountMapper accountMapper;

    private final StreamBridge streamBridge;

    @Override
    public void test() {
        var accountsMsgDTO = new AccountsMsgDTO(123123L, "pasongsoso",
                "test@naver.com","040440");
        log.info("Sending Communication request for the details: {}", accountsMsgDTO);
        var result = streamBridge.send("sendCommunication-out-0", accountsMsgDTO);
        log.info("Is the Communication request successfully triggered ? : {}", result);
    }

    @Override
    @Async
    public CompletableFuture<Void> createAccount(CustomerDTO customer) {
        if (customerRepository.existsByMobileNumber(customer.mobileNumber())) {
            throw new CustomerAlreadyExistsException("Customer already registered with given mobileNumber "
                + customer.mobileNumber());
        }
        var customerEntity = customerMapper.toEntity(customer);
        var savedCustomer = customerRepository.save(customerEntity);
        return CompletableFuture.completedFuture(
            accountRepository.save(createNewAccount(savedCustomer)))
            .thenAccept(savedAccount ->   sendCommunication(savedAccount, savedCustomer));
    }

    @Override
    public CustomerDTO fetchAccount(String mobileNumber) {
        var customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
            () -> new EntityNotFoundException(Customer.class, "mobileNumber", mobileNumber)
        );
        var account = accountRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
            () -> new EntityNotFoundException(Account.class, "customerId", customer.getCustomerId().toString())
        );
        return customerMapper.toDto(customer, account);
    }

    @Override
    public List<AccountDTO> getAccountRevisions(Long accountNumber) {
        return accountRepository.findRevisions(accountNumber)
            .stream()
            .map(revision -> accountMapper.toDto(revision.getEntity()))
            .toList();
    }

    @Override
    public String geCreatorUsername(Long accountNumber) {
        var revision = accountRepository.findRevision(accountNumber, 1).orElseThrow(
            () -> new EntityNotFoundException(Revision.class, "accountNumber", String.valueOf(accountNumber))
        );
        var metadata = revision.getMetadata();
        RevisionEntity revisionEntity = metadata.getDelegate();
        return revisionEntity.getUsername();
    }

    @Override
    public boolean updateAccount(Long accountNumber, CustomerDTO customer) {
        boolean isUpdated = false;
        AccountDTO accountDTO = customer.account();
        if (accountDTO != null) {
            var account = accountRepository.findById(accountNumber).orElseThrow(
                () -> new EntityNotFoundException(Account.class, "AccountNumber", accountNumber.toString())
            );
            accountMapper.partialUpdate(account, accountDTO);
            account.setAccountNumber(accountNumber);
            account = accountRepository.save(account);

            Long customerId = account.getCustomerId();
            var customerEntity = customerRepository.findById(customerId).orElseThrow(
                () -> new EntityNotFoundException(Customer.class, "CustomerID", customerId.toString())
            );
            customerMapper.partialUpdate(customerEntity, customer);
            customerRepository.save(customerEntity);
            isUpdated = true;
        }
        return isUpdated;
    }

    @Override
    public void deleteAccount(String mobileNumber) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
            () -> new EntityNotFoundException(Customer.class, "mobileNumber", mobileNumber)
        );
        accountRepository.deleteByCustomerId(customer.getCustomerId());
        customerRepository.deleteById(customer.getCustomerId());
    }

    @Override
    public boolean updateCommunicationStatus(Long accountNumber) {
        boolean isUpdated = false;
        if (accountNumber != null) {
            Account account = accountRepository.findById(accountNumber).orElseThrow(
                () -> new EntityNotFoundException(Account.class, "AccountNumber", accountNumber.toString())
            );
            account.setCommunicationSw(true);
            accountRepository.save(account);
            isUpdated = true;
        }
        return isUpdated;
    }

    private Account createNewAccount(Customer customer) {
        var newAccount = new Account();
        newAccount.setCustomerId(customer.getCustomerId());
        long randomAccNumber = 1000000000L + RANDOM.nextInt(900000000);
        newAccount.setAccountNumber(randomAccNumber);
        newAccount.setAccountType(AccountConstants.SAVINGS);
        newAccount.setBranchAddress(AccountConstants.ADDRESS);
        newAccount.setDeleted(false);
        return newAccount;
    }

    private void sendCommunication(Account account, Customer customer) {
        var accountsMsgDTO = new AccountsMsgDTO(account.getAccountNumber(), customer.getName(),
            customer.getEmail(), customer.getMobileNumber());
        log.info("Sending Communication request for the details: {}", accountsMsgDTO);
        var result = streamBridge.send("sendCommunication-out-0", accountsMsgDTO);
        log.info("Is the Communication request successfully triggered ? : {}", result);
    }

}
