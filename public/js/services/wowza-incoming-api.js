import angular from 'angular';
import 'theseus-angular';

export const wowzaIncomingApi = angular.module('lv.services.api.wowza', ['theseus']);

wowzaIncomingApi.factory('wowzaIncomingApi', ['apiRoot', 'theseus.client', function (apiRoot, client) {
    const root = client.resource(apiRoot);

    const list = (id = 'live') => root.follow('incoming-wowza', {id: id}).get();

    return {
        list
    }
}]);
