_ = require('lodash')
function main(argsArray) {
    const _args = argsArray || [1, 1, 1, 2];
    const uniqueArray = _.uniq(_args);
    return uniqueArray;
}
main();
