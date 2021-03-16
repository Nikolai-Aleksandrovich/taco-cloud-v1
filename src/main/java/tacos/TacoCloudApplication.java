package tacos;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import tacos.data.IngredientRepository;
import tacos.data.JPAIngredientRepository;
import tacos.data.UserRepository;

@SpringBootApplication
public class TacoCloudApplication {


	public static void main(String[] args) {
		SpringApplication.run(TacoCloudApplication.class, args);
	}

}
