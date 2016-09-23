import angular from 'angular';
import 'theseus-angular';

export const streamApi = angular.module('lv.services.api.stream', ['theseus']);

streamApi.factory('streamApi', ['$q', 'apiRoot', 'theseus.client', 'apiPoll', function ($q, apiRoot, client, apiPoll) {
    const root = client.resource(apiRoot);

    const get = (id) => root.follow('stream', {id: id}).get();

    const list = () => root.follow('streams').get();

    function create(newStreamRequest) {
        // TODO look up this value via wowza api?
        if (! newStreamRequest.wowzaApp) {
            newStreamRequest.wowzaApp = 'live'
        }

        function untilStreamActive(stream) {
            return stream.follow('healthcheck').get()
                .then(healthcheck => healthcheck.data.streamStatus === 'active' ? stream : $q.reject());
        }

        function untilStreamInTesting(stream) {
            return stream.follow('healthcheck').get()
                .then(healthcheck => angular.equals(healthcheck.data, {broadcastStatus: 'testing', streamStatus: 'active'}) ? stream : $q.reject());
        }

        return root.follow('streams')
            .post({data: newStreamRequest})
            .then(newStream => apiPoll(() => untilStreamActive(newStream)))
            .then(activeStream => activeStream.perform('monitor', {body: {data: {monitor: true}}}))
            .then(monStream => apiPoll(() => untilStreamInTesting(monStream)))
            .then(monitoredStream => monitoredStream.perform('start', {body: {data: {start: true}}}));
    }

    return {
        get,
        list,
        create
    };
}]);
