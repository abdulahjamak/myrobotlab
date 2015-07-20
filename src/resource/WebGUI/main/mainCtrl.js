/* FIXME - remove combine if necessary with svcCtrl.js
    One (only one) "workspace" controller needs to process the panels[]
*/    
angular.module('mrlapp.main.mainCtrl', ['mrlapp.mrl'])
.controller('mainCtrl', ['$log', '$scope', '$document', 'mrl', 'serviceSvc', 
    function($log, $scope, $document, mrl, serviceSvc) {
        $log.info('mainCtrl');
        $scope.services = mrl.getRegistry();
    }]);
