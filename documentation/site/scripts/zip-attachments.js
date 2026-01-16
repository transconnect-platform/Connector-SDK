#!/usr/bin/env node

const fs = require('fs');
const path = require('path');
const archiver = require('archiver');

const config = {
  languages: ['de', 'en'],
  component: 'dev-doc',
  version: '4.X.X',
  attachmentFolder: 'tutorial-example'
};

function createZip(language) {
  const attachmentPath = path.join(
    '../public',
    language,
    config.component,
    config.version,
    '_attachments',
    config.attachmentFolder
  );
  
  if (!fs.existsSync(attachmentPath)) {
    console.error(`❌ Attachment folder not found: ${attachmentPath}`);
    return Promise.resolve(false);
  }
  
  const zipFileName = `${config.attachmentFolder}.zip`;
  const outputDir = path.dirname(attachmentPath);
  const zipPath = path.join(outputDir, zipFileName);
  
  return new Promise((resolve) => {
    const output = fs.createWriteStream(zipPath);
    const archive = archiver('zip', {
      zlib: { level: 9 }
    });
    
    output.on('close', () => {
      console.log(`✅ Created: ${zipPath} (${archive.pointer()} bytes)`);

      // Remove source files after successful ZIP creation
      try {
        fs.rmSync(attachmentPath, { recursive: true, force: true });
        console.log(`✅ Removed source files: ${attachmentPath}`);
      } catch (err) {
        console.error(`⚠️ Failed to remove source files: ${err.message}`);
      }

      resolve(true);
    });
    
    archive.on('error', (err) => {
      console.error(`❌ Failed to create zip for ${language}: ${err.message}`);
      resolve(false);
    });
    
    archive.pipe(output);
    archive.directory(attachmentPath, config.attachmentFolder);
    archive.finalize();
  });
}

async function main() {
  if (!fs.existsSync('../public')) {
    console.error('❌ Build directory not found. Run the build process first.');
    process.exit(1);
  }
  
  console.log('Creating attachment zips...');
  
  let successCount = 0;
  
  for (const language of config.languages) {
    const success = await createZip(language);
    if (success) {
      successCount++;
    }
  }
  
  console.log(`\nCompleted: ${successCount}/${config.languages.length} zips created`);
  
  if (successCount === 0) {
    console.error('❌ No zips were created successfully');
    process.exit(1);
  }
}

if (require.main === module) {
  main().catch(console.error);
}