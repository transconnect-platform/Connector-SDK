#!/usr/bin/env node

const { execSync } = require('child_process');
const path = require('path');

// Determine the correct gradle wrapper command based on platform
const isWindows = process.platform === 'win32';
const gradleCmd = isWindows ? 'gradlew.bat' : './gradlew';

// Change to parent directory and run gradle task
const projectRoot = path.join(__dirname, '..', '..');
const command = `${gradleCmd} :api:copyJavadocToAntora`;

console.log(`Running: ${command}`);
console.log(`In directory: ${projectRoot}`);

try {
  execSync(command, {
    cwd: projectRoot,
    stdio: 'inherit'
  });
  console.log('Prebuild completed successfully');
} catch (error) {
  console.error('Prebuild failed:', error.message);
  process.exit(1);
}
