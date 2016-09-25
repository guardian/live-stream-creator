import angular from 'angular';
import 'angular-route';
import './util/async';
import './services/stream-api';
import './services/youtube-channel-api';
import './services/wowza-incoming-api';
import './lv-list/lv-list';
import './lv-create/lv-create';
import './lv-detail/lv-detail';

import listTemplate from './lv-list/lv-list.html!text';
import createTemplate from './lv-create/lv-create.html!text';
import detailTemplate from './lv-detail/lv-detail.html!text';

const config = {
    apiRoot: document.querySelector('link[rel="api-uri"]').getAttribute('href')
};

const app = angular.module('liveVideo', [
    'ngRoute',
    'util.async',
    'lv.services.api.stream',
    'lv.services.api.youtube',
    'lv.services.api.wowza',
    'lv.list',
    'lv.create',
    'lv.detail'
]);

angular.forEach(config, (value, key) => app.constant(key, value));

app.config(['$routeProvider', '$locationProvider', function ($routeProvider, $locationProvider) {
    $locationProvider.html5Mode(true);

    return $routeProvider
        .when('/', {
            template: listTemplate,
            controller: 'lvListCtrl',
            controllerAs: 'ctrl'
        })
        .when('/create', {
            template: createTemplate,
            controller: 'lvCreateCtrl',
            controllerAs: 'ctrl'
        })
        .when('/stream/:id', {
            template: detailTemplate,
            controller: 'lvDetailCtrl',
            controllerAs: 'ctrl'
        })
        .otherwise({
            redirectTo: '/'
        });
}]);

angular.element(document).ready(function() {
    angular.bootstrap(document, ['liveVideo']);
});
