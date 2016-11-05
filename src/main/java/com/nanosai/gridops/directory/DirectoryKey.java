package com.nanosai.gridops.directory;

import java.util.Arrays;

/**
 * Created by jjenkov on 10-09-2016.
 */
public class DirectoryKey {
	
	private byte[] idSource;
	private int    idOffset;
	private int    idLength;
	
	@Override
	public boolean equals (Object o) {

		if( !(o instanceof DirectoryKey)){
			return false;
		}
		
		DirectoryKey otherDirectoryKey = (DirectoryKey) o;

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
		
		return Arrays.hashCode(this.getIdSource());
	}

    public DirectoryKey() {
    }

    public DirectoryKey(byte[] idSource, int idOffset, int idLength) {
		this.idSource = idSource;
		this.idOffset = idOffset;
		this.idLength = idLength;
	}

	public byte[] getIdSource() {
		return idSource;
	}

	public void setIdSource(byte[] idSource) {
		this.idSource = idSource;
	}

	public int getIdOffset() {
		return idOffset;
	}

	public void setIdOffset(int idOffset) {
		this.idOffset = idOffset;
	}

	public int getIdLength() {
		return idLength;
	}

	public void setIdLength(int idLength) {
		this.idLength = idLength;
	}
}
