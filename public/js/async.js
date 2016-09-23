import angular from 'angular';

export const async = angular.module('util.async', []);

/**
 * Return a Promise that is resolved with no value after `duration`.
 */
async.factory('delay', ['$q', '$timeout', function($q, $timeout) {
    function delay(duration) {
        const defer = $q.defer();
        $timeout(defer.resolve, duration);
        return defer.promise;
    }

    return delay;
}]);


async.factory('race', ['$q', function($q) {
    function race(promises) {
        var first = $q.defer();
        promises.forEach(promise => {
            promise.then(first.resolve, first.reject);
        });
        return first.promise;
    }

    return race;
}]);

async.factory('poll', ['$q', 'delay', 'race', function($q, delay, race) {
    function poll(func, pollEvery, maxWait) {
        var timeout = delay(maxWait).then(() => $q.reject(new Error('timeout')));

        // Returns the result of promise or a rejected timeout
        // promise, whichever happens first
        function withTimeout(promise) {
            return race([promise, timeout]);
        }

        function pollRecursive() {
            return func().catch(() => {
                return withTimeout(delay(pollEvery)).then(pollRecursive);
            });
        }

        return withTimeout(pollRecursive());
    }

    return poll;
}]);

// Polling with sensible defaults for API polling
async.factory('apiPoll', ['poll', function(poll) {
    const pollFrequency = 1000; // ms
    const pollTimeout   = 50 * 1000; // ms
    return func => poll(func, pollFrequency, pollTimeout);
}]);
