package com.imooc.guessmusic.model;

public class Song {
	private String mSongName;
	private String mSongFileName;
	private int mNameLength;
	
	public char[] getNameCharacters(){
		return mSongName.toCharArray();
	}
	
	public String getmSongName() {
		return mSongName;
	}
	public void setSongName(String mSongName) {
		this.mSongName = mSongName;
		this.mNameLength = this.mSongName.length();
	}
	public String getSongFileName() {
		return mSongFileName;
	}
	public void setSongFileName(String mSongFileName) {
		this.mSongFileName = mSongFileName;
	}
	public int getNameLength() {
		return mNameLength;
	}
	public void setNameLength(int mNameLength) {
		this.mNameLength = mNameLength;
	}
	
}
