
function customizeToolbar(editor) {
    groupStyleItems(editor)
    groupInsertItems(editor);
}

function groupStyleItems(editor) {
    editor.addMenuGroup( 'style_group' );

    var items = {};

    items.bold = {
        label: editor.lang.basicstyles.bold,
        group: 'style_group',
        command: 'bold',
        order: 10
    };

    items.italic = {
        label: editor.lang.basicstyles.italic,
        group: 'style_group',
        command: 'italic',
        order: 20
    };

    items.underline = {
        label: editor.lang.basicstyles.underline,
        group: 'style_group',
        command: 'underline',
        order: 30
    };

    editor.addMenuItems( items );

    editor.ui.add( 'basicstyles', CKEDITOR.UI_MENUBUTTON, {
        label: editor.lang.toolbar.toolbarGroups.styles,
        modes: { wysiwyg: 1 },
        icon: 'bold',
        toolbar: 'customized_basic_style,10',
        onMenu: function() {
            var active = {};

            for ( var p in items )
                active[ p ] = CKEDITOR.TRISTATE_OFF;

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

    // not that logic but in order to save space on the toolbar i added them here
    items.outdent = {
        label: editor.lang.indent.outdent,
        group: 'insert_group',
        command: 'outdent',
        directional: true,
        order: 40
    };

    items.indent = {
        label: editor.lang.indent.indent,
        group: 'insert_group',
        command: 'indent',
        directional: true,
        order: 40
    };

    items.link = {
        label: editor.lang.link.toolbar,
        group: 'insert_group',
        command: 'link',
        order: 50
    };

    items.unlink = {
        label: editor.lang.link.unlink,
        group: 'insert_group',
        command: 'unlink',
        order: 50
    };

    items.symbol = {
        label: editor.lang.symbol.toolbar,
        group: 'insert_group',
        command: 'symbol',
        order: 60
    };

    editor.addMenuItems( items );

    editor.ui.add( 'insert', CKEDITOR.UI_MENUBUTTON, {
        label: editor.lang.toolbar.toolbarGroups.insert,
        modes: { wysiwyg: 1 },
        icon: 'image',
        toolbar: 'customized_insert,10',
        onMenu: function() {
            var active = {};

            for ( var p in items )
                active[ p ] = CKEDITOR.TRISTATE_OFF;

            return active;
        }
    } );
}

