package dev.krocon.shop.ep018;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cache.cfg.spi.DomainDataRegionBuildingContext;
import org.hibernate.cache.cfg.spi.DomainDataRegionConfig;
import org.hibernate.cache.spi.support.DomainDataStorageAccess;
import org.hibernate.cache.spi.support.RegionFactoryTemplate;
import org.hibernate.cache.spi.support.StorageAccess;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

/**
 * Minimaler Second-Level-Cache-Provider auf ConcurrentHashMap – NUR für die Demo,
 * damit der Test ohne zusätzliche Dependency läuft. In echten Projekten: JCache
 * (hibernate-jcache + Ehcache/Caffeine), siehe episodes/018/README.md.
 * Kein Ablauf, keine Größenbegrenzung, nicht clusterfähig.
 */
public class MapRegionFactory extends RegionFactoryTemplate {

  @Override
  protected void prepareForUse(SessionFactoryOptions options, Map<String, Object> configValues) { }

  @Override
  protected void releaseFromUse() { }

  @Override
  protected DomainDataStorageAccess createDomainDataStorageAccess(
      DomainDataRegionConfig regionConfig, DomainDataRegionBuildingContext buildingContext) {
    return new MapStorage();
  }

  @Override
  protected StorageAccess createQueryResultsRegionStorageAccess(
      String regionName, SessionFactoryImplementor sessionFactory) {
    return new MapStorage();
  }

  @Override
  protected StorageAccess createTimestampsRegionStorageAccess(
      String regionName, SessionFactoryImplementor sessionFactory) {
    return new MapStorage();
  }

  /** Eine Region = eine Map. */
  static class MapStorage implements DomainDataStorageAccess {
    private final Map<Object, Object> data = new ConcurrentHashMap<>();

    @Override
    public Object getFromCache(Object key, SharedSessionContractImplementor session) {
      return data.get(key);
    }

    @Override
    public void putIntoCache(Object key, Object value, SharedSessionContractImplementor session) {
      data.put(key, value);
    }

    @Override
    public void removeFromCache(Object key, SharedSessionContractImplementor session) {
      data.remove(key);
    }

    @Override
    public void clearCache(SharedSessionContractImplementor session) {
      data.clear();
    }

    @Override
    public boolean contains(Object key) {
      return data.containsKey(key);
    }

    @Override
    public void evictData() {
      data.clear();
    }

    @Override
    public void evictData(Object key) {
      data.remove(key);
    }

    @Override
    public void release() {
      data.clear();
    }
  }
}
