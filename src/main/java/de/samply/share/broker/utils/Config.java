package de.samply.share.broker.utils;

import de.samply.share.common.utils.AbstractConfig;

/**
 * The configuration of this project.
 */
public class Config extends AbstractConfig {

  private static final String CONFIG_FILENAME = "samply.share.broker.conf";

  private static Config instance;

  private Config(String... fallbacks) {
    super(CONFIG_FILENAME, fallbacks);
  }

  /**
   * Create a new instance of Config if it not exists.
   * @param fallbacks fallback of directory paths
   * @return Config
   */
  public static Config getInstance(String... fallbacks) {
    if (instance == null) {
      instance = new Config(fallbacks);
    }
    return instance;
  }

}
