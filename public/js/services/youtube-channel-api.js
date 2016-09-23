import angular from 'angular';
import 'theseus-angular';

export const youtubeChannelApi = angular.module('lv.services.api.youtube', ['theseus']);

youtubeChannelApi.factory('youtubeChannelApi', ['apiRoot', 'theseus.client', function (apiRoot, client) {
    const root = client.resource(apiRoot);

    const list = () => root.follow('outgoing-youtube').get();

    return {
        list
    }
}]);
