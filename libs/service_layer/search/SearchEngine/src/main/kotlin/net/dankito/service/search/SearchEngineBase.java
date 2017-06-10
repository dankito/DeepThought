package net.dankito.service.search;


import net.dankito.deepthought.model.DeepThought;
import net.dankito.deepthought.model.Tag;
import net.dankito.service.search.specific.EntriesSearch;
import net.dankito.service.search.specific.FindAllEntriesHavingTheseTagsResult;
import net.dankito.service.search.specific.ReferenceSearch;
import net.dankito.service.search.specific.TagsSearch;
import net.dankito.service.search.util.CombinedLazyLoadingList;
import net.dankito.utils.IThreadPool;

import java.util.Collection;


public abstract class SearchEngineBase implements ISearchEngine {

  protected DeepThought deepThought = null;

  protected IThreadPool threadPool;


  public SearchEngineBase(IThreadPool threadPool) {
    this.threadPool = threadPool;

    Application.addApplicationListener(applicationListener);
    this.deepThought = Application.getDeepThought();
  }


  public void close() {
    Application.removeApplicationListener(applicationListener);
    // nothing to do here, maybe in sub classes
  }


  @Override
  public void getEntriesForTagAsync(final Tag tag, final SearchCompletedListener<Collection<Entry>> listener) {
    threadPool.runAsync(new Runnable() {
      @Override
      public void run() {
        getEntriesForTag(tag, listener);
      }
    });
  }

  protected abstract void getEntriesForTag(final Tag tag, final SearchCompletedListener<Collection<Entry>> listener);


  @Override
  public void searchTags(final TagsSearch search) {
    if(StringUtils.isNullOrEmpty(search.getSearchTerm()))
      filterTagsForEmptySearchTerm(search);
    else {
      final String[] tagNamesToFilterFor = getTagNamesToSearchFromSearchTerm(search.getSearchTerm());

      threadPool.runAsync(new Runnable() {
        @Override
        public void run() {
          filterTags(search, tagNamesToFilterFor);
        }
      });
    }
  }

  protected String[] getTagNamesToSearchFromSearchTerm(String searchTerm) {
    String lowerCaseFilter = searchTerm.toLowerCase();
    final String[] tagNamesToFilterFor = lowerCaseFilter.split(",");

    for (int i = 0; i < tagNamesToFilterFor.length; i++)
      tagNamesToFilterFor[i] = tagNamesToFilterFor[i].trim();

    return tagNamesToFilterFor;
  }

  protected void filterTagsForEmptySearchTerm(TagsSearch search) {
    search.setRelevantMatchesSorted(new CombinedLazyLoadingList<Tag>(Application.getDeepThought().getSortedTags()));
    search.setHasEmptySearchTerm(true);

    search.fireSearchCompleted();
  }

  protected abstract void filterTags(TagsSearch search, String[] tagNamesToFilterFor);

  @Override
  public void findAllEntriesHavingTheseTags(final Collection<Tag> tagsToFilterFor, final SearchCompletedListener<FindAllEntriesHavingTheseTagsResult> listener) {
    findAllEntriesHavingTheseTags(tagsToFilterFor, "", listener);
  }

  @Override
  public void findAllEntriesHavingTheseTags(final Collection<Tag> tagsToFilterFor, final String searchTerm, final SearchCompletedListener<FindAllEntriesHavingTheseTagsResult> listener) {
    threadPool.runAsync(new Runnable() {
      @Override
      public void run() {
        final String[] tagNamesToFilterFor = getTagNamesToSearchFromSearchTerm(searchTerm);
        findAllEntriesHavingTheseTagsAsync(tagsToFilterFor, tagNamesToFilterFor, listener);
      }
    });
  }

  protected abstract void findAllEntriesHavingTheseTagsAsync(Collection<Tag> tagsToFilterFor, String[] tagNamesToFilterFor, SearchCompletedListener<FindAllEntriesHavingTheseTagsResult> listener);

  @Override
  public void searchEntries(final EntriesSearch search) {
    String lowerCaseFilter = search.getSearchTerm().toLowerCase();
    final String[] termsToFilterFor = lowerCaseFilter.split(" ");

    threadPool.runAsync(new Runnable() {
      @Override
      public void run() {
        filterEntries(search, termsToFilterFor);
      }
    });
  }

  protected abstract void filterEntries(EntriesSearch search, String[] termsToFilterFor);


  @Override
  public void searchReferenceBases(final ReferenceSearch search) {
    // no, don't do this, as they are might not sorted (e.g. when a ReferenceBase gets added during runtime)
//    if(StringUtils.isNullOrEmpty(search.getSearchTerm().trim())) { // no filter term specified -> return all ReferenceBases
//      setReferenceBasesEmptyFilterSearchResult(search);
//      return;
//    }

    final String lowerCaseFilter = search.getSearchTerm().toLowerCase();
    final boolean filterForReferenceHierarchy = lowerCaseFilter.contains(",");

    if(search.getType() != ReferenceBaseType.All)
      filterOnlyOneTypeOfReferenceBase(search, lowerCaseFilter);
    else if(filterForReferenceHierarchy == false) {
      threadPool.runAsync(new Runnable() {
        @Override
        public void run() {
          searchAllReferenceBaseTypesForSameFilter(search, lowerCaseFilter);
        }
      });
    }
    else {
      searchEachReferenceBaseWithSeparateSearchTerm(search, lowerCaseFilter);
    }
  }

  protected void filterOnlyOneTypeOfReferenceBase(ReferenceSearch search, String lowerCaseFilter) {
    if(search.getType() == ReferenceBaseType.SeriesTitle)
      searchEachReferenceBaseWithSeparateSearchTerm(search, lowerCaseFilter, null, null);
    else if(search.getType() == ReferenceBaseType.Reference)
      searchEachReferenceBaseWithSeparateSearchTerm(search, null, lowerCaseFilter, null);
    else if(search.getType() == ReferenceBaseType.ReferenceSubDivision)
      searchEachReferenceBaseWithSeparateSearchTerm(search, null, null, lowerCaseFilter);
  }

  protected void searchEachReferenceBaseWithSeparateSearchTerm(final ReferenceSearch search, String lowerCaseFilter) {
    String seriesTitleFilter = null, referenceFilter = null, referenceSubDivisionFilter = null;
    String[] parts = lowerCaseFilter.split(",");

    seriesTitleFilter = parts[0].trim();
    if(seriesTitleFilter.length() == 0) seriesTitleFilter = null;

    if(parts.length > 1) {
      referenceFilter = parts[1].trim();
      if(referenceFilter.length() == 0) referenceFilter = null;
    }

    if(parts.length > 2) {
      referenceSubDivisionFilter = parts[2].trim();
      if(referenceSubDivisionFilter.length() == 0) referenceSubDivisionFilter = null;
    }

    final String finalSeriesTitleFilter = seriesTitleFilter;
    final String finalReferenceFilter = referenceFilter;
    final String finalReferenceSubDivisionFilter = referenceSubDivisionFilter;

    threadPool.runAsync(new Runnable() {
      @Override
      public void run() {
        searchEachReferenceBaseWithSeparateSearchTerm(search, finalSeriesTitleFilter, finalReferenceFilter, finalReferenceSubDivisionFilter);
      }
    });
  }

  protected abstract void searchAllReferenceBaseTypesForSameFilter(ReferenceSearch search, String referenceBaseFilter);

  protected abstract void searchEachReferenceBaseWithSeparateSearchTerm(ReferenceSearch search, String seriesTitleFilter, String referenceFilter, String FilterReferenceBasesreferenceSubDivisionFilter);



  protected ApplicationListener applicationListener = new ApplicationListener() {
    @Override
    public void deepThoughtChanged(DeepThought deepThought) {
      DeepThought previousDeepThought = SearchEngineBase.this.deepThought;
      SearchEngineBase.this.deepThought = deepThought;
      SearchEngineBase.this.deepThoughtChanged(previousDeepThought, deepThought);
    }

    @Override
    public void notification(Notification notification) {
      if(notification.getType() == NotificationType.ApplicationInstantiated) {
        applicationInstantiated();
      }
      else if(notification.getType() == NotificationType.InitialDatabaseSynchronizationDone) {
        initialDatabaseSynchronizationDone();
      }
    }
  };

  protected void deepThoughtChanged(DeepThought previousDeepThought, DeepThought newDeepThought) {

  }

  protected void applicationInstantiated() {

  }

  protected void initialDatabaseSynchronizationDone() {

  }

}
