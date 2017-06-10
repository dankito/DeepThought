package net.dankito.service.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;


public class Search<T> extends SearchBase {

  private final static Logger log = LoggerFactory.getLogger(Search.class);


  protected Collection<T> results = new HashSet<>();

  protected SearchCompletedListener<Collection<T>> completedListener = null;


  public Search(String searchTerm) {
    super(searchTerm);
  }

  public Search(String searchTerm, SearchCompletedListener<Collection<T>> completedListener) {
    this(searchTerm);
    this.completedListener = completedListener;
  }


  public boolean addResult(T result) {
    return results.add(result);
  }

  public void setResults(Collection<T> results) {
    this.results = results;
  }

  public Collection<T> getResults() {
    return results;
  }

  protected void callCompletedListener() {
    if(completedListener != null)
      completedListener.completed(results);
  }

  @Override
  protected int getResultsCount() {
    return results.size();
  }


  @Override
  public String toString() {
    return searchTerm;
  }

}
