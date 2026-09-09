const fs = require("fs");

function resolveVersionDir(componentDir) {
  if (!fs.existsSync(componentDir)) {
    console.error(`❌ Component dir not found: ${componentDir}`);
    return null;
  }
  const entries = fs
    .readdirSync(componentDir, { withFileTypes: true })
    .filter((e) => e.isDirectory());
  if (entries.length !== 1) {
    console.error(
      `❌ Expected exactly one version dir in ${componentDir}, found ${entries.length}`,
    );
    return null;
  }
  return entries[0].name;
}

module.exports = { resolveVersionDir };
