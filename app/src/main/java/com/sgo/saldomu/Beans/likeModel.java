package com.sgo.saldomu.Beans;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.util.SQLiteUtils;

import java.util.List;

/**
 * Created by thinkpad on 5/13/2015.
 */

@Table(name = "Like")
public class likeModel extends Model {
    @Column
    private int like_id;

    @Column
    private String post_id;

    @Column
    private String from_id;

    @Column
    private String from_name;

    @Column
    private String from_profile_picture;

    @Column
    private String to_id;

    @Column
    private String to_name;

    @Column
    private String to_profile_picture;

    @Column
    private String datetime;

    public likeModel() {
        super();
    }

    public likeModel(int _like_id, String _post_id, String _from_id, String _from_name, String _from_profile_picture,
                     String _to_id, String _to_name, String _to_profile_picture, String _datetime) {
        super();
        this.setLike_id(_like_id);
        this.setPost_id(_post_id);
        this.setFrom_id(_from_id);
        this.setFrom_name(_from_name);
        this.setFrom_profile_picture(_from_profile_picture);
        this.setTo_id(_to_id);
        this.setTo_name(_to_name);
        this.setTo_profile_picture(_to_profile_picture);
        this.setDatetime(_datetime);
    }

    public static List<likeModel> getAll() {
        // This is how you execute a query
        return new Select()
                .all()
                .from(likeModel.class).orderBy("like_id DESC")
                .execute();
    }

    public static List<likeModel> getByPostId(String PostId) {
        // This is how you execute a query
        return new Select()
                .all()
                .from(likeModel.class).where("post_id='" + PostId + "'").orderBy("like_id DESC")
                .execute();
    }

    public static void deleteByPostId(String PostId) {
        // This is how you execute a query
        new Delete().from(likeModel.class).where("post_id='" + PostId + "'").execute();
        SQLiteUtils.execSql("DELETE FROM Like WHERE post_id='" + PostId + "';");
    }

    public static void deleteAll() {
        // This is how you execute a query
        new Delete().from(likeModel.class).execute();
        SQLiteUtils.execSql("DELETE FROM SQLITE_SEQUENCE WHERE name='Like';");
    }

    public String getFrom_profile_picture() {
        return from_profile_picture;
    }

    private void setFrom_profile_picture(String from_profile_picture) {
        this.from_profile_picture = from_profile_picture;
    }

    public String getTo_profile_picture() {
        return to_profile_picture;
    }

    private void setTo_profile_picture(String to_profile_picture) {
        this.to_profile_picture = to_profile_picture;
    }

    public String getDatetime() {
        return datetime;
    }

    private void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getFrom_id() {
        return from_id;
    }

    private void setFrom_id(String from_id) {
        this.from_id = from_id;
    }

    public String getFrom_name() {
        return from_name;
    }

    private void setFrom_name(String from_name) {
        this.from_name = from_name;
    }

    public int getLike_id() {
        return like_id;
    }

    private void setLike_id(int like_id) {
        this.like_id = like_id;
    }

    public String getPost_id() {
        return post_id;
    }

    private void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getTo_id() {
        return to_id;
    }

    private void setTo_id(String to_id) {
        this.to_id = to_id;
    }

    public String getTo_name() {
        return to_name;
    }

    private void setTo_name(String to_name) {
        this.to_name = to_name;
    }
}
