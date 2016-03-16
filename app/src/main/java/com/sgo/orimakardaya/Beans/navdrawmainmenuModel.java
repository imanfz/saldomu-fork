package com.sgo.orimakardaya.Beans;/*
  Created by Administrator on 12/9/2014.
 */

public class navdrawmainmenuModel {

  private String title;

  private int indexImage;

  private boolean isGroupHeader = false;

  public navdrawmainmenuModel(int _indexImage,String title,Boolean _isGroupHeader) {
    this.setTitle(title);
    if(_isGroupHeader)setGroupHeader(true);
    else {
      setGroupHeader(false);
      this.setIndexImage(_indexImage);
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
}
