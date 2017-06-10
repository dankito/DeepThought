package net.dankito.service.search.specific;


import net.dankito.deepthought.model.Entry;
import net.dankito.deepthought.model.Tag;
import net.dankito.service.search.util.CombinedLazyLoadingList;

import java.util.Collection;
import java.util.List;


public class FindAllEntriesHavingTheseTagsResult {

  protected Collection<Entry> entriesHavingFilteredTags;

  protected Collection<Tag> tagsOnEntriesContainingFilteredTags;
  protected List<Tag> tagsOnEntriesContainingFilteredTagsList = null;


  public FindAllEntriesHavingTheseTagsResult(Collection<Entry> entriesHavingFilteredTags, Collection<Tag> tagsOnEntriesContainingFilteredTags) {
    this.entriesHavingFilteredTags = entriesHavingFilteredTags;
    this.tagsOnEntriesContainingFilteredTags = tagsOnEntriesContainingFilteredTags;
  }

  public Collection<Entry> getEntriesHavingFilteredTags() {
    return entriesHavingFilteredTags;
  }

  public int getTagsOnEntriesContainingFilteredTagsCount() {
    return tagsOnEntriesContainingFilteredTags.size();
  }

  public Collection<Tag> getTagsOnEntriesContainingFilteredTags() {
    return tagsOnEntriesContainingFilteredTags;
  }

  public Tag getTagsOnEntriesContainingFilteredTagsAt(int index) {
    if(index < 0 || index >= getTagsOnEntriesContainingFilteredTagsCount())
      return null;

    if(tagsOnEntriesContainingFilteredTagsList == null) {
      if(tagsOnEntriesContainingFilteredTags instanceof List)
        tagsOnEntriesContainingFilteredTagsList = (List<Tag>)tagsOnEntriesContainingFilteredTags;
      else
        tagsOnEntriesContainingFilteredTagsList = new CombinedLazyLoadingList<Tag>(tagsOnEntriesContainingFilteredTags);
    }

    return tagsOnEntriesContainingFilteredTagsList.get(index);
  }
}
