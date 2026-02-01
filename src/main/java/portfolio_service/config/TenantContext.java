package portfolio_service.config;

public final class TenantContext {
    private static final ThreadLocal<String> TENANT = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(String tenant) { TENANT.set(tenant); }

    public static String get() {
        String t = TENANT.get();
        return (t == null || t.isBlank()) ? "default" : t.trim();
    }

    public static void clear() { TENANT.remove(); }
}
