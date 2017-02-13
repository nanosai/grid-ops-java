package com.nanosai.gridops.id;

import com.nanosai.gridops.iap.directory.DirectoryKey;

/**
 * Created by jjenkov on 12/02/2017.
 */
public class Id {

    public byte[] idSource = null;
    public int idOffset = 0;
    public int idLength = 0;

    public Id() {
    }

    public Id(byte[] idSource, int idOffset, int idLength) {
        this.idSource = idSource;
        this.idOffset = idOffset;
        this.idLength = idLength;
    }

    public void setEntity(byte[] entityIdSource, int entityIdOffset, int entityIdLength){
        this.idSource = entityIdSource;
        this.idOffset = entityIdOffset;
        this.idLength = entityIdLength;
    }

    public void setIdSource(byte[] idSource) {
        this.idSource = idSource;
    }

    public void setIdOffset(int idOffset) {
        this.idOffset = idOffset;
    }

    public void setIdLength(int idLength) {
        this.idLength = idLength;
    }

    public boolean equalTo(Id target){
        return equalTo(target.idSource, target.idOffset, target.idLength);
    }

    public boolean equalTo(byte[] target, int targetOffset, int targetLength){
        if(this.idLength >= targetLength) {
            return false;
        }

        for(int i = 0; i<this.idLength; i++){
            if(this.idSource[this.idOffset + i] != target[targetOffset + i]){
                return false;
            }
        }
        return true;
    }


    public boolean startsWith(Id id){
        return startsWith(id.idSource, id.idOffset, id.idLength);
    }

    public boolean startsWith(byte[] target, int targetOffset, int targetLength) {
        if(targetLength > this.idLength) {
            return false;
        }
        for(int i=0; i<targetLength; i++){
            if(this.idSource[this.idOffset + i] != target[targetOffset + i]){
                return false;
            }
        }
        return true;
    }



    @Override
    public boolean equals (Object o) {

        if( !(o instanceof DirectoryKey)){
            return false;
        }

        Id otherDirectoryKey = (Id) o;

        if(this.idLength != otherDirectoryKey.idLength){
            return false;
        }
        for(int i=0; i<this.idLength; i++){
            if(this.idSource[this.idOffset + i] != otherDirectoryKey.idSource[otherDirectoryKey.idOffset + i]){
                return false;
            }
        }

        return true;

    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        for(int i=0; i < this.idLength; i++){
            hashCode += this.idSource[this.idOffset + i] * i;
        }

        return hashCode;
    }

}
