import angular from 'angular';

import './lv-detail.css!';
import template from './lv-detail.html!text';

export const lvDetail = angular.module('lv.detail', []);

lvDetail.controller('lvDetailController', ['$sce', 'streamApi', function ($sce, streamApi) {
    const ctrl = this;

    streamApi.get('quDGIMb4bc95CT90cXcPMA1474657450661163')
        .then(stream => {
            ctrl.stream = stream;
            ctrl.embedSrc = $sce.trustAsResourceUrl(`https://www.youtube.com/embed/${ctrl.stream.data.videoId}`)
        });
}]);

lvDetail.directive('lvDetail', [function () {
    return {
        restrict: 'E',
        controller: 'lvDetailController',
        controllerAs: 'ctrl',
        bindToController: true,
        template: template
    }
}]);
