import angular from 'angular';
import 'theseus-angular';

export const streamApi = angular.module('lv.services.api.stream', ['theseus']);

streamApi.factory('streamApi', ['$rootScope', '$q', 'apiRoot', 'theseus.client', 'apiPoll', function ($rootScope, $q, apiRoot, client, apiPoll) {
    const root = client.resource(apiRoot);

    const get = (id) => root.follow('stream', {id: id}).get();

    const list = () => root.follow('streams').get();

    const create = (streamRequest) => {
        const streamRequestCopy = angular.copy(streamRequest);

        // TODO look up this value via wowza api?
        if (! streamRequestCopy.wowzaApp) {
            streamRequestCopy.wowzaApp = 'live';
        }

        streamRequestCopy.wowzaStream = streamRequestCopy.wowza.name;
        streamRequestCopy.wowzaApplicationInstance = streamRequestCopy.wowza.applicationInstance;
        delete streamRequestCopy.wowza;

        const untilStreamActive = (stream) => {
            return performHealthcheck(stream)
                .then(healthcheck => healthcheck.data.streamStatus === 'active' ? stream : $q.reject());
        };

        const untilStreamInTesting = (stream) => {
            const testingHealthcheck = { broadcastStatus: 'testing', streamStatus: 'active'};

            return performHealthcheck(stream)
                .then(healthcheck => angular.equals(healthcheck.data, testingHealthcheck) ? stream : $q.reject());
        };

        return root.follow('streams')
            .post({data: streamRequestCopy})
            .then(newStream => apiPoll(() => untilStreamActive(newStream)))
            .then(s => {
                $rootScope.$emit('stream-active', s.data);
                return s;
            })
            .then(activeStream => activeStream.perform('monitor', {body: {data: {monitor: true}}}))
            .then(s => {
                $rootScope.$emit('stream-monitored', s.data);
                return s;
            })
            .then(monStream => apiPoll(() => untilStreamInTesting(monStream)))
            .then(s => {
                $rootScope.$emit('stream-in-testing', s.data);
                return s;
            })
            .then(monitoredStream => monitoredStream.perform('start', {body: {data: {start: true}}}))
            .then(s => {
                $rootScope.$emit('stream-started', s.data);
                return s;
            });
    };

    const stop = (stream) => {
        const untilStreamStopped = (maybeStoppedStream) => {
            return performHealthcheck(maybeStoppedStream)
                .then(healthcheck => healthcheck.data.broadcastStatus === 'complete');
        };

        return stream.perform('stop', {body: {data: {stop: true}}})
            .then(maybeStopped => apiPoll(() => untilStreamStopped(maybeStopped)))
            .then(s => {
                $rootScope.$emit('stream-stopped', s.data);
                return s;
            });
    };

    const performHealthcheck = (stream) => stream.follow('healthcheck').get();

    return {
        get,
        list,
        create,
        stop,
        performHealthcheck
    };
}]);
