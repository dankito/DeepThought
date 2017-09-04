/**
 * @license Copyright (c) 2003-2016, CKSource - Frederico Knabben. All rights reserved.
 * For licensing, see LICENSE.md or http://ckeditor.com/license
 */

CKEDITOR.editorConfig = function( config ) {
	// Define changes to default configuration here.
	// For complete reference see:
	// http://docs.ckeditor.com/#!/api/CKEDITOR.config

	// The toolbar groups arrangement, optimized for a single toolbar row.
	config.toolbarGroups = [
		{ name: 'styles' },
		{ name: 'basicstyles', groups: [ 'basicstyles' ] },
		{ name: 'clipboard',   groups: [ 'clipboard', 'undo' ] },
		{ name: 'insert',   groups: [ 'customized_insert' ] },
		{ name: 'editing',     groups: [ 'find', 'selection', 'spellchecker' ] }
	];

	// The default plugins included in the basic setup define some buttons that
	// are not needed in a basic editor. They are removed here.
	config.removeButtons = 'Cut,Copy,Strike,Subscript,Superscript,Anchor,Link,Unlink,Image,Symbol,Replace';

	// Dialog windows are also simplified.
	config.removeDialogTabs = 'link:advanced';

};

