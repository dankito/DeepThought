package net.dankito.service.search.specific;


import net.dankito.deepthought.model.Tag;
import net.dankito.service.search.Search;
import net.dankito.service.search.util.CombinedLazyLoadingList;

import java.util.ArrayList;
import java.util.List;


public class TagsSearchResults {

  public final static TagsSearchResults EmptySearchResults = new TagsSearchResults(Search.EmptySearchTerm, new ArrayList<Tag>());


  protected String overAllSearchTerm;

  protected boolean hasEmptySearchTerm = false;

  protected List<TagsSearchResult> results = new ArrayList<>();

  protected List<Tag> allMatches = null;

  protected List<Tag> relevantMatchesSorted = new ArrayList<>();

  protected List<Tag> exactMatches = null;

  protected List<Tag> exactOrSingleMatchesNotOfLastResult = null;

  protected List<Tag> matchesButOfLastResult = null;


  public TagsSearchResults() {

  }

  public TagsSearchResults(String overAllSearchTerm) {
    this();
    this.overAllSearchTerm = overAllSearchTerm;
  }

  public TagsSearchResults(String overAllSearchTerm, List<Tag> relevantMatchesSorted) {
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

  public List<Tag> getRelevantMatchesSorted() {
    if(relevantMatchesSorted == null)
      return getAllMatches();
    return relevantMatchesSorted;
  }

  public void setRelevantMatchesSorted(List<Tag> relevantMatchesSorted) {
    this.relevantMatchesSorted = relevantMatchesSorted;

    // TODO: what was that good for?
    if(results.size() == 0)
      addSearchResult(new TagsSearchResult(getOverAllSearchTerm(), relevantMatchesSorted, null));
  }


  public List<Tag> getAllMatches() {
    if(allMatches == null)
      allMatches = determineAllMatches();

    return allMatches;
  }



  public List<Tag> getExactMatches() {
    if(exactMatches == null)
      exactMatches = determineExactMatches();

    return exactMatches;
  }

  protected List<Tag> determineExactMatches() {
    List<Tag> exactMatches = new ArrayList<>();

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


  protected List<Tag> getExactOrSingleMatchesNotOfLastResult() {
    if(exactOrSingleMatchesNotOfLastResult == null)
      exactOrSingleMatchesNotOfLastResult = determineExactOrSingleMatchesNotOfLastResult();
    return exactOrSingleMatchesNotOfLastResult;
  }

  protected List<Tag> determineExactOrSingleMatchesNotOfLastResult() {
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

  protected List<Tag> getMatchesNotOfLastResult() {
    if(matchesButOfLastResult == null)
      matchesButOfLastResult = determineMatchesNotOfLastResult();
    return matchesButOfLastResult;
  }

  protected List<Tag> determineMatchesNotOfLastResult() {
    List<Tag> nonLastResultNotExactOrSingleMatches = new ArrayList<>();

    for(int i = 0; i < results.size() - 1; i++) {
      TagsSearchResult result = results.get(i);
      if(result.hasExactMatch() == false && result.hasSingleMatch() == false)
        nonLastResultNotExactOrSingleMatches.addAll(result.getAllMatches());
    }

    return nonLastResultNotExactOrSingleMatches;
  }


  protected List<Tag> determineAllMatches() {
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
