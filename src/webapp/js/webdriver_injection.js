// send the AJAX responses to WebDriver
(function(img, callback) {
	var $ = jQuery;
	// wait for images to be fully-loaded in the DOM
	imagesLoaded('body', function() {
		// override the global AJAX callbacks
		CALLBACKS.success = function(data, textStatus, jqXHR) {
			if (data.hasOwnProperty('files')) {
				callback(JSON.stringify(data.files));
			}
			callback(['Expected to find "files" array ('+textStatus+')']);
		};		
		CALLBACKS.error = function(jqXHR, textStatus, errorThrown) {
			callback(['Error ('+textStatus+') : ' + errorThrown]);
		};
		// submit image via AJAX
		$(img).click();
	});
})(arguments[0], arguments[arguments.length - 1]);