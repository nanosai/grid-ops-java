package com.nanosai.gridops.directory;

import java.util.Arrays;

/**
 * Created by jjenkov on 10-09-2016.
 */
public class DirectoryKey {
	
	private byte[] id;
	
	@Override
	public boolean equals (Object o) {
		
		if(o instanceof DirectoryKey){
			DirectoryKey directoryKey = (DirectoryKey) o;
			
			if(Arrays.equals(directoryKey.getId(), this.getId())){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		
		return Arrays.hashCode(this.getId());
	 }

	public byte[] getId() {
		return id;
	}

	public void setId(byte[] id) {
		this.id = id;
	}
}
