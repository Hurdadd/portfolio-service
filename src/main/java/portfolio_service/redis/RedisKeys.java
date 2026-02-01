package portfolio_service.redis;

public final class RedisKeys {
    private RedisKeys() {}

    public static String positionById(String env, String tenant, String id) {
        return env + ":" + tenant + ":cache:position:" + id;
    }

    public static String positionsByClient(String env, String tenant, String clientId) {
        return env + ":" + tenant + ":cache:client:" + clientId + ":positions";
    }

    public static String rateLimitKey(String env, String tenant, String clientId, String endpoint, long windowStartSec) {
        return env + ":" + tenant + ":rl:" + clientId + ":" + endpoint + ":" + windowStartSec;
    }

    public static String lockKey(String env, String tenant, String name) {
        return env + ":" + tenant + ":lock:" + name;
    }
    public static String positionByIdLock(String env, String tenantNs, String id) {
        return env + ":" + tenantNs + ":lock:cache:position:" + id;
    }

    public static String positionsByClientLock(String env, String tenantNs, String clientId) {
        return env + ":" + tenantNs + ":lock:cache:positionsByClient:" + clientId;
    }

}
