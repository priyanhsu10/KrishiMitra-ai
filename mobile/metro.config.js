const { getDefaultConfig } = require('@expo/metro-config');

module.exports = (() => {
  const config = getDefaultConfig(__dirname);

  // Skip iOS for now to focus on Android
  config.skipLibraryValidation = true;

  return config;
})();