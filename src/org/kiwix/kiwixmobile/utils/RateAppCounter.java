/*
 * Copyright 2013  Elad Keyshawn <elad.keyshawn@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU  General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */

package org.kiwix.kiwixmobile.utils;

import android.content.SharedPreferences;
import javax.inject.Inject;

import static org.kiwix.kiwixmobile.constants.PreferenceTag.PREF_NO_THANKS_CLICKED;
import static org.kiwix.kiwixmobile.constants.PreferenceTag.PREF_RATE_COUNTER;

public class RateAppCounter {

  private SharedPreferences preferences;

  @Inject
  public RateAppCounter(SharedPreferences preferences) {
    this.preferences = preferences;
  }

  public boolean getNoThanksState() {
    return preferences.getBoolean(PREF_NO_THANKS_CLICKED, false);
  }

  public void setNoThanksState(boolean state) {
    SharedPreferences.Editor CounterEditor = preferences.edit();
    CounterEditor.putBoolean(PREF_NO_THANKS_CLICKED, state);
    CounterEditor.apply();
  }

  public int getCount() {
    return preferences.getInt(PREF_RATE_COUNTER, 0);
  }

  public void setCount(int count) {
    SharedPreferences.Editor CounterEditor = preferences.edit();
    CounterEditor.putInt(PREF_RATE_COUNTER, count);
    CounterEditor.apply();
  }
}
