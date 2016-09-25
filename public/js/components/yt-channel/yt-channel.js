import angular from 'angular';

import './yt-channel.css!';
import template from './yt-channel.html!text';

export const ytChannel = angular.module('yt.channel', []);

ytChannel.controller('ytChannelCtrl', [function () {}]);

ytChannel.directive('ytChannel', [function () {
    return {
        restrict: 'E',
        controller: 'ytChannelCtrl',
        controllerAs: 'ctrl',
        bindToController: true,
        template: template,
        scope: {
            channel: '='
        }
    };
}]);
