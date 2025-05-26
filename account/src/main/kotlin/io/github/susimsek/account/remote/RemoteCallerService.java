package io.github.susimsek.account.remote;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RemoteCallerService {

    @RemoteEndpoint
    private final RemoteService remoteService;

    void doSomething() {
        remoteService.doSomething();
    }
}
