package net.dankito.service.search.specific;

import java.util.ArrayList;
import java.util.List;


public class IndexTerm {

  protected String term;

  protected int numberOfEntriesContainingTerm;

  protected List<Long> entriesContainingTermIds = new ArrayList<>();


  public IndexTerm(String term, int numberOfEntriesContainingTerm) {
    this.term = term;
    this.numberOfEntriesContainingTerm = numberOfEntriesContainingTerm;
  }

  public boolean addEntryContainingTerm(Long id) {
    return entriesContainingTermIds.add(id);
  }


  @Override
  public String toString() {
    return term + " (" + numberOfEntriesContainingTerm + ")";
  }

}
