System.config({
  baseURL: "/assets",
  defaultJSExtensions: true,
  transpiler: "traceur",
  paths: {
    "github:*": "jspm_packages/github/*",
    "npm:*": "jspm_packages/npm/*"
  },

  map: {
    "angular": "github:angular/bower-angular@1.5.8",
    "angular-bootstrap": "github:angular-ui/bootstrap-bower@2.1.4",
    "angular-route": "github:angular/bower-angular-route@1.5.8",
    "css": "github:systemjs/plugin-css@0.1.27",
    "dashjs": "npm:dashjs@2.3.0",
    "text": "github:systemjs/plugin-text@0.0.9",
    "theseus-angular": "npm:theseus-angular@0.3.1",
    "traceur": "github:jmcriffey/bower-traceur@0.0.93",
    "traceur-runtime": "github:jmcriffey/bower-traceur-runtime@0.0.93",
    "github:angular/bower-angular-route@1.5.8": {
      "angular": "github:angular/bower-angular@1.5.8"
    },
    "github:jspm/nodelibs-assert@0.1.0": {
      "assert": "npm:assert@1.4.1"
    },
    "github:jspm/nodelibs-buffer@0.1.0": {
      "buffer": "npm:buffer@3.6.0"
    },
    "github:jspm/nodelibs-path@0.1.0": {
      "path-browserify": "npm:path-browserify@0.0.0"
    },
    "github:jspm/nodelibs-process@0.1.2": {
      "process": "npm:process@0.11.9"
    },
    "github:jspm/nodelibs-util@0.1.0": {
      "util": "npm:util@0.10.3"
    },
    "github:jspm/nodelibs-vm@0.1.0": {
      "vm-browserify": "npm:vm-browserify@0.0.4"
    },
    "npm:any-http-angular@0.1.0": {
      "angular": "github:angular/bower-angular@1.5.8"
    },
    "npm:any-promise-angular@0.1.1": {
      "angular": "github:angular/bower-angular@1.5.8"
    },
    "npm:assert@1.4.1": {
      "assert": "github:jspm/nodelibs-assert@0.1.0",
      "buffer": "github:jspm/nodelibs-buffer@0.1.0",
      "process": "github:jspm/nodelibs-process@0.1.2",
      "util": "npm:util@0.10.3"
    },
    "npm:buffer@3.6.0": {
      "base64-js": "npm:base64-js@0.0.8",
      "child_process": "github:jspm/nodelibs-child_process@0.1.0",
      "fs": "github:jspm/nodelibs-fs@0.1.2",
      "ieee754": "npm:ieee754@1.1.6",
      "isarray": "npm:isarray@1.0.0",
      "process": "github:jspm/nodelibs-process@0.1.2"
    },
    "npm:dashjs@2.3.0": {
      "buffer": "github:jspm/nodelibs-buffer@0.1.0",
      "codem-isoboxer": "npm:codem-isoboxer@0.2.2",
      "process": "github:jspm/nodelibs-process@0.1.2",
      "round10": "npm:round10@1.0.3"
    },
    "npm:inherits@2.0.1": {
      "util": "github:jspm/nodelibs-util@0.1.0"
    },
    "npm:path-browserify@0.0.0": {
      "process": "github:jspm/nodelibs-process@0.1.2"
    },
    "npm:process@0.11.9": {
      "assert": "github:jspm/nodelibs-assert@0.1.0",
      "fs": "github:jspm/nodelibs-fs@0.1.2",
      "vm": "github:jspm/nodelibs-vm@0.1.0"
    },
    "npm:theseus-angular@0.3.1": {
      "angular": "github:angular/bower-angular@1.5.8",
      "any-http-angular": "npm:any-http-angular@0.1.0",
      "any-promise-angular": "npm:any-promise-angular@0.1.1",
      "theseus": "npm:theseus@0.5.2"
    },
    "npm:theseus@0.5.2": {
      "uri-templates": "npm:uri-templates@0.1.9"
    },
    "npm:uri-templates@0.1.9": {
      "path": "github:jspm/nodelibs-path@0.1.0",
      "systemjs-json": "github:systemjs/plugin-json@0.1.2",
      "util": "github:jspm/nodelibs-util@0.1.0"
    },
    "npm:util@0.10.3": {
      "inherits": "npm:inherits@2.0.1",
      "process": "github:jspm/nodelibs-process@0.1.2"
    },
    "npm:vm-browserify@0.0.4": {
      "indexof": "npm:indexof@0.0.1"
    }
  }
});
