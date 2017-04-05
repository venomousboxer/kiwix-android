package org.kiwix.kiwixmobile;

public interface ViewCallback {

  void showLoading();

  void hideLoading();

  void showError(String message);
}