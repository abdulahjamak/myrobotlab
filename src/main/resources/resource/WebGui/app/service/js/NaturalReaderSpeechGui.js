angular.module('mrlapp.service.NaturalReaderSpeechGui', []).controller('NaturalReaderSpeechGuiCtrl', ['$scope', '$log', 'mrl', '$uibModal', function($scope, $log, mrl, $uibModal) {
    $log.info('NaturalReaderSpeechGuiCtrl');
    var _self = this;
    var msg = this.msg;

    this.updateState = function(service) {
		$scope.service = service;
	};

    _self.updateState($scope.service);

    // console.log('mary', $scope.service);

    this.onMsg = function(inMsg) {
        switch (inMsg.method) {
        case 'onState':
            _self.updateState(inMsg.data[0]);
            $scope.$apply();
            break;
        default:
            $log.error("ERROR - unhandled method " + $scope.name + " " + inMsg.method);
            break;
        }
    }
    ;

    // I suspect speak is not "setup" like other functions and is not accessable like others in the
    // theml e.g. msg.speak - so got to figure that out or temporarily create a $scope.speak kludge
    $scope.speak = function(text){
        console.log($scope.service.voice.name);
        msg.send("speak", text);
    };

    $scope.setVoice  = function(text){
        console.log($scope.service.voice.name);
        msg.send("setVoice", text.name);
        // msg.send("broadcastState");
    };

    msg.subscribe(this);
}
]);
