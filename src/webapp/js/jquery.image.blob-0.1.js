/**
 * Image Blob v0.1.  
 * 		A jQuery plugin for uploading embedded IMG elements, thus eliminating 
 * 		the need to download locally and insert into a form field.  This
 * 		implementation copies the image data and converts it into a Blob before
 * 		uploading to the server.
 * 
 * TODO
 * 	- test with spaces in the non-dataUri img.src path (unescape()?)
 * 		- see: http://stackoverflow.com/questions/18287213/displaying-image-name-with-html-decimal-code-and-url-encoded
 * 
 * @author Joe Nasca
 * @see		http://stackoverflow.com/a/12470362/159570
 */
(function($, window, document, undefined) {
	
	/**
	 * Plugin initializer.
	 */
	$.fn.imageBlob = function() {

		/**
		 * Get a blob of the first image in the set of matched images.
		 * 
		 * Usage:
		 * 		var blob = $('img').imageBlob().blob();
		 * 
		 * @returns 	A blob of the image, or null if first matched element
		 * 				is not an IMG.
		 */
		this.blob = function() {
			var img = getFirstMatchedDomImage(this);
			if (!img) return null;
			return getImageBlob(img);
		};
		
		/**
		 * Get a blob of the first image in the set of matched images and perform
		 * a jQuery AJAX request with it.  The "name" attribute of the IMG is used
		 * as the request parameter name, or a default name is used.
		 * 
		 * Usage:
		 * 		$('img').imageBlob().ajax('/upload', {
		 * 			complete: function(jqXHR, textStatus) { alert(textStatus); } 
		 * 		});
		 * 
		 * @param url		[optional] The destination URL
		 * @param settings	[optional] AJAX settings object (overrides the plugin defaults)
		 * @returns		A jqXHR object, or null if first matched element
		 * 					is not an IMG.
		 */
		this.ajax = function(url, settings) {
			// convert to blob 
			var blob = this.blob();
			if (!blob) return null;
			
			// check optional params
			if (typeof url == 'object') {
				settings = url;
				url = undefined;
			}
			settings = settings || {};
			var ajaxSettings = $.extend({}, $.fn.imageBlob.ajaxSettings, settings);
			
			// append the blob to the FormData
			var filename = getImageName(this);
			ajaxSettings.data = $.fn.imageBlob.formData();
			ajaxSettings.data.append(filename, blob, filename);
			
			// perform the AJAX request
			if (typeof url == 'string') {
				return $.ajax(url, ajaxSettings);
			}
			return $.ajax(ajaxSettings);
		}
		
		// expose the public methods
		return this;
	};
	
	/////////////////////////////////////
	// public members (can be overridden)
	/////////////////////////////////////

	// Default AJAX settings
	$.fn.imageBlob.ajaxSettings = $.extend({}, $.ajaxSettings, {
		cache: false,
		processData: false, // necessary when submitting a FormData object
		contentType: false, // will ensure the correct content-type and multipart boundaries
		type: 'POST'
	});
	
	// Generates a FormData for the blob and other request parameters
	$.fn.imageBlob.formData = function() {
		return new FormData();
	};
	
	// Default image name (used when "name" attribute is missing).
	$.fn.imageBlob.defaultImageName = 'IMG_Upload';

	/////////////////////////////////////
	// private members
	/////////////////////////////////////
	
	var DATA_URI_REGEXP = /data:(image\/[^;]+);base64,((?:.|\r|\n)*)/;
	var JPEG_REGEXP = /.*\.jpe?g/g;
	
	function getFirstMatchedDomImage($img) {
		if ($img.length == 0 || 'IMG' != ($img.prop('tagName'))) {
			return null;
		}
		return $img.get(0);
	}
	
	function getImageName($img) {
		var name = $img.attr('name');
		if (typeof name == 'undefined') {
			name = $.fn.imageBlob.defaultImageName;
		}
		return name;
	}
	
	function getImageBlob(img) {
	    var matches = parseDataUri(img);	// [src, mimeType, dataUri]
	    return createBlob(matches[1], matches[2]);
	}

	function parseDataUri(img) {
		var src = $(img).attr('src');
		var matches = src.match(DATA_URI_REGEXP);
	    if (matches == null) {
	    	var mimeType;
	    	if (src.match(JPEG_REGEXP) != null) {
				mimeType = 'image/jpeg';
			}
			else {
				mimeType = 'image/png';
			}
	    	var canvas = document.createElement('canvas');
	    	var ctx = canvas.getContext('2d');
	    	canvas.width = img.width;
	    	canvas.height = img.height;
	    	ctx.drawImage(img, 0, 0);
	    	src = canvas.toDataURL(mimeType);
	    	matches = src.match(DATA_URI_REGEXP);
	    }
	    return matches;
	}
	
	function createBlob(mimeType, dataUri) {
		// atob() can't handle whitespace
		dataUri = dataUri.replace(/\s/g, '');
    	var base64 = atob(dataUri);
        var charCodes = [];
        for(var i = 0; i < base64.length; i++) {
            charCodes.push(base64.charCodeAt(i));
        }
        return new Blob(
            [new Uint8Array(charCodes)],
            {type: mimeType}
        );
    }
})(jQuery, window, document);