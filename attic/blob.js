// node_modules/.bin/browserify blob.js -o ../tokamak-core/src/test/resources/com/wrmsr/tokamak/blob.js.txt
// node_modules/.bin/browserify blob.js -o bundle.js

_ = require('lodash');
barf = 1;
main = function(argsArray) {
    const _args = argsArray || [1, 1, 1, 2];
    const uniqueArray = _.uniq(_args);
    return uniqueArray;
};
module.exports.main = main;
