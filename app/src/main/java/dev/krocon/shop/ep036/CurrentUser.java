package dev.krocon.shop.ep036;

/** Ersatz für Spring Security in der Demo: aktueller Benutzer pro Thread. */
public final class CurrentUser {

  private static final ThreadLocal<String> USER = new ThreadLocal<>();

  private CurrentUser() { }

  public static void set(String user) { USER.set(user); }
  public static void clear() { USER.remove(); }
  public static String get() { return USER.get(); }
}
