const path = require("path");
const { getSdkVersionFromGradle } = require("./sdk-version");

module.exports.register = function () {
  this.on("contentAggregated", ({ playbook, contentAggregate }) => {
    const projectRoot = path.resolve(playbook.dir, "..");
    const sdkVersion =
      process.env.SDK_VERSION || getSdkVersionFromGradle(projectRoot);
    if (!sdkVersion) return;

    contentAggregate.forEach((componentVersion) => {
      componentVersion.version = sdkVersion;
    });
  });
};
