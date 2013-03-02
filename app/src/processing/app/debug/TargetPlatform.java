/* -*- mode: jde; c-basic-offset: 2; indent-tabs-mode: nil -*- */
/*
 TargetPlatform - Represents a hardware platform
 Part of the Arduino project - http://www.arduino.cc/

 Copyright (c) 2009 David A. Mellis

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 $Id$
 */
package processing.app.debug;

import static processing.app.I18n._;
import static processing.app.I18n.format;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import processing.app.helpers.PreferencesMap;

public class TargetPlatform {

  private String name;
  private File folder;

  /**
   * Contains preferences for every defined board
   */
  private Map<String, TargetBoard> boards = new LinkedHashMap<String, TargetBoard>();

  /**
   * Contains preferences for every defined programmer
   */
  private Map<String, PreferencesMap> programmers = new LinkedHashMap<String, PreferencesMap>();

  /**
   * Contains preferences for platform
   */
  private PreferencesMap preferences = new PreferencesMap();

  private PreferencesMap customMenus = new PreferencesMap();

  public TargetPlatform(String _name, File _folder)
      throws TargetPlatformException {
    name = _name;
    folder = _folder;

    // If there is no boards.txt, this is not a valid 1.5 hardware folder
    File boardsFile = new File(folder, "boards.txt");
    if (!boardsFile.exists() || !boardsFile.canRead())
      throw new TargetPlatformException(
          format(_("Could not find boards.txt in {0}. Is it pre-1.5?"),
                 boardsFile.getAbsolutePath()));

    // Load boards
    try {
      PreferencesMap p = new PreferencesMap(boardsFile);
      Map<String, PreferencesMap> boardsPreferences = p.firstLevelMap();

      // Create custom menus using the key "menu" and its subkeys
      if (boardsPreferences.containsKey("menu")) {
        customMenus = new PreferencesMap(boardsPreferences.get("menu"));
        boardsPreferences.remove("menu");
      }

      // Create boards
      for (String id : boardsPreferences.keySet()) {
        PreferencesMap preferences = boardsPreferences.get(id);
        boards.put(id, new TargetBoard(id, preferences));
      }
    } catch (IOException e) {
      throw new TargetPlatformException(format(_("Error loading {0}"),
                                               boardsFile.getAbsolutePath()), e);
    }

    File platformsFile = new File(folder, "platform.txt");
    try {
      if (platformsFile.exists() && platformsFile.canRead()) {
        preferences.load(platformsFile);
      }
    } catch (IOException e) {
      throw new TargetPlatformException(
          format(_("Error loading {0}"), platformsFile.getAbsolutePath()), e);
    }

    File progFile = new File(folder, "programmers.txt");
    try {
      if (progFile.exists() && progFile.canRead()) {
        PreferencesMap prefs = new PreferencesMap();
        prefs.load(progFile);
        programmers = prefs.firstLevelMap();
      }
    } catch (IOException e) {
      throw new TargetPlatformException(format(_("Error loading {0}"), progFile
          .getAbsolutePath()), e);
    }
  }

  public String getName() {
    return name;
  }

  public File getFolder() {
    return folder;
  }

  public Map<String, TargetBoard> getBoards() {
    return boards;
  }

  public PreferencesMap getCustomMenus() {
    return customMenus;
  }

  public Map<String, PreferencesMap> getProgrammers() {
    return programmers;
  }

  public PreferencesMap getProgrammer(String programmer) {
    return getProgrammers().get(programmer);
  }

  public PreferencesMap getTool(String tool) {
    return getPreferences().subTree("tools").subTree(tool);
  }

  public PreferencesMap getPreferences() {
    return preferences;
  }

  public TargetBoard getBoard(String boardId) {
    return boards.get(boardId);
  }
}
