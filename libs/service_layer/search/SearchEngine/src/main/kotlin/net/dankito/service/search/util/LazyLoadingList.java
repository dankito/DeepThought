package net.dankito.service.search.util;

import net.dankito.data_access.database.IEntityManager;
import net.dankito.deepthought.model.BaseEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;


public class LazyLoadingList<T extends BaseEntity> extends AbstractList<T> {

  private final Logger log = LoggerFactory.getLogger(LazyLoadingList.class);


  protected IEntityManager entityManager;

  protected Class<T> resultType;

  protected Collection<String> entityIds;

  protected Map<Integer, T> cachedResults = new HashMap<>();

  protected int countEntitiesToQueryOnDatabaseAccess = 20;


  public LazyLoadingList(IEntityManager entityManager, Class<T> resultType) {
    this(entityManager, resultType, new HashSet<String>());
  }

  public LazyLoadingList(IEntityManager entityManager, Class<T> resultType, Collection<String> entityIds) {
    this.entityManager = entityManager;
    this.resultType = resultType;
    this.entityIds = entityIds;
  }


  @Override
  public int size() {
    return entityIds.size();
  }

  @Override
  public void clear() {
    entityIds.clear();
    cachedResults.clear();
  }

  @Override
  public T get(int index) {
    if(cachedResults.containsKey(index))
      return cachedResults.get(index);

    try {
//      Long id = getEntityIdForIndex(index);
//
//      T item = Application.getEntityManager().getEntityById(resultType, id);
//      cachedResults.put(index, item);
//
//      return item;

      long startTime = new Date().getTime();
      List<String> idsOfNextEntities = getNextEntityIdsForIndex(index, countEntitiesToQueryOnDatabaseAccess);
      List<BaseEntity> loadedEntities = (List<BaseEntity>)entityManager.getEntitiesById(resultType, idsOfNextEntities, false);

      for(int i = 0; i < idsOfNextEntities.size(); i++ ) {
        T item = findItemById((List<T>)loadedEntities, idsOfNextEntities.get(i));
        if(item != null)
          cachedResults.put(index + i, item);
      }

      long elapsed = new Date().getTime() - startTime;
      log.debug("Preloaded {} Entities in {} milliseconds", idsOfNextEntities.size(), elapsed);
      return cachedResults.get(index);
    } catch(Exception ex) {
      log.error("Could not load Result of type " + resultType + " from Lucene search results", ex);
    }

    return null;
  }

  protected T findItemById(List<T> entities, String id) {
    for(BaseEntity entity : entities) {
      if(id.equals(entity.getId()))
        return (T)entity;
    }

    return null;
  }

  protected String getEntityIdForIndex(int index) {
    if(entityIds instanceof List == true)
      return ((List<String>)entityIds).get(index);

    Iterator<String> iterator = entityIds.iterator();
    int i = 0;
    while(iterator.hasNext()) {
      if(i == index)
        return iterator.next();

      i++;
      iterator.next();
    }

    entityIds = new ArrayList<>(entityIds); // last resort: quite a bad solution as in this way all items of entityIds will be traverse (and therefor loaded if it's a lazy  loading list
    return ((List<String>)entityIds).get(index);
  }

  protected List<String> getNextEntityIdsForIndex(int index, int maxCountIdsToReturn) {
    List<String> ids = new ArrayList<>();

    for(int i = index; i < (index + maxCountIdsToReturn < size() ? index + maxCountIdsToReturn : size()); i++)
      ids.add(getEntityIdForIndex(i));

    return ids;
  }

  @Override
  public Iterator<T> iterator() {
    loadAllResults();
    return super.iterator();
  }

  @Override
  public ListIterator<T> listIterator(int index) {
    loadAllResults();
    return super.listIterator(index);
  }

  protected void loadAllResults() {
//    log.debug("An iterator has been called on LazyLoadingList with " + entityIds.size() + " Entity IDs, therefor all Entities will now be loaded");
//    try { throw new Exception(); } catch(Exception ex) { log.debug("Stacktrace is:", ex); }

    if (cachedResults.size() < size()) {
      try {
        List<T> allItems = entityManager.getEntitiesById(resultType, entityIds, true);

        int i = 0;
        for(T item : allItems) {
          cachedResults.put(i, item);
          i++;
        }
      } catch (Exception ex) {
        log.error("Could not retrieve all result items from Lucene search result for result type " + resultType, ex);
      }
    }
  }

  @Override
  public void add(int index, T element) {
    if(entityIds instanceof List) {
      ((List)entityIds).add(index, element.getId());
      cachedResults.put(index, element);
    }
    else {
      entityIds.add(element.getId());
      cachedResults.put(cachedResults.size(), element);
    }
  }

  @Override
  public boolean remove(Object element) {
    if(cachedResults.containsValue(element)) {
      for(Map.Entry<Integer, T> entry : cachedResults.entrySet()) {
        if(element.equals(entry.getValue())) {
          Integer index = entry.getKey();
          cachedResults.remove(index);
          break;
        }
      }
    }

    try {
      return entityIds.remove(((T)element).getId());
    } catch(Exception ex) {

    }

    return false;
  }

  public Collection<String> getEntityIds() {
    return entityIds;
  }
}
