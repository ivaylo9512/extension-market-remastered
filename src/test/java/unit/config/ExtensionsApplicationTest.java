package unit.config;

import com.tick42.quicksilver.ExtensionsApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
public class ExtensionsApplicationTest {
    @Test
    public void start(){
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {

            mocked.when(() -> SpringApplication.run(ExtensionsApplication.class,
                            "arg1", "arg2"))
                    .thenReturn(Mockito.mock(ConfigurableApplicationContext.class));

            ExtensionsApplication.main(new String[] { "arg1", "arg2" });

            mocked.verify(() -> SpringApplication.run(ExtensionsApplication.class,
                    "arg1", "arg2"));

        }
    }
}
