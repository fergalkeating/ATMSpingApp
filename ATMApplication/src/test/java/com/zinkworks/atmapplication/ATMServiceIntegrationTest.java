package com.zinkworks.atmapplication;

import com.zinkworks.atmapplication.repository.atm.ATMRepository;
import com.zinkworks.atmapplication.repository.atm.CurrencyRepository;
import com.zinkworks.atmapplication.repository.customer.CustomerAccountRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.security.converter.RsaKeyConverters.x509;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ATMApplication.class)
@AutoConfigureMockMvc
public class ATMServiceIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CustomerAccountRepository customerAccountRepository;

    @Autowired
    ATMRepository atmRepository;

    @Autowired
    CurrencyRepository currencyRepository;

    @Test
    public void givenHealthcheck__thenStatus200()
            throws Exception {

        mvc.perform(get("/heartbeat")
                        .with(SecurityMockMvcRequestPostProcessors.x509("keystore/atmapplication.crt"))
                        .contentType(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                //.andExpect(jsonPath("$[0].name", is("bob")));
                .andExpect(content().string("Application is up and Running"));
    }

    @Test
    @WithMockUser(value = "user")
    public void getAccountData_thenStatus200()
            throws Exception {

        mvc.perform(get("/atm/account/1234")
                        .with(SecurityMockMvcRequestPostProcessors.x509("keystore/atmapplication.crt"))
                        .header("Authorization", "Basic MTIzNDoxMjM0")
                        .contentType(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accountId", is("1234")))
                .andExpect(jsonPath("$.accountBalance", is("300")));
    }

    @Test
    @WithMockUser(value = "user")
    public void patchithdrawMoneyFromAccount_thenStatus200()
            throws Exception {

        mvc.perform(patch("/atm/account/1234/withdraw?amount=20")
                        .with(SecurityMockMvcRequestPostProcessors.x509("keystore/atmapplication.crt"))
                        .header("Authorization", "Basic MTIzNDoxMjM0")
                        .contentType(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accountId", is("1234")))
                .andExpect(jsonPath("$.accountBalance", is("280")))
                .andExpect(jsonPath("$.amountWithdrawn", is("20")));
    }

    @Test
    @WithMockUser(value = "user")
    public void patchithdrawTooMuchMoneyFromAccount_thenStatus200()
            throws Exception {

        mvc.perform(patch("/atm/account/1234/withdraw?amount=5000")
                        .with(SecurityMockMvcRequestPostProcessors.x509("keystore/atmapplication.crt"))
                        .header("Authorization", "Basic MTIzNDoxMjM0")
                        .contentType(MediaType.ALL))
                .andExpect(status().is(400))
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("The amount requested exceeds the available balance of your account")));
    }

    @Test
    @WithMockUser(value = "user")
    public void patch_withdrawTooMuchMoneyFromAtm_thenStatus400()
            throws Exception {

        mvc.perform(patch("/atm/account/1238/withdraw?amount=1600")
                        .with(SecurityMockMvcRequestPostProcessors.x509("keystore/atmapplication.crt"))
                        .header("Authorization", "Basic MTIzNDoxMjM0")
                        .contentType(MediaType.ALL))
                .andExpect(status().is(400))
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("The balance requested is not available at this ATM. Please try a different amount in multiples of 5, 10, 20 and 50.")));
    }

}
