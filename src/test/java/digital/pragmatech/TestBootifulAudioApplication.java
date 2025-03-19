package digital.pragmatech;

import org.springframework.boot.SpringApplication;

public class TestBootifulAudioApplication {

	public static void main(String[] args) {
		SpringApplication.from(Application::main).with(TestcontainersConfiguration.class).run(args);
	}

}
