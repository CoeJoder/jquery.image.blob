# jquery.image.blob

A simple jQuery plugin for uploading embedded images, eliminating the need to manually download and upload via form.  The image is converted into a Blob and uploaded as a file.

## Installation

Add this line *after* the jQuery library.

```html
<script src="/path/to/jquery.image.blob.js"></script>
```

## Usage

Create an image blob:

```javascript
var blob = $('img').imageBlob().blob();
console.log('size=' + blob.size);
console.log('type=' + blob.type);
```

Create an image blob and upload it:

```javascript
$('img').imageBlob().ajax('/upload', {
    complete: function(jqXHR, textStatus) { console.log(textStatus); } 
});
```
Create an image blob and upload it along with other params:

```javascript
$('img').imageBlob()
    .formData({foo : 'bar'})
    .ajax('/upload');
```

Create an image blob with the specified MIME type:

```javascript
var blob = $('img').imageBlob('image/jpeg').blob();
```

## Configuration

The default AJAX settings are inherited from `$.ajaxSettings` and can be further modified:

```javascript
// using GET instead of POST
$.fn.imageBlob.ajaxSettings.type = 'GET';
```

If the image's `name` attribute is missing, a default filename is used:

```javascript
$.fn.imageBlob.defaultImageName = 'IMG_Upload';
```

## Author
Joe Nasca
