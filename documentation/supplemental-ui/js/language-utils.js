/**
 * Common language utilities
 */
(function(global) {
  // Shared configuration for both i18n and language switcher
  const Config = {
    DEBUG: false,
    DEFAULT_LANGUAGE: 'de',
    LANGUAGES: {
      en: 'English',
      de: 'Deutsch'
    }
  };

  /**
   * Get the current language from the DOM attribute, URL.
   * @returns {string} - The two-letter language code.
   */
  function getCurrentLanguage() {
    // Check if language was already set on the document
    const docLang = document.documentElement.getAttribute('data-current-language');
    if (docLang) return docLang;

    // Look for a language code in the URL path (e.g. /en/ or /de/)
    const langPathRegex = /\/([a-z]{2})\//;
    const match = window.location.pathname.match(langPathRegex);
    if (match && Config.LANGUAGES[match[1]]) return match[1];

    // Default language
    return Config.DEFAULT_LANGUAGE;
  }

  /**
   * Generate the URL for switching to a target language.
   * @param {string} targetLang - The target language code.
   * @param {string} currentLang - The current language code.
   * @param {string} currentUrl - The current URL.
   * @returns {string} - The URL updated with the target language.
   */
  function generateLanguageUrl(targetLang, currentLang, currentUrl) {
    const langRegex = /\/[a-z]{2}\//;
    if (langRegex.test(currentUrl)) {
      // Replace the current language segment with the target language
      return currentUrl.replace(`/${currentLang}/`, `/${targetLang}/`);
    } else {
      // If no language segment, insert the target language after the first slash
      const firstSlashIndex = currentUrl.indexOf('/', 1);
      if (firstSlashIndex !== -1) {
        return currentUrl.slice(0, firstSlashIndex + 1) +
               targetLang + '/' +
               currentUrl.slice(firstSlashIndex + 1);
      } else {
        // Simple URL like "/" – just add the language code
        return `/${targetLang}/`;
      }
    }
  }

  // Expose the utilities globally
  global.LanguageUtils = {
    Config,
    getCurrentLanguage,
    generateLanguageUrl
  };
})(window);