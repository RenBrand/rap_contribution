rap.registerTypeHandler( "at.renbrand.dnd.dropOutsideWindow", {
    factory : function( properties ){
        return at.renbrand.dnd.DropOutsideWindow.getInstance();
    },

    destructor : "destruct",

    methods : [ "detachWindow", "ensureHTMLLocation" ]
});

rwt.qx.Class.define( "at.renbrand.dnd.DropOutsideWindow", {
    type : "singleton",
    extend : rwt.qx.Object,

    construct : function() {
        console.log( "initialize drop outside window" );
        var el = rwt.client.Client.isGecko() ? window : document.body;
        rwt.html.EventRegistration.addEventListener( el, "mouseup", this._dropOutsideWindow );
    },

    destruct : function() {
        console.log( 'cleanup drop outside window' );
        var el = rwt.client.Client.isGecko() ? window : document.body;
        rwt.html.EventRegistration.removeEventListener( el, "mouseup", this._dropOutsideWindow );
    },

    members : {
        _windows : [],

        _dropOutsideWindow : function(e){
            console.log('mouse up on: ' + e);

            var dndHandler = rwt.event.DragAndDropHandler.getInstance();

            if( dndHandler.__dragCache && dndHandler.__dragCache.dragHandlerActive ){
                e = rwt.event.EventHandlerUtil.getDomEvent( arguments );  // don't use 'e' directly because getDomEvent needs the arguments array to work correctly
                console.log( 'drag was started: ' + e );

                /* this check is just for safety, we aren't called with an active drag handler if the drop is performed inside the RAP window */
                if( e.clientX < 0 || e.clientY < 0 || 
                    e.clientX > rwt.html.Window.getInnerWidth(window) || e.clientY > rwt.html.Window.getInnerHeight(window) ){
                    console.log( 'it\'s time to stop DnD, because we should create a new window' );
                    rap.getRemoteObject( at.renbrand.dnd.DropOutsideWindow.getInstance() ).call( "dropOutsideWindow", { dropAtX : e.screenX, dropAtY : e.screenY } );
                    dndHandler.cancelDrag( new rwt.event.MouseEvent("mouseup", e, null, null, null, null )); // cancel used because we don't have a destination widget
                }
            } else {
                console.log( 'we shouldn\'t care about this!' );
            }
        },

        _addRWTReferences : function( w ){
            for( var wAttr in window ){
                if( w[ wAttr ] === undefined ){
                    console.log( "copy attribute: " + wAttr );
                    w[ wAttr ] = window[ wAttr ];
                }
            }
        },

        _windowClosed : function( e ){
            var windows = at.renbrand.dnd.DropOutsideWindow.getInstance()._windows;
            console.log( "before remove: " + windows );
            windows.pop( window );
            console.log( "after remove: " + windows );
        },

        detachWindow : function( properties ){
            var shellID = properties.newShell;
            var w = window.open("", shellID, "left=" + properties.style.x + ",top=" + properties.style.y + ",width=" + properties.style.width + ",heigth=" + properties.style.heigth + ",menubar=no,location=no,status=no,toolbar=no", true);
            this._addRWTReferences(w);
            this._windows.push( w );

            var d = w.document;

            // --- specify base to resolve relative URL resource (e.g.: images, css, scripts, ...) --
            var base = d.createElement("base");
            base.setAttribute("href", document.baseURI);
            d.head.appendChild( base );

            // --- copy the window header ---
            d.title = w.opener.document.title + ": ";
            for( var part in properties.parts ){
                d.title += properties.parts[ part ] + ", ";
            }

            if( properties.parts.length > 0 ){
                d.title = d.title.substring(0, d.title.length - 2);
            }

            // --- register RAP specific listeners (enables event handling in RAP) ---
            this._attachListeners(w);

            // --- move shell div to the new window ---
            var newShellElement = rwt.remote.ObjectRegistry.getEntry( shellID ).object;
            var newShellParentElement = newShellElement.getParent();
            var newShellHTMLNode = newShellElement._getTargetNode();
            d.body.appendChild( newShellHTMLNode );
        },

        _attachListeners : function( w ){
            var el = w.rwt.client.Client.isGecko() ? w : w.document.body;
            var eUtil = w.rwt.html.EventRegistration;
            var eHandler = w.rwt.event.EventHandler;

            // our specialized unload listener
            eUtil.addEventListener( el, "unload", this._windowClosed );

            // reattach listeners from rwt.event.EventHandler
            this._modAttachEventTypes( el, eHandler._mouseEventTypes, eHandler.__onmouseevent );
            this._modAttachEventTypes( el, eHandler._dragEventTypes, eHandler.__ondragevent );
            this._modAttachEventTypes( el, eHandler._keyEventTypes, eHandler.__onKeyEvent );
            eUtil.addEventListener( el, "blur", eHandler.__onwindowblur );
            eUtil.addEventListener( el, "focus", eHandler.__onwindowfocus );
            eUtil.addEventListener( el, "resize", eHandler.__onwindowresize );
            w.document.body.onselect = eHandler.__onselectevent;
            w.document.onselectstart = eHandler.__onselectevent;
            w.document.onselectionchange = eHandler.__onselectevent;
        },

        _modAttachEventTypes : function( el, vEventTypes, vFunctionPointer ) {
            try {
                // Gecko is a bit buggy to handle key events on document if
                // not previously focused. Internet Explorer has problems to use
                // 'window', so there we use the 'body' element
                for( var i=0, l=vEventTypes.length; i<l; i++ ) {
                    rwt.html.EventRegistration.addEventListener( el, vEventTypes[i], vFunctionPointer );
                }
            }
            catch( ex ) {
                throw new Error( "EventHandler: Failed to attach window event types: " + vEventTypes + ": " + ex );
            }
                  
        },

        ensureHTMLLocation : function( controlParam ){
            var newShell = rwt.remote.ObjectRegistry.getEntry( controlParam.shellID ).object;
            var control = rwt.remote.ObjectRegistry.getEntry( controlParam.controlID ).object;

            newShell._getTargetNode().appendChild( control._getTargetNode() );
        }
    }
});
