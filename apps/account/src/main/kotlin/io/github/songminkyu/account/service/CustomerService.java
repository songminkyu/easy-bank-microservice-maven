package io.github.songminkyu.account.service;

import io.github.songminkyu.account.dto.CustomerDetailsDTO;

public interface CustomerService {

    CustomerDetailsDTO fetchCustomerDetails(String mobileNumber, String correlationId);
}
