// node_modules/.bin/browserify blob.js -o bundle.js

_ = require('lodash')
function main(argsArray) {
    const _args = argsArray || [1, 1, 1, 2];
    const uniqueArray = _.uniq(_args);
    return uniqueArray;
}
console.log(main());
