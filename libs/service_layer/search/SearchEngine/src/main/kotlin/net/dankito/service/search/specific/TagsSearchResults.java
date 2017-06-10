package net.dankito.service.search.specific;


import net.dankito.deepthought.model.Tag;
import net.dankito.service.search.SearchBase;
import net.dankito.service.search.util.CombinedLazyLoadingList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class TagsSearchResults {

  public final static TagsSearchResults EmptySearchResults = new TagsSearchResults(SearchBase.EmptySearchTerm, new ArrayList<Tag>());


  protected String overAllSearchTerm;

  protected boolean hasEmptySearchTerm = false;

  protected List<TagsSearchResult> results = new ArrayList<>();

  protected Collection<Tag> allMatches = null;

  protected Collection<Tag> relevantMatchesSorted = new ArrayList<>();

  protected Collection<Tag> exactMatches = null;

  protected Collection<Tag> exactOrSingleMatchesNotOfLastResult = null;

  protected Collection<Tag> matchesButOfLastResult = null;


  public TagsSearchResults() {

  }

  public TagsSearchResults(String overAllSearchTerm) {
    this();
    this.overAllSearchTerm = overAllSearchTerm;
  }

  public TagsSearchResults(String overAllSearchTerm, Collection<Tag> relevantMatchesSorted) {
    this(overAllSearchTerm);
    setRelevantMatchesSorted(relevantMatchesSorted);
  }


  public boolean addSearchResult(TagsSearchResult result) {
    allMatches = null;
    return results.add(result);
  }

  public int getRelevantMatchesCount() {
    if(getRelevantMatchesSorted() != null)
      return getRelevantMatchesSorted().size();
    return 0;
  }

  public Collection<Tag> getRelevantMatchesSorted() {
    if(relevantMatchesSorted == null)
      return getAllMatches();
    return relevantMatchesSorted;
  }

  public void setRelevantMatchesSorted(Collection<Tag> relevantMatchesSorted) {
    this.relevantMatchesSorted = relevantMatchesSorted;

    // TODO: what was that good for?
    if(results.size() == 0)
      addSearchResult(new TagsSearchResult(getOverAllSearchTerm(), relevantMatchesSorted, null));
  }


  public Collection<Tag> getAllMatches() {
    if(allMatches == null)
      allMatches = determineAllMatches();

    return allMatches;
  }



  public Collection<Tag> getExactMatches() {
    if(exactMatches == null)
      exactMatches = determineExactMatches();

    return exactMatches;
  }

  protected Collection<Tag> determineExactMatches() {
    Collection<Tag> exactMatches = new ArrayList<>();

    for(TagsSearchResult result : getResults()) {
      if(result.hasExactMatch())
        exactMatches.add(result.getExactMatch());
    }

    return exactMatches;
  }


  public boolean isExactOrSingleMatchButNotOfLastResult(Tag tag) {
    if(hasEmptySearchTerm()) // no exact or relevant matches
      return false;
    if(results.size() < 2) // no or only one (= last) result
      return false;

    return getExactOrSingleMatchesNotOfLastResult().contains(tag);
  }

  public boolean isMatchButNotOfLastResult(Tag tag) {
    if(hasEmptySearchTerm()) // no exact or relevant matches
      return false;
    if(results.size() < 2) // no or only one (= last) result
      return false;

    return getMatchesNotOfLastResult().contains(tag);
  }

  public boolean isExactMatchOfLastResult(Tag tag) {
    if(hasEmptySearchTerm()) // no exact or relevant matches
      return false;
    if(hasLastResult() == false)
      return false;

    TagsSearchResult lastResult = getLastResult();
    return lastResult.hasExactMatch() && lastResult.getExactMatch().equals(tag);
  }

  public boolean isSingleMatchOfLastResult(Tag tag) {
    if(hasEmptySearchTerm()) // no exact or relevant matches
      return false;
    if(hasLastResult() == false)
      return false;

    TagsSearchResult lastResult = getLastResult();
    return lastResult.hasSingleMatch() && lastResult.getSingleMatch().equals(tag);
  }

  public boolean isMatchOfLastResult(Tag tag) {
    if(hasEmptySearchTerm()) // no exact or relevant matches
      return false;
    if(hasLastResult())
      return false;

    return getLastResult().getAllMatches().contains(tag);
  }


  protected Collection<Tag> getExactOrSingleMatchesNotOfLastResult() {
    if(exactOrSingleMatchesNotOfLastResult == null)
      exactOrSingleMatchesNotOfLastResult = determineExactOrSingleMatchesNotOfLastResult();
    return exactOrSingleMatchesNotOfLastResult;
  }

  protected Collection<Tag> determineExactOrSingleMatchesNotOfLastResult() {
    List<Tag> nonLastResultExactOrSingleMatches = new ArrayList<>();

    for(int i = 0; i < results.size() - 1; i++) {
      TagsSearchResult result = results.get(i);
      if(result.hasExactMatch())
        nonLastResultExactOrSingleMatches.add(result.getExactMatch());
      else if(result.hasSingleMatch())
        nonLastResultExactOrSingleMatches.add(result.getSingleMatch());
    }

    return nonLastResultExactOrSingleMatches;
  }

  protected Collection<Tag> getMatchesNotOfLastResult() {
    if(matchesButOfLastResult == null)
      matchesButOfLastResult = determineMatchesNotOfLastResult();
    return matchesButOfLastResult;
  }

  protected Collection<Tag> determineMatchesNotOfLastResult() {
    List<Tag> nonLastResultNotExactOrSingleMatches = new ArrayList<>();

    for(int i = 0; i < results.size() - 1; i++) {
      TagsSearchResult result = results.get(i);
      if(result.hasExactMatch() == false && result.hasSingleMatch() == false)
        nonLastResultNotExactOrSingleMatches.addAll(result.getAllMatches());
    }

    return nonLastResultNotExactOrSingleMatches;
  }


  protected Collection<Tag> determineAllMatches() {
    CombinedLazyLoadingList<Tag> allMatches = new CombinedLazyLoadingList<>();

    for(TagsSearchResult result : getResults()) {
      allMatches.addAll(result.getAllMatches());
    }

    return allMatches;
  }


  protected boolean hasLastResult() {
    return results.size() > 0; // no result (and therefore not last result) at all
  }

  public boolean hasLastResultExactMatch() {
    if(hasLastResult() == false)
      return false;

    TagsSearchResult lastResult = getLastResult();
    return lastResult.hasExactMatch();
  }

  public TagsSearchResult getLastResult() {
    if(results.size() == 0) {
      return null;
    }

    return results.get(results.size() - 1);
  }

  public Tag getExactMatchesOfLastResult() {
    TagsSearchResult lastResult = getLastResult();
    if(lastResult != null) {
      return lastResult.getExactMatch();
    }

    return null;
  }


  public List<TagsSearchResult> getResults() {
    return results;
  }

  public String getOverAllSearchTerm() {
    return overAllSearchTerm;
  }

  public boolean hasEmptySearchTerm() {
    return hasEmptySearchTerm;
  }

  public void setHasEmptySearchTerm(boolean hasEmptySearchTerm) {
    this.hasEmptySearchTerm = hasEmptySearchTerm;
  }


  @Override
  public String toString() {
    return overAllSearchTerm + " has " + getRelevantMatchesCount() + " results";
  }

}
