import angular from 'angular';

export const async = angular.module('util.async', []);

/**
 * Return a Promise that is resolved with no value after `duration`.
 */
async.factory('delay', ['$q', '$timeout', function($q, $timeout) {
    return (duration) => {
        const defer = $q.defer();
        $timeout(defer.resolve, duration);
        return defer.promise;
    };
}]);


async.factory('race', ['$q', function($q) {
    return (promises) => {
        const first = $q.defer();
        promises.forEach(promise => promise.then(first.resolve, first.reject));
        return first.promise;
    };
}]);

async.factory('poll', ['$q', 'delay', 'race', function($q, delay, race) {
    return (func, pollEvery, maxWait) => {
        const timeout = delay(maxWait).then(() => $q.reject(new Error('timeout')));

        // Returns the result of promise or a rejected timeout
        // promise, whichever happens first
        const withTimeout = (promise) => race([promise, timeout]);

        const pollRecursive = () => {
            return func().catch(() => withTimeout(delay(pollEvery)).then(pollRecursive));
        };

        return withTimeout(pollRecursive());
    };
}]);

// Polling with sensible defaults for API polling
async.factory('apiPoll', ['poll', function(poll) {
    const pollFrequency = 1000; // ms
    const pollTimeout   = 50 * 1000; // ms
    return func => poll(func, pollFrequency, pollTimeout);
}]);
