var exec = require('cordova/exec');
function MLE() {
    var me = this;
}
MLE.prototype.check = function(callback){
    exec(function(result) {
        callback(result);        
    }, 
    function(error) {
        console.log(error);
    },
    "MLEnabled", "check", []);
};
MLE.prototype.toString = function(){
    return "CLE ready for action";
};
module.exports = new MLE();
