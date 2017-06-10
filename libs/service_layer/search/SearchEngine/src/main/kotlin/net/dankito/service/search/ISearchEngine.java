package net.dankito.service.search;


import net.dankito.deepthought.model.Entry;
import net.dankito.deepthought.model.Reference;
import net.dankito.deepthought.model.Tag;
import net.dankito.service.search.specific.EntriesSearch;
import net.dankito.service.search.specific.FilesSearch;
import net.dankito.service.search.specific.FindAllEntriesHavingTheseTagsResult;
import net.dankito.service.search.specific.ReferenceSearch;
import net.dankito.service.search.specific.TagsSearch;

import java.util.Collection;


public interface ISearchEngine {

  void getEntriesForTagAsync(Tag tag, final SearchCompletedListener<Collection<Entry>> listener);

  void searchTags(TagsSearch search);

  void findAllEntriesHavingTheseTags(Collection<Tag> tagsToFilterFor, SearchCompletedListener<FindAllEntriesHavingTheseTagsResult> listener);
  void findAllEntriesHavingTheseTags(Collection<Tag> tagsToFilterFor, String searchTerm, SearchCompletedListener<FindAllEntriesHavingTheseTagsResult> listener);

  void searchEntries(EntriesSearch search);

  void searchReferenceBases(ReferenceSearch search);
  void searchForReferenceOfDate(String optionalSeriesTitleTitle, Search<Reference> search);

  void searchFiles(FilesSearch search);


  void close();

}
