const fs = require("fs");
const path = require("path");

// Read version from gradle.properties at the repo root.
// The collector's run command executes from the repo root.
function getSdkVersionFromGradle(projectRoot) {
  try {
    const gradlePropsPath = path.join(projectRoot, "gradle.properties");
    if (fs.existsSync(gradlePropsPath)) {
      const content = fs.readFileSync(gradlePropsPath, "utf-8");
      const match = content.match(/^version=(.+)$/m);
      if (match) {
        return match[1].trim();
      }
    }
  } catch (err) {
    console.warn(`Warning: Could not read gradle.properties: ${err.message}`);
  }
  return null;
}

module.exports = { getSdkVersionFromGradle };
