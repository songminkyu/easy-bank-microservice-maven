package io.github.susimsek.account.service;

import io.github.susimsek.account.dto.CustomerDetailsDTO;

public interface CustomerService {

    CustomerDetailsDTO fetchCustomerDetails(String mobileNumber, String correlationId);
}
