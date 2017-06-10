package net.dankito.service.search.util;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


public class CombinedLazyLoadingList<T> extends AbstractList<T> {

  protected List<Collection<T>> combinedUnderlyingCollections = new ArrayList<>();


  public CombinedLazyLoadingList() {

  }

  public CombinedLazyLoadingList(Collection<T>... underlyingCollections) {
    for(Collection<T> underlyingCollection : underlyingCollections)
      combinedUnderlyingCollections.add(underlyingCollection);
  }


  @Override
  public int size() {
    int size = 0;
    for(Collection<T> underlyingCollection : combinedUnderlyingCollections)
      size += underlyingCollection.size();
    return size;
  }

  @Override
  public T get(int index) {
    int count = 0;
    Collection<T> underlyingCollectionForThisIndex = null;

    for(Collection<T> underlyingCollection : combinedUnderlyingCollections) {
      if(underlyingCollection.size() + count > index) {
        underlyingCollectionForThisIndex = underlyingCollection;
        break;
      }
      count += underlyingCollection.size();
    }

    if(underlyingCollectionForThisIndex != null)
      return getItemFromCollection(index - count, underlyingCollectionForThisIndex);

    return null;
  }

  protected T getItemFromCollection(int index, Collection<T> collection) {
    if(collection instanceof List)
      return ((List<T>) collection).get(index);

    int i = 0;
    Iterator<T> iterator = collection.iterator();
    while(iterator.hasNext()) {
      if(i == index)
        return iterator.next();

      iterator.next();
      i++;
    }

    return null;
  }

  public void setUnderlyingCollection(Collection<T> underlyingCollection) {
    clear();

    combinedUnderlyingCollections.add(underlyingCollection);
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
    return combinedUnderlyingCollections.add((Collection<T>)c);
  }

  @Override
  public void clear() {
    combinedUnderlyingCollections.clear();
  }
}
