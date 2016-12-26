package com.zsf.guesssong.model;

/**
 * Created by zsf on 2016/12/26.
 */
public class Song {
    //歌曲名称
    private String mSongName;
    //歌曲的文件名
    private String mSongFileName;
    //歌曲名字长度
    private int mNameLength;

    /**
     * 返回一个字符串的歌曲名
     * @return
     */
    public char[] getNameCharacter(){
        return mSongName.toCharArray();
    }

    public String getmSongName() {
        return mSongName;
    }

    public void setSongName(String songName) {
        this.mSongName = songName;
        this.mNameLength = songName.length();
    }

    public String getSongFileName() {
        return mSongFileName;
    }

    public void setSongFileName(String songFileName) {
        this.mSongFileName = songFileName;
    }

    public int getNameLength() {
        return mNameLength;
    }

    public void setNameLength(int nameLength) {
        this.mNameLength = nameLength;
    }
}
