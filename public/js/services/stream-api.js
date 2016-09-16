import angular from 'angular';
import 'theseus-angular';

export const streamApi = angular.module('lv.services.api.stream', ['theseus']);

streamApi.factory('streamApi', ['$q', 'apiRoot', 'theseus.client', 'apiPoll', function ($q, apiRoot, client, apiPoll) {
    const root = client.resource(apiRoot);

    function list() {
        return root.follow('streams').get();
    }

    function create(newStreamRequest) {
        // TODO look up this value via wowza api?
        if (! newStreamRequest.wowzaApp) {
            newStreamRequest.wowzaApp = 'live'
        }

        function untilStreamActive (stream) {
            return root.follow('stream-health', {id: stream.data.id}).get().then((resp) => {
                console.log(resp);
               if (resp.data.status === 'active') {
                   return resp.data.status;
               } else {
                   $q.reject();
               }
            });
        }

        return root.follow('streams')
            .post({data: newStreamRequest})
            .then((newStream) => {
                apiPoll(() => untilStreamActive(newStream.data))
            }).then((health) => {
                console.log(health);
                debugger;
            });
    }

    return {
        root,
        list,
        create
    };
}]);
