import angular from 'angular';

import './yt-channel.css!';
import template from './yt-channel.html!text';

export const ytChannel = angular.module('yt.channel', []);

ytChannel.directive('ytChannel', [function () {
    return {
        restrict: 'E',
        template: template,
        scope: {
            channel: '='
        }
    };
}]);
