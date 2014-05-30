rap.registerTypeHandler( "at.renbrand.rap.detach.unleashedShell", {
    factory : function( properties ){
        return at.renbrand.rap.detach.UnleashedShell.getInstance();
    },

    destructor : "destruct",

    methods : [ "detachWindow", "ensureHTMLLocation" ]
});

rwt.qx.Class.define( "at.renbrand.rap.detach.UnleashedShell", {
    type : "singleton",
    extend : rwt.qx.Object,

    construct : function() {
        console.log( "initialize drop outside window" );
    },

    destruct : function() {
        console.log( 'cleanup drop outside window' );
    },

    members : {
        _windows : [],

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
            var w = window.open("", shellID, this._buildSpecsString(properties.specs), true);
            this._addRWTReferences(w);
            this._windows.push( w );

            var d = w.document;

            // --- specify base to resolve relative URL resource (e.g.: images, css, scripts, ...) --
            var base = d.createElement("base");
            base.setAttribute("href", document.baseURI);
            d.head.appendChild( base );

            // --- copy the window header ---
            d.title = w.opener.document.title;
            if( properties.title !== undefined ){
                if( d.title !== undefined && d.title != "" ){
                    d.title += ": ";
                }
                d.title += properties.title;
            }

            // --- register RAP specific listeners (enables event handling in RAP) ---
            this._attachListeners(w);

            // --- move shell div to the new window ---
            var newShellElement = rwt.remote.ObjectRegistry.getEntry( shellID ).object;
            var newShellParentElement = newShellElement.getParent();
            var newShellHTMLNode = newShellElement._getTargetNode();
            d.body.appendChild( newShellHTMLNode );
        },

        _buildSpecsString : function( specs ){
            var specsStr = '';

            if( specs !== undefined ){
                
                for( var option in specs ){
                    specsStr += option + '=' + specs[ option ] + ',';
                }

                specsStr = specsStr.replace(/,$/, '');  /* remove last: , */
            }

            console.log( "open specs for window: " + specsStr );

            return specsStr;
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
