
function customizeToolbar(editor) {
    addItemsFromDropDownMenusToHiddenToolbar(editor);

    groupMoreStylesItems(editor);

    groupClipboardItems(editor);

    // maybe give BulletList, NumberedList, Outend and Intend back their own toolbar (add 'list_and_indent' to 'format' again below)
//    configureListAndIndentToolbar(editor);

    groupJustifyItems(editor);

    groupInsertItems(editor);

    //configureSpellCheckAndBidiToolbar(editor); // may give BiDi options their own Drop Down Menu back again; placing them in More Styles Drop Down is not that senseful

    configureMaximizeAndRareUsedItemsToolbar(editor);

    setToolbarGroups(editor);

	editor.on('loaded', function() {
		hideToolbarWithOtherwiseGroupedItems(); // now hide buttons, which menu items don't work without a button, again
	});
}

function hideToolbarWithOtherwiseGroupedItems() {
    $('.cke_toolbar a[title=\"Align Left\"]').parent().parent().hide();
}


function setToolbarGroups() {
    CKEDITOR.config.toolbarGroups = [
    		{ name: 'styles', groups: [ 'styles' ] },
    		{ name: 'basicstyles', groups: [ 'basicstyles' ] },
    		{ name: 'clipboard', groups: [ 'clipboard', 'undo' ] },
    		{ name: 'format', groups: [ /*'list_and_indent',*/ 'justify_and_insert', 'colors', 'find' ] },
    		{ name: 'language_and_rare_items', groups: [ 'spellchecker', /*'bidi',*/ 'maximize_and_rare_used_items' ] }, // may show Language button again and give BiDi options their own Drop Down back again
    		{ name: 'hidden', groups: [ 'hidden' ] } // to circumvent that a lot of items aren't shown in Drop Down menus, see explanation below
    	];

    CKEDITOR.config.removeButtons = 'Styles,Paste,PasteText,PasteFromWord,Replace,Table,Image,Chart,HorizontalRule,PageBreak,Anchor,Symbol,EqnEditor,texzilla,Smiley';
}

/*  This costed me a lot of time to find out: A lot of commands aren't show as item in Drop Down menus if they haven't been registered otherwise
    So i add these commands to a toolbar called 'hidden' as Buttons and hide this toolbar then */
function addItemsFromDropDownMenusToHiddenToolbar(editor) {
    editor.ui.addButton( 'Strike', {
        label: editor.lang.basicstyles.strike,
        command: 'strike',
        toolbar: 'hidden'
    } );

    editor.ui.addButton( 'Subscript', {
        label: editor.lang.basicstyles.subscript,
        command: 'subscript',
        toolbar: 'hidden'
    } );

    editor.ui.addButton( 'Superscript', {
        label: editor.lang.basicstyles.superscript,
        command: 'superscript',
        toolbar: 'hidden'
    } );

    editor.ui.addButton( 'blockquote', {
        label: editor.lang.blockquote.toolbar,
        command: 'blockquote',
        toolbar: 'hidden'
    } );


    editor.ui.addButton( 'JustifyLeft', {
        label: editor.lang.justify.left,
        command: 'justifyleft',
        toolbar: 'hidden'
    } );

    editor.ui.addButton( 'JustifyCenter', {
        label: editor.lang.justify.center,
        command: 'justifycenter',
        toolbar: 'hidden'
    } );

//    editor.ui.addButton( 'JustifyRight', {
//        label: editor.lang.justify.right,
//        command: 'justifyright',
//        toolbar: 'hidden'
//    } );
//
//    editor.ui.addButton( 'JustifyBlock', {
//        label: editor.lang.justify.block,
//        command: 'justifyblock',
//        toolbar: 'hidden'
//    } );


    editor.ui.addButton( 'link', {
        label: editor.lang.link.toolbar,
        command: 'link',
        toolbar: 'hidden'
    } );

    editor.ui.addButton( 'unlink', {
        label: editor.lang.link.unlink,
        command: 'unlink',
        toolbar: 'hidden'
    } );

    editor.ui.addButton( 'anchor', {
        label: editor.lang.link.anchor.toolbar,
        command: 'anchor',
        toolbar: 'hidden'
    } );

    editor.ui.addButton( 'table', {
        label: editor.lang.table.toolbar,
        command: 'table',
        toolbar: 'hidden'
    } );

    editor.ui.addButton( 'horizontalrule', {
        label: editor.lang.horizontalrule.toolbar,
        command: 'horizontalrule',
        toolbar: 'hidden'
    } );

    editor.ui.addButton( 'pagebreak', {
        label: editor.lang.pagebreak.toolbar,
        command: 'pagebreak',
        toolbar: 'hidden'
    } );


    editor.ui.addButton( 'bidirtl', {
        label: editor.lang.bidi.ltr,
        command: 'bidiltr',
        toolbar: 'hidden'
    } );

    editor.ui.addButton( 'bidirtl', {
        label: editor.lang.bidi.rtl,
        command: 'bidirtl',
        toolbar: 'hidden'
    } );
}


function groupMoreStylesItems(editor) {
    editor.addMenuGroup( 'more_styles_group' );

    var items = {};

    items.strike = {
        label: editor.lang.basicstyles.strike,
        group: 'more_styles_group',
        command: 'strike',
        order: 1
    };

    items.subscript = {
        label: editor.lang.basicstyles.subscript,
        group: 'more_styles_group',
        command: 'subscript',
        order: 2
    };

    items.superscript = {
        label: editor.lang.basicstyles.superscript,
        group: 'more_styles_group',
        command: 'superscript',
        order: 3
    };

    items.blockquote = {
        label: editor.lang.blockquote.toolbar,
        group: 'more_styles_group',
        command: 'blockquote',
        order: 40
    };

    // not that logic but in order to save space on the toolbar i added them here
    items.outdent = {
        label: editor.lang.indent.outdent,
        group: 'more_styles_group',
        command: 'outdent',
        directional: true,
        order: 50
    };

    items.indent = {
        label: editor.lang.indent.indent,
        group: 'more_styles_group',
        command: 'indent',
        directional: true,
        order: 50
    };

    // also not that logic but for saving some space on the toolbar i also added them here
    items.bidiltr = {
        label: editor.lang.bidi.ltr,
        group: 'more_styles_group',
        command: 'bidiltr',
        order: 70
    };

    items.bidirtl = {
        label: editor.lang.bidi.rtl,
        group: 'more_styles_group',
        command: 'bidirtl',
        order: 70
    };

    items.removeFormat = {
        label: editor.lang.removeformat.toolbar,
        group: 'more_styles_group',
        command: 'removeFormat',
        order: 100
    };

    editor.addMenuItems( items );

    editor.ui.add( 'more_styles', CKEDITOR.UI_MENUBUTTON, {
        label: 'More Styles', // TODO: translate
        modes: { wysiwyg: 1 },
        icon: 'superscript',
        toolbar: 'basicstyles,100',
        onMenu: function() {
            var active = {};

            // Make all items active.
            for ( var p in items )
                active[ p ] = CKEDITOR.TRISTATE_OFF;

            return active;
        }
    } );
}


function groupClipboardItems(editor) {
    editor.addMenuGroup( 'paste_group' );

    var items = {};

    items.paste = {
        label: editor.lang.clipboard.paste,
        group: 'paste_group',
        command: 'paste',
        order: 1
    };

    items.pastetext = {
        label: editor.lang.pastetext.title,
        group: 'paste_group',
        command: 'pastetext',
        order: 2
    };

    items.pastefromword = {
        label: editor.lang.pastefromword.title,
        group: 'paste_group',
        command: 'pastefromword',
        order: 3
    };

    editor.addMenuItems( items );

    editor.ui.add( 'clipboard', CKEDITOR.UI_MENUBUTTON, {
        label: editor.lang.clipboard.paste, // TODO: translate
        modes: { wysiwyg: 1 }, // disable in source mode // TODO: but is this for Clipboard items, at least Paste, that senseful?
        icon: 'Paste',
        toolbar: 'clipboard,30',
        onMenu: function() {
            var active = {};

            // Make all items active.
            for ( var p in items )
                active[ p ] = CKEDITOR.TRISTATE_OFF;

            return active;
        }
    } );
}


function configureListAndIndentToolbar(editor) {
    editor.ui.addButton( 'NumberedList', {
        label: editor.lang.list.numberedlist,
        command: 'numberedlist',
        directional: true,
        toolbar: 'list_and_indent,10'
    } );

    editor.ui.addButton( 'BulletedList', {
        label: editor.lang.list.bulletedlist,
        command: 'bulletedlist',
        directional: true,
        toolbar: 'list_and_indent,20'
    } );

    editor.ui.addButton( 'Outdent', {
        label: editor.lang.indent.outdent,
        command: 'outdent',
        directional: true,
        toolbar: 'list_and_indent,30'
    } );

    editor.ui.addButton( 'Indent', {
        label: editor.lang.indent.indent,
        command: 'indent',
        directional: true,
        toolbar: 'list_and_indent,40'
    } );
}

function groupJustifyItems(editor) {
    editor.addMenuGroup( 'justify_group' );

    var items = {};

    items.justifyleft = {
        label: editor.lang.justify.left,
        group: 'justify_group',
        command: 'justifyleft',
        order: 1
    };

    items.justifycenter = {
        label: editor.lang.justify.center,
        group: 'justify_group',
        command: 'justifycenter',
        order: 2
    };

    items.justifyright = {
        label: editor.lang.justify.right,
        group: 'justify_group',
        command: 'justifyright',
        order: 3
    };

    items.justifyblock = {
        label: editor.lang.justify.block,
        group: 'justify_group',
        command: 'justifyblock',
        order: 4
    };

    editor.addMenuItems( items );

    editor.ui.add( 'Justify', CKEDITOR.UI_MENUBUTTON, {
        label: editor.lang.common.alignJustify,
        name: 'Justify',
        modes: { wysiwyg: 1 },
        icon: 'JustifyLeft',
        toolbar: 'justify_and_insert,10',
        onMenu: function() {
            var active = {};

            for ( var p in items )
                //active[ p ] = CKEDITOR.TRISTATE_OFF;
                active[ p ] = null;

            return active;
        }
    } );
}


function groupInsertItems(editor) {
    editor.addMenuGroup( 'insert_group' );

    var items = {};

    items.image = {
        label: editor.lang.common.image,
        group: 'insert_group',
        command: 'image',
        order: 10
    };

    items.table = {
        label: editor.lang.table.toolbar,
        group: 'insert_group',
        command: 'table',
        order: 10
    };

    // also not that logic, but for the sake of saving space on the toolbar i placed NumberedList and BulletedList here
    items.numberedlist = {
        label: editor.lang.list.numberedlist,
        group: 'insert_group',
        command: 'numberedlist',
        directional: true,
        order: 30
    };

    items.bulletedlist = {
        label: editor.lang.list.bulletedlist,
        group: 'insert_group',
        command: 'bulletedlist',
        directional: true,
        order: 30
    };

    items.link = {
        label: editor.lang.link.toolbar,
        group: 'insert_group',
        command: 'link',
        order: 40
    };

    items.unlink = {
        label: editor.lang.link.unlink,
        group: 'insert_group',
        command: 'unlink',
        order: 40
    };

    items.anchor = {
        label: editor.lang.link.anchor.toolbar,
        group: 'insert_group',
        command: 'anchor',
        order: 40
    };

//    items.videodetector = {
//        label: 'Insert a Youtube, Vimeo or Dailymotion video', // TODO: translate
//        group: 'insert_group',
//        command: 'videodetector',
//        icon: CKEDITOR.plugins.getPath('videodetector') + '/icons/videodetector_gray.png',
//        order: 60
//    };
//
//    items.videodetector = {
//        label : editor.lang.youtube.button,
//        group: 'insert_group',
//        command: 'youtube',
//        order: 60
//    };

    items.symbol = {
        label: editor.lang.symbol.toolbar,
        group: 'insert_group',
        command: 'symbol',
        order: 60
    };

    items.eqneditor = {
        label: editor.lang.eqneditor.toolbar,
        group: 'insert_group',
        command: 'eqneditorDialog',
        order: 60
    };

// TODO: translate
    items.texzilla = {
        label: "Insert MathML based on (La)TeX",
        group: 'insert_group',
        command: 'texzillaDialog',
        order: 60
    };

    items.chart = {
        label: editor.lang.chart.chart,
        group: 'insert_group',
        command: 'chart',
        order: 60
    };

    items.horizontalrule = {
        label: editor.lang.horizontalrule.toolbar,
        group: 'insert_group',
        command: 'horizontalrule',
        order: 60
    };

    items.pagebreak = {
        label: editor.lang.pagebreak.toolbar,
        group: 'insert_group',
        command: 'pagebreak',
        order: 60
    };

    editor.addMenuItems( items );

    editor.ui.add( 'insert', CKEDITOR.UI_MENUBUTTON, {
        label: editor.lang.toolbar.toolbarGroups.insert,
        modes: { wysiwyg: 1 },
        icon: 'image',
        toolbar: 'justify_and_insert,20',
        onMenu: function() {
            var active = {};

            for ( var p in items )
                active[ p ] = CKEDITOR.TRISTATE_OFF;

            return active;
        }
    } );
}


function configureSpellCheckAndBidiToolbar(editor) {
//    editor.ui.addButton( 'Maximize', {
//        label: editor.lang.scayt.text_title,
//        title : ( editor.plugins.wsc ? editor.lang.wsc.title : lang.text_title ),
//        command: 'maximize',
//        toolbar: 'maximize_and_rare_used_items,10'
//    } );

    editor.addMenuGroup( 'bidi_group' );

    var items = {};

    items.bidiltr = {
        label: editor.lang.bidi.ltr,
        group: 'bidi_group',
        command: 'bidiltr',
        order: 1
    };

    items.bidirtl = {
        label: editor.lang.bidi.rtl,
        group: 'bidi_group',
        command: 'bidirtl',
        order: 2
    };

    editor.addMenuItems( items );

    editor.ui.add( 'BiDi', CKEDITOR.UI_MENUBUTTON, {
        label: 'BiDi', // TODO: translate
        modes: {
            wysiwyg: 1
        },
        icon: 'bidiltr',
        toolbar: 'bidi,0',
        onMenu: function() {
            var active = {};

            for ( var p in items )
                //active[ p ] = CKEDITOR.TRISTATE_OFF;
                active[ p ] = null;

            return active;
        }
    } );
}


function configureMaximizeAndRareUsedItemsToolbar(editor) {
    editor.ui.addButton( 'Maximize', {
        label: editor.lang.maximize.maximize,
        command: 'maximize',
        toolbar: 'maximize_and_rare_used_items,10'
    } );

    groupRareUsedItems(editor);
}

function groupRareUsedItems(editor) {
    editor.addMenuGroup( 'rare_used_items_group' );

    var items = {};

    items.preview = {
        label: editor.lang.preview.preview,
        group: 'rare_used_items_group',
        command: 'preview',
        order: 1
    };

    items.print = {
        label: editor.lang.print.toolbar,
        group: 'rare_used_items_group',
        command: 'print',
        order: 2
    };

    items.source = {
        label: editor.lang.sourcearea.toolbar,
        group: 'rare_used_items_group',
        command: 'source',
        order: 3
    };

    items.about = {
        label: editor.lang.about.title,
        group: 'rare_used_items_group',
        command: 'about',
        order: 100
    };

    editor.addMenuItems( items );

    editor.ui.add( 'RareUsed', CKEDITOR.UI_MENUBUTTON, {
        label: 'Rare Used', // TODO: translate
		modes: { wysiwyg: 1, source: 1 }, // Source command of course has to be abled in source mode
        icon: 'preview',
        toolbar: 'maximize_and_rare_used_items,100',
        onMenu: function() {
            var active = {};

            for ( var p in items )
                active[ p ] = CKEDITOR.TRISTATE_OFF;

            return active;
        }
    } );
}