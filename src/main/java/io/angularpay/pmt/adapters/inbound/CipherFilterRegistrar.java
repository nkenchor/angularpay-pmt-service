package io.angularpay.pmt.adapters.inbound;

import io.angularpay.pmt.adapters.outbound.CipherServiceAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CipherFilterRegistrar {

    @ConditionalOnProperty(
            value = "angularpay.cipher.enabled",
            havingValue = "true",
            matchIfMissing = true)
    @Bean
    public FilterRegistrationBean<CipherFilter> registerPostCommentsRateLimiter(CipherServiceAdapter cipherServiceAdapter) {
        FilterRegistrationBean<CipherFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CipherFilter(cipherServiceAdapter));
        registrationBean.addUrlPatterns(
                "/pmt/requests",
                "/pmt/requests/*/amount",
                "/pmt/requests/*/exchange-rate",
                "/pmt/requests/*/beneficiary",
                "/pmt/requests/*/investors",
                "/pmt/requests/*/bargains",
                "/pmt/requests/*/investors/*/amount",
                "/pmt/requests/*/investors/*/payment"
        );
        return registrationBean;
    }
}
