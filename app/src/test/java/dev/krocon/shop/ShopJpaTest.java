package dev.krocon.shop;

import java.lang.annotation.*;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

/**
 * JPA-Slice-Test gegen echte PostgreSQL (kein H2, Folge 039).
 * Jeder Test läuft in einer Transaktion, die am Ende zurückgerollt wird.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
public @interface ShopJpaTest {
}
