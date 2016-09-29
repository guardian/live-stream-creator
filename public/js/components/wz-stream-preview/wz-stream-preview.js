import angular from 'angular';

import dashjs from 'dashjs';

import './wz-stream-preview.css!';
import template from './wz-stream-preview.html!text';

export const wzStreamPreview = angular.module('wz.preview', []);

wzStreamPreview.controller('wzStreamPreviewCtrl', ['$rootScope', '$scope', function ($rootScope, $scope) {
    const ctrl = this;

    const player = dashjs.MediaPlayer().create();
    player.getDebug().setLogToBrowserConsole(false);

    $scope.$watch(() => ctrl.stream, stream => {
        if (stream) {
            if (! player.isReady()) {
                player.initialize(document.querySelector('wz-stream-preview video'), stream.dashManifestUri, true);
            } else {
                player.attachSource(stream.dashManifestUri);
            }
        }
    });

    $rootScope.$on('stream-started', () => player.pause());
}]);

wzStreamPreview.directive('wzStreamPreview', [function () {
    return {
        restrict: 'E',
        controller: 'wzStreamPreviewCtrl',
        controllerAs: 'ctrl',
        bindToController: true,
        template: template,
        scope: {
            stream: '='
        }
    };
}]);
