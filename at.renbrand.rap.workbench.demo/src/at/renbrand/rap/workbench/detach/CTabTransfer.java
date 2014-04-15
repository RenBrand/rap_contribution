package at.renbrand.rap.workbench.detach;

import org.eclipse.rap.rwt.SingletonUtil;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;

/**
 * A simple "internal" transfer object which is used to transfer the stack and the tabs to move/drag.
 * <p>
 * The "internal" word should highlight the fact that this transfer object can't be used outside the JVM.
 * This means there is no real native presentation of the data to transfer. It just uses the references
 * to do the transfer and so it can only be used inside one JVM.
 * </p>
 */
final class CTabTransfer extends Transfer {
    
    /** The serialization ID of this object. */
    private static final long serialVersionUID = -7287146696600353183L;
    
    /** The name of the transfer type. */
    private static final String TYPE_NAME = "PresentablePartTransfer";
    
    /** The registered ID of this transfer object/type. */
    private static final int TYPE_ID = registerType(TYPE_NAME);
    
    /** Constant for the success transfer state. */
    private static final int SUCCESS_TRANSFER = 1;
    
    /**
     * Sole private constructor.
     * <p>
     * To retrieve an instance use the {@link CTabTransfer#getInstance()} method.
     * </p> 
     */
    private CTabTransfer() {}

    @Override
    protected int[] getTypeIds() {
        return new int[]{ TYPE_ID };
    }

    @Override
    protected String[] getTypeNames() {
        return new String[]{TYPE_NAME};
    }
    
    /**
     * Creates a session singleton instance of the CTabTransfer.
     * @return a session singleton instance
     */
    public static CTabTransfer getInstance(){
        return SingletonUtil.getSessionInstance(CTabTransfer.class);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b style="color: red;">Caution:</b> There is no real native representation of the {@link CTabDnDHolder}
     * because this Drag&Drop is special and requires the object references.
     * </p>
     */
    @Override
    public void javaToNative(Object data, TransferData transferData) {
        if( !isSupportedType(transferData) || !(data instanceof CTabDnDHolder) ){
            DND.error( DND.ERROR_INVALID_DATA );
        }
        
        transferData.data = data; // use the reference directly
        transferData.result = SUCCESS_TRANSFER;
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * <b style="color: red;">Caution:</b> There is no real native representation of the {@link CTabDnDHolder}
     * because this Drag&Drop is special and requires the object references.
     * </p>
     */
    @Override
    public Object nativeToJava(TransferData transferData) {
        if( !isSupportedType(transferData) || transferData.data == null || transferData.result != SUCCESS_TRANSFER ){
            return null;
        }
        
        return transferData.data; // use the reference directly
    }

    @Override
    public TransferData[] getSupportedTypes() {
        int[] types = getTypeIds();
        TransferData[] supportedTypes = new TransferData[types.length];
        for( int i = 0; i < types.length; i++ ){
            supportedTypes[i] = new TransferData();
            supportedTypes[i].type = types[i];
        }
        return supportedTypes;
    }

    @Override
    public boolean isSupportedType(TransferData transferData) {
        if( transferData != null ) {
            for( int typeID : getTypeIds() ){
                if( transferData.type == typeID ){
                    return true;
                }
            }
        }
        return false;
    }
}
