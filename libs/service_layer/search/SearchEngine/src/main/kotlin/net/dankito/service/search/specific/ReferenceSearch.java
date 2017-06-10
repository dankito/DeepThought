package net.dankito.service.search.specific;


import net.dankito.deepthought.model.Reference;
import net.dankito.service.search.SearchBase;
import net.dankito.service.search.SearchCompletedListener;

import java.util.ArrayList;
import java.util.Collection;


public class ReferenceSearch extends SearchBase {

  protected Collection<Reference> results = new ArrayList<>();

  protected SearchCompletedListener<Collection<Reference>> completedListener = null;


  public ReferenceSearch(String searchTerm, SearchCompletedListener<Collection<Reference>> completedListener) {
    super(searchTerm);
    this.completedListener = completedListener;
  }


  public void setResults(Collection<Reference> results) {
    this.results = results;
  }

  protected void callCompletedListener() {
    if(completedListener != null)
      completedListener.completed(results);
  }

  @Override
  protected int getResultsCount() {
    return results.size();
  }


  public boolean addResult(Reference result) {
    return this.results.add(result);
  }

  public Collection<Reference> getResults() {
    return results;
  }


}
