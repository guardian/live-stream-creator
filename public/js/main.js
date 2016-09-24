import angular from 'angular';
import './async';
import './services/stream-api';
import './services/youtube-channel-api';
import './services/wowza-incoming-api';
import './lv-list/lv-list';
import './lv-create/lv-create';
import './lv-detail/lv-detail';

const app = angular.module('liveVideo', [
    'util.async',
    'lv.services.api.stream',
    'lv.services.api.youtube',
    'lv.services.api.wowza',
    'lv.list',
    'lv.create',
    'lv.detail'
]);

const config = {
    apiRoot: document.querySelector('link[rel="api-uri"]').getAttribute('href')
};

angular.forEach(config, (value, key) => app.constant(key, value));


app.directive('lvApp', [function () {
    return {
        restrict: 'E',
        template: '<lv-create></lv-create>'
    };
}]);

angular.element(document).ready(function() {
    angular.bootstrap(document, ['liveVideo']);
});
