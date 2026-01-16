#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

const sourceFile = path.join(__dirname, '..', 'site', 'index.html');
const destDir = path.join(__dirname, '..', '..', 'public');
const destFile = path.join(destDir, 'index.html');

console.log(`Copying ${sourceFile} to ${destFile}`);

try {
  // Ensure destination directory exists
  if (!fs.existsSync(destDir)) {
    fs.mkdirSync(destDir, { recursive: true });
  }

  // Copy the file
  fs.copyFileSync(sourceFile, destFile);
  console.log('Copy completed successfully');
} catch (error) {
  console.error('Copy failed:', error.message);
  process.exit(1);
}
