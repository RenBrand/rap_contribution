rap.registerTypeHandler( "at.renbrand.dnd.dropOutsideWindow", {
    factory : function( properties ){
        return at.renbrand.dnd.DropOutsideWindow.getInstance();
    },

    destructor : "destruct",

    methods : [ "ensureHTMLLocation" ]
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

        ensureHTMLLocation : function( controlParam ){
            var newShell = rwt.remote.ObjectRegistry.getEntry( controlParam.shellID ).object;
            var control = rwt.remote.ObjectRegistry.getEntry( controlParam.controlID ).object;

            if( newShell._getTargetNode() != null ){
                newShell._getTargetNode().appendChild( control._getTargetNode() );
            } else {
                rwt.client.Timer.once( function() {
                    this.ensureHTMLLocation( controlParam );
                }, this, 250 );
            }
        }
    }
});
