/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot;

import java.util.ArrayList;

/**
 * Represents the set of user profiles installed on this machine.
 * User profiles are stored in a JSON configuration file with other configuration files.
 */
public class UserProfiles {

  private static final String kProfilesFilename = "profiles";

  /**
   * Constructs an empty set of UserProfiles.
   */
  public UserProfiles() {
    this.default_ = "";
    this.profiles_ = new ArrayList<UserProfile>();
  }

  /**
   * Loads the user profiles for the current context.
   * Returns null if the user profiles could not be loaded.
   *
   * @return The set of user profiles or null.
   */
  public static UserProfiles load() {
    return ConfigFiles.Instance.load(kProfilesFilename, UserProfiles.class);
  }

  /**
   * Adds a user profile if a profile with the same name does not already exist.
   * Returns true if the profile has been added.
   *
   * To make the addition permanent, the updated set of profiles must be saved via the
   * {@link #save()} method.
   *
   * @param profile The user profile to add.
   * @return True if the profile has been added.
   */
  public boolean addProfile(UserProfile profile) {
    if (findProfile(profile.getName()) != null) {
      return false;
    }
    profiles_.add(profile);
    return true;
  }

  /**
   * Returns the default profile. If no default profile is explicitly specified, returns the first
   * profile. If no profiles exist or the explicitly specified default profile does not exist
   * returns null.
   *
   * @return The default user profile or null.
   */
  public UserProfile defaultProfile() {
    if (default_.length() > 0) {
      return findProfile(default_);
    } else if (profiles_.size() > 0) {
      return profiles_.get(0);
    } else {
      return null;
    }
  }

  /**
   * Deletes the profile with the specified name. If the profile does not exist, results in a
   * no-op.
   *
   * To make the deletion permanent, the updated set of profiles must be saved via the
   * {@link #save()} method.
   *
   * @param name The name of the user profile to delete.
   */
  public void deleteProfile(String name) {
    UserProfile profile = findProfile(name);
    if (profile == null) {
      return;
    }
    profiles_.remove(profile);
  }

  /**
   * Returns the user profile with the specified name or null if no such profile exists.
   *
   * @param name The name of the user profile.
   * @return The user profile with the specified name or null.
   */
  public UserProfile findProfile(String name) {
    for (UserProfile profile : profiles_) {
      if (profile.getName().equals(name)) {
        return profile;
      }
    }
    return null;
  }

  /**
   * Returns the name of the default user profile or empty string if no default has been specified.
   *
   * @return Name of default user profile.
   */
  public String getDefault() {
    return default_;
  }

  /**
   * Returns all user profiles. A profile defines user log in information. The user can pick one
   * of the profiles to log in.
   *
   * @return All user profiles.
   */
  public ArrayList<UserProfile> getProfiles() {
    return profiles_;
  }

  /**
   * Saves the set of user profiles by updating the file from which the user profiles were loaded.
   *
   * @return True if the user profiles were successfully saved.
   */
  public boolean save() {
    return ConfigFiles.Instance.save(kProfilesFilename, this);
  }

  /**
   * Sets the name of the default user profile. The name must be the name of an existing profile.
   *
   * @param default_profile_name The name of the default profile.
   */
  public void setDefault(String default_profile_name) {
    this.default_ = default_profile_name;
  }

  /**
   * Updates the user profiles. A profile defines user log in information. The user can pick one
   * of the profiles to log in.
   *
   * @param profiles The updated set of user profiles to replace the current set of profiles.
   */
  public void setProfiles(ArrayList<UserProfile> profiles) {
    this.profiles_ = profiles;
  }

  /**
   * Returns the number of user profiles.
   *
   * @return The number of user profiles.
   */
  public int size() {
    return profiles_.size();
  }

  private String default_;  // The name of the default profile.
  private ArrayList<UserProfile> profiles_;  // List of user profiles.
}
