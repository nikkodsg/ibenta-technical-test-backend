package au.com.ibenta.test.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/test-actuator")
public class ActuatorController {

    private RestTemplate restTemplate;

    @Autowired
    public ActuatorController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/health-status")
    public String getHealthStatus() {
        return restTemplate.getForObject("http://authentication-service-jx-staging.gitops.ibenta.com/actuator/health", String.class);
    }
}
