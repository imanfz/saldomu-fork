package com.sgo.saldomu.models;

import com.sgo.saldomu.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lenovo on 21/03/2017.
 */

public class ShopCategory {

    class MappingImage {
        int imageId;
        String imageCode;

        public MappingImage(int imageId, String imageCode) {
            this.imageId = imageId;
            this.imageCode = imageCode;
        }

        public int getImageId() {
            return imageId;
        }

        public void setImageId(int imageId) {
            this.imageId = imageId;
        }

        public String getImageCode() {
            return imageCode;
        }

        public void setImageCode(String imageCode) {
            this.imageCode = imageCode;
        }
    }



    List<MappingImage> mappingImages;
    private String categoryId;
    private String shopId;
    private String categoryCode;
    private String categoryName;
    private String schemeCode;
    private int categoryImage;

    public ShopCategory() {
        mappingImages   = new ArrayList<>();
        mappingImages.add(new MappingImage(R.drawable.ic_tambahsaldo, "CAT2"));
        mappingImages.add(new MappingImage(R.drawable.ic_tarik_tunai, "CAT3"));
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {

        this.categoryId = categoryId;
        int imageIcon   = 0;
        for(int i =0; i < mappingImages.size(); i++) {
            if ( mappingImages.get(i).getImageCode().equalsIgnoreCase(categoryId) ) {
                imageIcon = mappingImages.get(i).getImageId();
                break;
            }
        }

        if ( imageIcon == 0 ) {
            imageIcon = R.drawable.ic_laporan;
        }

        this.categoryImage = imageIcon;
    }

    public int getCategoryImage() {
        return categoryImage;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getSchemeCode() {
        return schemeCode;
    }

    public void setSchemeCode(String schemeCode) {
        this.schemeCode = schemeCode;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }


}
