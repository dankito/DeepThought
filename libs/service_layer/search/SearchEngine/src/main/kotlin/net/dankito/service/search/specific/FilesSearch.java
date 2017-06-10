package net.dankito.service.search.specific;


import net.dankito.deepthought.model.FileLink;
import net.dankito.service.search.Search;
import net.dankito.service.search.SearchCompletedListener;

import java.util.Collection;


public class FilesSearch extends Search<FileLink> {


  protected boolean searchFileName;

  protected boolean searchFileUri;

  protected boolean searchFileDescription;

  protected boolean inHtmlEmbeddableFilesOnly = false;


  public FilesSearch(String searchTerm, SearchCompletedListener<Collection<FileLink>> completedListener) {
    this(searchTerm, true, true, true, completedListener);
  }

  public FilesSearch(String searchTerm, boolean searchFileName, boolean searchFileUri, boolean searchFileDescription, SearchCompletedListener<Collection<FileLink>> completedListener) {
    super(searchTerm, completedListener);
    this.searchFileName = searchFileName;
    this.searchFileUri = searchFileUri;
    this.searchFileDescription = searchFileDescription;
  }


  public boolean searchFileName() {
    return searchFileName;
  }

  public boolean searchFileUri() {
    return searchFileUri;
  }

  public boolean searchFileDescription() {
    return searchFileDescription;
  }

  public boolean inHtmlEmbeddableFilesOnly() {
    return inHtmlEmbeddableFilesOnly;
  }

  public void setInHtmlEmbeddableFilesOnly(boolean inHtmlEmbeddableFilesOnly) {
    this.inHtmlEmbeddableFilesOnly = inHtmlEmbeddableFilesOnly;
  }

}
