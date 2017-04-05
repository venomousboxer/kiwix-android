package org.kiwix.kiwixmobile;

public interface Presenter {

  void resume();

  void pause();

  void destroy();

  void setCallback(ViewCallback callback);
}