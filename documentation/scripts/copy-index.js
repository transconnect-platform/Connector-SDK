#!/usr/bin/env node

const fs = require("fs");
const path = require("path");

const sourceFile = path.join(__dirname, "..", "site", "index.html");
const destDir = path.join(__dirname, "..", "..", "public");
const destFile = path.join(destDir, "index.html");

const { resolveVersionDir } = require("./resolve-version-dir");

console.log(`Copying ${sourceFile} to ${destFile}`);

try {
  // Ensure destination directory exists
  if (!fs.existsSync(destDir)) {
    fs.mkdirSync(destDir, { recursive: true });
  }

  // Point the redirects at the version dirs that were actually built
  const enVersionDir = resolveVersionDir(path.join(destDir, "en", "sdk-doc"));
  const deVersionDir = resolveVersionDir(path.join(destDir, "de", "sdk-doc"));
  if (!enVersionDir || !deVersionDir) process.exit(1);

  let html = fs.readFileSync(sourceFile, "utf-8");
  html = html.replace(/en\/sdk-doc\/[^/"]+/g, `en/sdk-doc/${enVersionDir}`);
  html = html.replace(/de\/sdk-doc\/[^/"]+/g, `de/sdk-doc/${deVersionDir}`);
  fs.writeFileSync(destFile, html);
  console.log("Copy completed successfully");
} catch (error) {
  console.error("Copy failed:", error.message);
  process.exit(1);
}
