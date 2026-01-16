/**
 * Simple i18n handler for Antora UI with external translations.
 */
(function() {
  const { Config, getCurrentLanguage } = window.LanguageUtils;
  // Enable debug mode if needed
  Config.DEBUG = false;

  // Container for translations loaded from the external JSON file
  let translations = {};

  // Retrieve the translations URL from the script's data
  const currentScript = document.currentScript;
  const translationsUrl = currentScript ? currentScript.dataset.translationsUrl : '/config/translations.json';

  /**
   * Load translations from the external JSON file.
   * @param {Function} callback - Called after translations are loaded.
   */
  function loadTranslations(callback) {
      fetch(translationsUrl)
        .then(response => {
          if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
          return response.json();
        })
        .then(data => {
          translations = data;
          if (Config.DEBUG) console.log('Translations loaded:', translations);
          callback();
        })
        .catch(error => console.error('Error loading translations:', error));
  }

  /**
   * Initialize i18n: load translations, detect current language, and apply translations.
   */
  function initI18n() {
    loadTranslations(function() {
      const currentLang = getCurrentLanguage();
      if (Config.DEBUG) console.log('Current language:', currentLang);
      applyTranslations(currentLang);

      // Listen for language change events; page reload handles language updates.
      document.addEventListener('languageChanged', function(event) {
        if (Config.DEBUG) console.log('Language changed:', event.detail);
      });
    });
  }

  /**
   * Apply translations to all elements with translation keys.
   * @param {string} lang - The current language code.
   */
  function applyTranslations(lang) {
    // Translate elements with data-i18n attribute.
    document.querySelectorAll('[data-i18n]').forEach(function(element) {
      const key = element.getAttribute('data-i18n');
      const text = translateKey(key, lang);
      if (text) {
        element.textContent = text;
      }
    });

    // Translate elements with data-i18n-placeholder attribute.
    document.querySelectorAll('[data-i18n-placeholder]').forEach(function(element) {
      const key = element.getAttribute('data-i18n-placeholder');
      const text = translateKey(key, lang);
      if (text) {
        element.setAttribute('placeholder', text);
      }
    });
  }

  /**
   * Translate a given key for the specified language.
   * Falls back to the default language if needed.
   * @param {string} key - In the format "section.key".
   * @param {string} lang - The language code.
   * @returns {string|null} The translated string or null.
   */
  function translateKey(key, lang) {
    try {
      const [section, translationKey] = key.split('.');
      let text = translations[section]?.[lang]?.[translationKey] ||
                 translations[section]?.[Config.DEFAULT_LANGUAGE]?.[translationKey];
      if (text) {
        // Replace dynamic variables like {year}
        text = text.replace(/{year}/g, new Date().getFullYear());
        return text;
      }
    } catch (error) {
      console.error('Error translating key:', key, error);
    }
    return null;
  }

  // Expose a global i18n function for use by other modules.
  window.i18n = {
    translate: function(key, defaultText) {
      const lang = getCurrentLanguage();
      return translateKey(key, lang) || defaultText || key;
    }
  };

  // Initialize when the DOM is ready.
  document.addEventListener('DOMContentLoaded', initI18n);
})();