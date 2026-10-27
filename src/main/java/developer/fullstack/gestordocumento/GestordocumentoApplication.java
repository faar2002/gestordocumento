package developer.fullstack.gestordocumento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class GestordocumentoApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestordocumentoApplication.class, args);
	}

	// Agrega este método para registrar el RestTemplate como un bean de Spring
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
