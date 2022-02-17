package com.zinkworks.atmapplication.api;
import com.zinkworks.atmapplication.api.exception.InvalidAccountException;
import com.zinkworks.atmapplication.service.atm.account.AccountService;
import com.zinkworks.atmapplication.service.atm.dto.RetrieveAccountBalanceRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@Slf4j
public class HeartBeatEndpoint {

    private final AccountService accountService;
    public HeartBeatEndpoint(final AccountService accountService)
    {
        this.accountService = accountService;
    }

    @GetMapping(path="/heartbeat")
    public @ResponseBody
    String isApplicationRunning () {

        return "Application is up and Running";

    }
}


