package net.dankito.service.search;


public interface SearchCompletedListener<T> {

  void completed(T results);

}
