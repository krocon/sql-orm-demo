package dev.krocon.shop.ep035;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;

/**
 * Startwarnung, wenn spring.jpa.open-in-view NICHT gesetzt ist.
 * Statt application.yaml wird ep035-osiv-unset.yaml geladen (gleiche Einstellungen, aber ohne open-in-view).
 */
@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(properties = "spring.config.name=ep035-osiv-unset")
@Import(TestcontainersConfiguration.class)
class OsivWarningTest {

  @Test
  void warnsAtStartup(CapturedOutput output) {
    assertThat(output.getOut()).contains(
        "spring.jpa.open-in-view is enabled by default. Therefore, database queries may be "
            + "performed during view rendering. Explicitly configure spring.jpa.open-in-view "
            + "to disable this warning");
  }
}
