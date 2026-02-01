package portfolio_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import portfolio_service.api.NotFoundException;
import portfolio_service.config.TenantContext;
import portfolio_service.domain.PortfolioPosition;
import portfolio_service.dto.CreatePositionRequest;
import portfolio_service.dto.PositionResponse;
import portfolio_service.dto.UpdatePositionRequest;
import portfolio_service.redis.CacheTtl;
import portfolio_service.redis.PositionProtoMapper;
import portfolio_service.redis.RedisBytesCache;
import portfolio_service.redis.RedisKeys;
import portfolio_service.redis.RedisLock;
import portfolio_service.repository.PortfolioPositionRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PortfolioPositionService {

    private static final String ENV = "prod";

    private final PortfolioPositionRepository repo;
    private final RedisBytesCache cache;
    private final RedisLock lock;

    @Transactional
    public PositionResponse create(CreatePositionRequest req) {
        PortfolioPosition p = new PortfolioPosition();
        p.setId(UUID.randomUUID());
        p.setClientId(req.getClientId().trim());
        p.setSymbol(req.getSymbol().trim());
        p.setQuantity(req.getQuantity());
        p.setAvgPrice(req.getAvgPrice());
        p.setUpdatedAt(Instant.now());

        PortfolioPosition saved = repo.save(p);

        safeDel(clientListKey(saved.getClientId()));

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PositionResponse getById(UUID id) {
        String cacheKey = positionKey(id.toString());
        String lockKey  = RedisKeys.positionByIdLock(ENV, tenantNs(), id.toString());

        try {
            byte[] bytes = cache.get(cacheKey);
            if (bytes != null) {
                return PositionProtoMapper.fromBytes(bytes);
            }
        } catch (Exception ignoreFailOpen) {}

        String token = null;
        try {
            token = lock.tryLock(lockKey, CacheTtl.cacheRebuildLockTtl());
            if (token == null) {
                PositionResponse cached = waitForCache(cacheKey);
                if (cached != null) return cached;

                return loadFromDbAndBestEffortCacheById(id, cacheKey);
            }

            try {
                byte[] bytes2 = cache.get(cacheKey);
                if (bytes2 != null) {
                    return PositionProtoMapper.fromBytes(bytes2);
                }
            } catch (Exception ignoreFailOpen) {}

            PositionResponse resp = loadFromDb(id);

            try {
                cache.set(cacheKey, PositionProtoMapper.toBytes(resp), CacheTtl.positionTtlWithJitter());
            } catch (Exception ignore) {}

            return resp;

        } finally {
            if (token != null) {
                try { lock.unlock(lockKey, token); } catch (Exception ignore) {}
            }
        }
    }

    @Transactional(readOnly = true)
    public List<PositionResponse> listByClient(String clientId) {
        String normalized = clientId.trim();
        String cacheKey = clientListKey(normalized);
        String lockKey = RedisKeys.positionsByClientLock(ENV, tenantNs(), normalized);

        try {
            byte[] bytes = cache.get(cacheKey);
            if (bytes != null) {
                return PositionProtoMapper.listFromBytes(bytes);
            }
        } catch (Exception ignoreFailOpen) {}

        String token = null;
        try {
            token = lock.tryLock(lockKey, CacheTtl.cacheRebuildLockTtl());
            if (token == null) {
                List<PositionResponse> cached = waitForCacheList(cacheKey);
                if (cached != null) return cached;

                return loadListFromDbAndBestEffortCache(normalized, cacheKey);
            }

            try {
                byte[] bytes2 = cache.get(cacheKey);
                if (bytes2 != null) {
                    return PositionProtoMapper.listFromBytes(bytes2);
                }
            } catch (Exception ignoreFailOpen) {}

            List<PositionResponse> resp = repo.findByClientId(normalized).stream()
                    .map(this::toResponse)
                    .toList();

            try {
                cache.set(cacheKey, PositionProtoMapper.listToBytes(resp), CacheTtl.clientListTtl());
            } catch (Exception ignore) {}

            return resp;

        } finally {
            if (token != null) {
                try { lock.unlock(lockKey, token); } catch (Exception ignore) {}
            }
        }
    }

    @Transactional
    public PositionResponse update(UUID id, UpdatePositionRequest req) {
        PortfolioPosition p = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Position not found: " + id));

        p.setSymbol(req.getSymbol().trim());
        p.setQuantity(req.getQuantity());
        p.setAvgPrice(req.getAvgPrice());
        p.setUpdatedAt(Instant.now());

        safeDel(positionKey(id.toString()));
        safeDel(clientListKey(p.getClientId()));

        return toResponse(p);
    }

    @Transactional
    public void delete(UUID id) {
        PortfolioPosition p = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Position not found: " + id));

        repo.delete(p);

        safeDel(positionKey(id.toString()));
        safeDel(clientListKey(p.getClientId()));
    }

    private PositionResponse loadFromDbAndBestEffortCacheById(UUID id, String cacheKey) {
        PositionResponse resp = loadFromDb(id);
        try {
            cache.set(cacheKey, PositionProtoMapper.toBytes(resp), CacheTtl.positionTtlWithJitter());
        } catch (Exception ignore) {}
        return resp;
    }

    private List<PositionResponse> loadListFromDbAndBestEffortCache(String clientId, String cacheKey) {
        List<PositionResponse> resp = repo.findByClientId(clientId).stream()
                .map(this::toResponse)
                .toList();
        try {
            cache.set(cacheKey, PositionProtoMapper.listToBytes(resp), CacheTtl.clientListTtl());
        } catch (Exception ignore) {}
        return resp;
    }

    private PositionResponse loadFromDb(UUID id) {
        PortfolioPosition p = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Position not found: " + id));
        return toResponse(p);
    }

    private PositionResponse toResponse(PortfolioPosition p) {
        return PositionResponse.builder()
                .id(p.getId())
                .clientId(p.getClientId())
                .symbol(p.getSymbol())
                .quantity(p.getQuantity())
                .avgPrice(p.getAvgPrice())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private String tenantNs() {
        return "asset:" + TenantContext.get();
    }

    private String positionKey(String id) {
        return RedisKeys.positionById(ENV, tenantNs(), id);
    }

    private String clientListKey(String clientId) {
        return RedisKeys.positionsByClient(ENV, tenantNs(), clientId);
    }

    private void safeDel(String key) {
        try { cache.del(key); } catch (Exception ignore) {}
    }

    private PositionResponse waitForCache(String cacheKey) {
        int[] waitsMs = {50, 80, 120, 200};
        for (int w : waitsMs) {
            sleepSilently(w);
            try {
                byte[] bytes = cache.get(cacheKey);
                if (bytes != null) return PositionProtoMapper.fromBytes(bytes);
            } catch (Exception ignore) {}
        }
        return null;
    }

    private List<PositionResponse> waitForCacheList(String cacheKey) {
        int[] waitsMs = {50, 80, 120, 200};
        for (int w : waitsMs) {
            sleepSilently(w);
            try {
                byte[] bytes = cache.get(cacheKey);
                if (bytes != null) return PositionProtoMapper.listFromBytes(bytes);
            } catch (Exception ignore) {}
        }
        return null;
    }

    private void sleepSilently(int ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
