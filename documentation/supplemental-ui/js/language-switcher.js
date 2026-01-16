/**
 * Language switcher for Antora documentation.
 */
(function () {
  const { Config, getCurrentLanguage, generateLanguageUrl } = window.LanguageUtils;
  // Optionally enable debug mode
  Config.DEBUG = false;

  /**
   * Initialize the language switcher by setting the current language
   * and building the dropdown menu.
   */
  function initLanguageSwitcher() {
    const currentLangText = document.getElementById('current-lang-text');
    const languageDropdown = document.getElementById('language-dropdown');

    if (!currentLangText || !languageDropdown) {
      if (Config.DEBUG) console.log('Language switcher elements not found in the DOM');
      return;
    }

    const currentUrl = window.location.pathname;
    if (Config.DEBUG) console.log('Current URL:', currentUrl);

    const currentLang = getCurrentLanguage();
    if (Config.DEBUG) console.log('Detected current language:', currentLang);

    // Store the current language on the document for global access
    document.documentElement.setAttribute('data-current-language', currentLang);

    // Update the displayed current language
    currentLangText.textContent = Config.LANGUAGES[currentLang] || currentLang;

    // Build the dropdown for switching languages
    buildLanguageDropdown(languageDropdown, currentLang, currentUrl);
  }

  /**
   * Build the language dropdown menu.
   * @param {HTMLElement} dropdownEl - The dropdown container.
   * @param {string} currentLang - The current language code.
   * @param {string} currentUrl - The current URL.
   */
  function buildLanguageDropdown(dropdownEl, currentLang, currentUrl) {
    // Clear any existing items
    dropdownEl.innerHTML = '';

    // Loop through all supported languages
    for (const langCode in Config.LANGUAGES) {
      if (langCode === currentLang) continue; // Skip the active language

      const langLink = document.createElement('a');
      langLink.className = 'navbar-item';
      langLink.textContent = Config.LANGUAGES[langCode];

      // Generate the URL for the target language
      const targetUrl = generateLanguageUrl(langCode, currentLang, currentUrl);
      if (Config.DEBUG) console.log(`Generated URL for ${langCode}:`, targetUrl);

      langLink.href = targetUrl;
      langLink.dataset.langCode = langCode;

      // Add an event listener to trigger the language change event
      langLink.addEventListener('click', function() {
        triggerLanguageChangeEvent(currentLang, this.dataset.langCode, targetUrl);
      });

      dropdownEl.appendChild(langLink);
    }
  }

  /**
   * Trigger a custom languageChanged event.
   * @param {string} fromLang - The current language.
   * @param {string} toLang - The target language.
   * @param {string} targetUrl - The URL for the target language.
   */
  function triggerLanguageChangeEvent(fromLang, toLang, targetUrl) {
    const changeEvent = new CustomEvent('languageChanged', {
      detail: { from: fromLang, to: toLang, url: targetUrl }
    });
    document.dispatchEvent(changeEvent);
  }

  // Listen for languageChanged events (if needed for further actions)
  document.addEventListener('languageChanged', function(event) {
    if (Config.DEBUG) console.log('Language changed event detected:', event.detail);
  });

  // Initialize when the DOM is fully loaded
  document.addEventListener('DOMContentLoaded', initLanguageSwitcher);
})();