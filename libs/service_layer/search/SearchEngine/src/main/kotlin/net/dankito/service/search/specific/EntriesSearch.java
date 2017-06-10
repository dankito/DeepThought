package net.dankito.service.search.specific;


import net.dankito.deepthought.model.Entry;
import net.dankito.deepthought.model.Tag;
import net.dankito.service.search.Search;
import net.dankito.service.search.SearchCompletedListener;

import java.util.ArrayList;
import java.util.Collection;


public class EntriesSearch extends Search<Entry> {

  protected boolean filterAbstract;

  protected boolean filterContent;

  protected Collection<Tag> entriesMustHaveTheseTags = new ArrayList<>();

  protected Collection<Entry> entriesToFilter = new ArrayList<>();

  protected boolean filterOnlyEntriesWithoutTags = false;


  public EntriesSearch(String searchTerm, boolean filterContent, boolean filterAbstract) {
    super(searchTerm);

    this.filterContent = filterContent;
    this.filterAbstract = filterAbstract;
  }

  public EntriesSearch(String searchTerm, boolean filterContent, boolean filterAbstract, SearchCompletedListener<Collection<Entry>> completedListener) {
    super(searchTerm, completedListener);

    this.filterContent = filterContent;
    this.filterAbstract = filterAbstract;
  }

  public EntriesSearch(String searchTerm, boolean filterContent, boolean filterAbstract, boolean filterOnlyEntriesWithoutTags, SearchCompletedListener<Collection<Entry>> completedListener) {
    this(searchTerm, filterContent, filterAbstract, completedListener);

    this.filterOnlyEntriesWithoutTags = filterOnlyEntriesWithoutTags;
  }

  public EntriesSearch(String searchTerm, boolean filterContent, boolean filterAbstract, Collection<Tag> entriesMustHaveTheseTags, SearchCompletedListener<Collection<Entry>> completedListener) {
    this(searchTerm, filterContent, filterAbstract, completedListener);

    this.entriesMustHaveTheseTags = entriesMustHaveTheseTags;
  }


  public boolean filterAbstract() {
    return filterAbstract;
  }

  public void setFilterAbstract(boolean filterAbstract) {
    this.filterAbstract = filterAbstract;
  }

  public boolean filterContent() {
    return filterContent;
  }

  public void setFilterContent(boolean filterContent) {
    this.filterContent = filterContent;
  }

  public Collection<Tag> getEntriesMustHaveTheseTags() {
    return entriesMustHaveTheseTags;
  }

  public void setEntriesMustHaveTheseTags(Collection<Tag> entriesMustHaveTheseTags) {
    this.entriesMustHaveTheseTags = entriesMustHaveTheseTags;
  }

  public boolean addTagEntriesMustHave(Tag tag) {
    return entriesMustHaveTheseTags.add(tag);
  }

  public boolean filterOnlyEntriesWithoutTags() {
    return filterOnlyEntriesWithoutTags;
  }

  public void setSearchOnlyEntriesWithoutTags(boolean filterOnlyEntriesWithoutTags) {
    this.filterOnlyEntriesWithoutTags = filterOnlyEntriesWithoutTags;
  }
}
