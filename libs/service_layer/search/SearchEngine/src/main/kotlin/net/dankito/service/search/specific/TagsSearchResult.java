package net.dankito.service.search.specific;


import net.dankito.deepthought.model.Tag;

import java.util.ArrayList;
import java.util.List;


public class TagsSearchResult {

  protected String searchTerm;

  protected boolean hasExactMatch;

  protected Tag exactMatch;

  protected Tag singleMatch = null;

  protected List<Tag> allMatches;


  public TagsSearchResult(String searchTerm, List<Tag> allMatches) {
    this.searchTerm = searchTerm;
    this.allMatches = allMatches;

    findExactMatch(searchTerm, allMatches);
  }

  public TagsSearchResult(String searchTerm, List<Tag> allMatches, Tag exactMatch) {
    this.searchTerm = searchTerm;
    this.allMatches = allMatches;
    this.hasExactMatch = exactMatch != null;
    this.exactMatch = exactMatch;
  }


  protected void findExactMatch(String searchTerm, List<Tag> allMatches) {
    for(Tag match : allMatches) {
      if(searchTerm.equals(match.getName().toLowerCase())) {
        this.hasExactMatch = true;
        this.exactMatch = match;
        break;
      }
    }
  }


  public String getSearchTerm() {
    return searchTerm;
  }

  public boolean hasExactMatch() {
    return hasExactMatch;
  }

  public Tag getExactMatch() {
    return exactMatch;
  }

  public boolean hasSingleMatch() {
    return getAllMatchesCount() == 1;
  }

  public Tag getSingleMatch() {
    if(hasSingleMatch()) {
      if(singleMatch == null) {
        if(allMatches instanceof List)
          singleMatch = ((List<Tag>)allMatches).get(0);
        else
          singleMatch = new ArrayList<Tag>(allMatches).get(0);
      }

      return singleMatch;
    }

    return null;
  }

  public int getAllMatchesCount() {
    return allMatches.size();
  }

  public List<Tag> getAllMatches() {
    return allMatches;
  }


  @Override
  public String toString() {
    return searchTerm + " has " + getAllMatchesCount() + " matches, hasExactMatch = " + hasExactMatch();
  }

}
