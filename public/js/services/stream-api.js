import angular from 'angular';
import 'theseus-angular';

export const streamApi = angular.module('lv.services.api.stream', ['theseus']);

streamApi.factory('streamApi', ['$q', 'apiRoot', 'theseus.client', 'apiPoll', function ($q, apiRoot, client, apiPoll) {
    const root = client.resource(apiRoot);

    const get = (id) => root.follow('stream', {id: id}).get();

    const list = () => root.follow('streams').get();

    const create = (newStreamRequest) => {
        // TODO look up this value via wowza api?
        if (! newStreamRequest.wowzaApp) {
            newStreamRequest.wowzaApp = 'live';
        }

        newStreamRequest.wowzaStream = newStreamRequest.wowza.name;
        newStreamRequest.wowzaApplicationInstance = newStreamRequest.wowza.applicationInstance;
        delete newStreamRequest.wowza;

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
            .post({data: newStreamRequest})
            .then(newStream => apiPoll(() => untilStreamActive(newStream)))
            .then(activeStream => activeStream.perform('monitor', {body: {data: {monitor: true}}}))
            .then(monStream => apiPoll(() => untilStreamInTesting(monStream)))
            .then(monitoredStream => monitoredStream.perform('start', {body: {data: {start: true}}}));
    };

    const stop = (stream) => {
        const untilStreamStopped = (maybeStoppedStream) => {
            return performHealthcheck(maybeStoppedStream)
                .then(healthcheck => healthcheck.data.broadcastStatus === 'complete');
        };

        return stream.perform('stop', {body: {data: {stop: true}}})
            .then(maybeStopped =>apiPoll(() => untilStreamStopped(maybeStopped)));
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
