package dev.krocon.shop.ep026;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;

/** Fake-Mailversand: merkt sich nur, an wen „verschickt“ wurde. */
@Service("ep026MailService")
public class MailService {

  private final List<String> sent = new CopyOnWriteArrayList<>();

  public void sendWelcome(Ep026Customer c) {
    sent.add(c.getEmail());
  }

  public List<String> sent() { return sent; }
}
