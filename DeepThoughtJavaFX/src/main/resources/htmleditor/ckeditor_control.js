
var editor;

var htmlHasBeenSetFromJavaApp = false; // after setting html 'change' is called asynchronously -> ignore this call

var htmlChanged = false;
var resetUndoStack = false;


function initializeCKEditor() {

	CKEDITOR.env.isCompatible = true;
	CKEDITOR.config.removePlugins = 'elementspath,resize,specialchar';

	editor = CKEDITOR.replace( 'editor', {
	   	top: 0,
	   	left: 0,
	   	width : '100%',
	   	height : '100%',
	   	allowedContent: true,

        on: {
			pluginsLoaded: function() {
			   customizeToolbar(editor);
			}
		}
	} );

    <!-- Use instanceReady instead of loaded event as on loaded CKEditor is not fully initialized yet -->
	editor.on('instanceReady', function() {
		if(typeof app !== 'undefined') {
			app.ckEditorLoaded();
		}

		resizeEditorToFitWindow();

		<!--$(editor.editable).click( function(evt) {-->
			<!--if(typeof app !== 'undefined') {-->
				<!--app.elementClicked(evt.target);-->
			<!--}-->
		<!--});-->
	});

	editor.on('change', function(evt) {
		<!-- Notify Java code that Html text has changed -->
		if(typeof app !== 'undefined') {
			if(htmlHasBeenSetFromJavaApp === true) { // after setting html 'change' is called asynchronously -> ignore this call
				htmlHasBeenSetFromJavaApp = false;
				if(resetUndoStack == true) {
					editor.resetUndo();
				}
				return;
			}

			if(htmlChanged === false) {
				htmlChanged = true;
				app.htmlChanged();
			}
			else if(editor.undoManager.hasUndo == false) {
				htmlChanged = false; // TODO: this is not true for all cases e.g. Html got saved in the mean time
				app.htmlHasBeenReset();
			}
		}
	});

	editor.on('beforeCommandExec', function(evt) {
		if(typeof app !== 'undefined') {
			return app.beforeCommandExecution(evt.data.name); // when returning false command then won't be executed
		}
	});

	editor.on( 'doubleclick', function( evt ) {
		var element = evt.data.element;

		if(typeof app !== 'undefined') {
			if(app.elementDoubleClicked(element.getOuterHtml())) {
				evt.data.dialog = '';
				return false;// when returning false command then won't be executed
			 }
		}
			<!--evt.data.dialog = 'tableProperties';-->
	} );

	$(window).resize( function() {
		resizeEditorToFitWindow();
	});

	<!-- handle Dialogs -->
	  <!--CKEDITOR.on('dialogDefinition', function(ev) {-->
        <!--// Take the dialog name and its definition from the event data-->
        <!--var dialogName = ev.data.name;-->
        <!--var dialogDefinition = ev.data.definition;-->

        <!--if (dialogName == 'image') {-->
           <!--dialogDefinition.onOk = function(e) {-->
              <!--var imageSrcUrl = e.sender.originalElement.$.src;-->
              <!--var imgHtml = CKEDITOR.dom.element.createFromHtml("<img src=" + imageSrcUrl + " alt='' align='right'/>");-->
              <!--CKEDITOR.instances.body.insertElement(imgHtml);-->
           <!--};-->
        <!--}-->
  <!--}-->

  	<!-- Preventing clicks on links to prevent navigation to link's target -->

  editor.on( 'contentDom', function() {
    var editable = editor.editable();

    var removeListener = editable.attachListener( editable, 'click', function(evt) {
        var element = CKEDITOR.plugins.link.getSelectedLink( editor ) || evt.data.element;
        if (element!=undefined &&  element.is( 'a' ) ) {
            if (evt.data.preventDefault) {
                evt.data.preventDefault();
            }

            if(evt.data.$.ctrlKey) {
                <!-- link is opened in a new Tab -->
                <!--window.open(element.$.href,'_blank');-->
            }
        }

        <!-- Notify Java code that an element has been clicked -->
        <!--if(typeof app !== 'undefined') {-->
            <!--var data = evt.data.$;-->
            <!--if(app.elementClicked(data.target.outerHTML, data.button, data.clientX, data.clientY)) {-->
                <!--evt.preventDefault(true);-->
                <!--return false;-->
            <!--}-->
        <!--}-->
    });

    editor.on( 'contentDomUnload', function() {
      editable.removeAllListeners();
    });
  });

}

function resizeEditorToFitWindow() {
    <!-- This is curious, mostly it fits perfectly in window then, but sometimes it gets a margin -->
    if(typeof editor !== 'undefined' && typeof window !== 'undefined' && typeof $(window).width() !== 'undefined') {
        try { editor.resize($(window).width() - 16, $(window).height() - 16); } catch (e) { }
    }
}


function replaceImageElement(embeddingId, newNodeHtml) {
    var nodeToReplace = ($(editor.document.$).find('img[embeddingid=' + embeddingId + ']'))[0];
    if(typeof nodeToReplace !== 'undefined') {
        $(nodeToReplace).replaceWith(newNodeHtml);
    }
}

// to be called from Java Application
function setHtml(html, shouldResetUndoStack) {
	htmlHasBeenSetFromJavaApp = true;
	resetUndoStack = shouldResetUndoStack;
	editor.setData(html);
	resetHtmlChanged();
}

function setHtmlHasBeenSaved() {
    resetHtmlChanged();
}

function resetHtmlChanged() {
    htmlChanged = false;
}

function showContextMenu(x, y) {
    var toolbarHeight = 0;
    var marginLeft = 0;
    try {
        var clientRect = CKEDITOR.instances.editor.ui.space('top').getClientRect();
        toolbarHeight = clientRect.bottom;
        marginLeft = clientRect.left;
    } catch(e) { }

    y -= toolbarHeight;
    x -= marginLeft;

    CKEDITOR.instances.editor.contextMenu.show(CKEDITOR.instances.editor.document.getBody(), null, x, y);
}

// for Android versions pre API 19: there was no built in function to get result of a JavaScript execution
function androidGetHtml() {
	if(typeof android !== 'undefined') {
		android.responseToGetHtml(CKEDITOR.instances.editor.getData());
	}
}