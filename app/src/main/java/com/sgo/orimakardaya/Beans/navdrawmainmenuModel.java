package com.sgo.orimakardaya.Beans;/*
  Created by Administrator on 12/9/2014.
 */

public class navdrawmainmenuModel {

    private String title;
    private int navItemId;
    private int indexImage;
    private int indexImageSelected;

    private boolean isGroupHeader = false;

    public navdrawmainmenuModel(String title) {
        setUpData(0,0,title,true,-1);
    }

    public navdrawmainmenuModel(int _indexImage, int _indexImageSelected,String title, int itemId) {
        setUpData(_indexImage,_indexImageSelected,title,false,itemId);
    }

    private void setUpData(int _indexImage, int _indexImageSelected, String title, Boolean _isGroupHeader, int itemId){
        this.setTitle(title);
        this.setNavItemId(itemId);

        if(_isGroupHeader)
            setGroupHeader(true);
        else {
            setGroupHeader(false);
            this.setIndexImage(_indexImage);
            if(_indexImageSelected != 0)
                this.setIndexImageSelected(_indexImageSelected);
            else
                this.setIndexImageSelected(_indexImage);
        }
    }


    public String getTitle() {
    return title;
    }

    public void setTitle(String title) {
    this.title = title;
    }

    public boolean isGroupHeader() {
    return isGroupHeader;
    }

    public void setGroupHeader(boolean isGroupHeader) {
    this.isGroupHeader = isGroupHeader;
    }

    public int getIndexImage() {
    return indexImage;
    }

    public void setIndexImage(int indexImage) {
    this.indexImage = indexImage;
    }

    public int getIndexImageSelected() {
        return indexImageSelected;
    }

    public void setIndexImageSelected(int indexImageSelected) {
        this.indexImageSelected = indexImageSelected;
    }

    public int getNavItemId() {
        return navItemId;
    }

    public void setNavItemId(int navItemId) {
        this.navItemId = navItemId;
    }
}
