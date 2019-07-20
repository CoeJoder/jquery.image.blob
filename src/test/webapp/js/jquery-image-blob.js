/**
 * Image Blob v1.0
 *        A jQuery plugin for uploading embedded IMG elements, thus eliminating
 *        the need to download locally and insert into a form field.  This
 *        implementation copies the image data and converts it into a Blob before
 *        uploading to the server.
 *
 * @author Joe Nasca
 * @see http://stackoverflow.com/a/12470362/159570
 */
(function ($, window, document, undefined) {

    /**
     * Plugin initializer.
     * @param mimeType    [optional] The MIME type of created blobs
     */
    $.fn.imageBlob = function (mimeType) {

        /**
         * Get a blob of the first image in the set of matched images.
         *
         * Usage:
         *        var blob = $('img').imageBlob().blob();
         *
         * @returns A blob of the image, or null if first matched element is not an IMG.
         */
        this.blob = function () {
            var img = getFirstMatchedDomImage(this);
            if (!img) return null;
            return getImageBlob(img);
        };

        /**
         * Set additional parameters to be sent in the AJAX request.  Chained
         * method.
         * @param obj   A hash of parameter values to send with the blob
         */
        this.formData = function (obj) {
            if (typeof obj == 'object') {
                var fd = new FormData();
                for (var i in obj) {
                    fd.append(i, obj[i]);
                }
                formData = fd;
            }
            return this;
        };

        /**
         * Get a blob of the first image in the set of matched images and perform
         * a jQuery AJAX request with it.  The "name" attribute of the IMG is used
         * as the request parameter name, or a default name is used.
         *
         * Usage:
         *        $('img').imageBlob().ajax('/upload', {
         * 			complete: function(jqXHR, textStatus) { alert(textStatus); }
         * 		});
         *
         * @param url       [optional] The destination URL
         * @param settings  [optional] AJAX settings object (overrides the plugin defaults)
         * @returns         A jqXHR object, or null if first matched element is not an IMG.
         */
        this.ajax = function (url, settings) {
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
            if (typeof formData == 'undefined') {
                formData = new FormData();
            }
            formData.append(filename, blob, filename);
            ajaxSettings.data = formData;

            // perform the AJAX request
            if (typeof url == 'string') {
                return $.ajax(url, ajaxSettings);
            }
            return $.ajax(ajaxSettings);
        }

        /////////////////////////////////////
        // private instance members
        /////////////////////////////////////

        var formData;
        var DATA_URI_REGEXP = /data:(image\/[^;]+);base64,(.+)/;
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
            src = src.replace(/\s/g, '');	// atob() can't handle whitespace
            var matches = src.match(DATA_URI_REGEXP);
            if (matches == null) {
                if (typeof mimeType != 'string') {
                    if (src.match(JPEG_REGEXP) != null) {
                        mimeType = 'image/jpeg';
                    } else {
                        mimeType = 'image/png';
                    }
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
            var base64 = atob(dataUri);
            var charCodes = [];
            for (var i = 0; i < base64.length; i++) {
                charCodes.push(base64.charCodeAt(i));
            }
            return new Blob(
                [new Uint8Array(charCodes)],
                {type: mimeType}
            );
        }

        // expose the public methods
        return this;
    };

    /////////////////////////////////////
    // public configurations (can be overridden)
    /////////////////////////////////////

    // Default AJAX settings
    $.fn.imageBlob.ajaxSettings = $.extend({}, $.ajaxSettings, {
        cache: false,
        processData: false, // necessary when submitting a FormData object
        contentType: false, // will ensure the correct content-type and multipart boundaries
        type: 'POST'
    });

    // Default image name (used when "name" attribute is missing).
    $.fn.imageBlob.defaultImageName = 'IMG_Upload';

})(jQuery, window, document);
