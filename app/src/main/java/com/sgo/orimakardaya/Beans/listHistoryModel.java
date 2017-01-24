package com.sgo.orimakardaya.Beans;/*
  Created by Administrator on 4/13/2015.
 */

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.activeandroid.util.SQLiteUtils;

import java.util.List;

@Table(name = "PostHistory")
public class listHistoryModel extends Model {

    @Column
    private int history_id;

    @Column
    private String post;

    @Column
    private String amount;

    @Column
    private String balance;

    @Column
    private String ccy_id;

    @Column
    private String datetime;

    @Column
    private String owner;

    @Column
    private String owner_id;

    @Column
    private String owner_profile_picture;

    @Column
    private String with_id;

    @Column
    private String with;

    @Column
    private String with_profile_picture;

    @Column
    private String tx_status;

    @Column
    private String typepost;

    @Column
    private String typecaption;

    @Column
    private String privacy;

    @Column
    private String numcomments;

    @Column
    private String numviews;

    @Column
    private String numlikes;

    @Column
    private String share;

    @Column
    private String comments;

    @Column
    private String likes;

    @Column
    private String comment_id_1;

    @Column
    private String comment_id_2;

    @Column
    private String from_name_1;

    @Column
    private String from_name_2;

    @Column
    private String from_profile_picture_1;

    @Column
    private String from_profile_picture_2;

    @Column
    private String reply_1;

    @Column
    private String reply_2;

    @Column
    private String isLike;


    public listHistoryModel() {
        super();
    }

    public listHistoryModel(int _history_id, String _post, String _amount,
                             String _balance, String _ccy_id, String _datetime, String _owner,
                             String _owner_id, String _owner_profile_picture,
                             String _with_id, String _with, String _with_profile_picture, String _tx_status,
                             String _typepost, String _typecaption, String _privacy, String _numcomments,
                             String _numviews, String _numlikes, String _share, String _comments, String _likes,
                             String _comment_id_1, String _from_name_1, String _from_profile_picture_1, String _reply_1,
                             String _comment_id_2, String _from_name_2, String _from_profile_picture_2, String _reply_2, String _isLike){

        super();
        this.setHistory_id(_history_id);
        this.setPost(_post);
        this.setDatetime(_datetime);
        this.setAmount(_amount);
        this.setBalance(_balance);
        this.setCcy_id(_ccy_id);
        this.setOwner(_owner);
        this.setOwner_id(_owner_id);
        this.setOwner_profile_picture(_owner_profile_picture);
        this.setWith(_with);
        this.setWith_id(_with_id);
        this.setWith_profile_picture(_with_profile_picture);
        this.setTx_status(_tx_status);
        this.setTypepost(_typepost);
        this.setTypecaption(_typecaption);
        this.setPrivacy(_privacy);
        this.setNumcomments(_numcomments);
        this.setNumviews(_numviews);
        this.setNumlikes(_numlikes);
        this.setShare(_share);
        this.setComments(_comments);
        this.setLikes(_likes);
        this.setComment_id_1(_comment_id_1);
        this.setFrom_name_1(_from_name_1);
        this.setFrom_profile_picture_1(_from_profile_picture_1);
        this.setReply_1(_reply_1);
        this.setComment_id_2(_comment_id_2);
        this.setFrom_name_2(_from_name_2);
        this.setFrom_profile_picture_2(_from_profile_picture_2);
        this.setReply_2(_reply_2);
        this.setIsLike(_isLike);
    }

    public static List<listHistoryModel> getAll() {
        // This is how you execute a query
        return new Select()
                .all()
                .from(listHistoryModel.class).orderBy("history_id DESC")
                .execute();
    }

    public static void deleteAll() {
        // This is how you execute a query
        new Delete().from(listHistoryModel.class).execute();
        SQLiteUtils.execSql("DELETE FROM SQLITE_SEQUENCE WHERE name='PostHistory';");
    }

    public static void updateNumlikes(String _numlikes, int _id){
        new Update(listHistoryModel.class).set("numlikes = ?", _numlikes).where("history_id = ?", _id).execute();
    }

    public static void updateIsLike(String _islike, int _id){
        new Update(listHistoryModel.class).set("isLike = ?", _islike).where("history_id = ?", _id).execute();
    }

    public static void updateLikes(String _likes, int _id){
        new Update(listHistoryModel.class).set("likes = ?", _likes).where("history_id = ?", _id).execute();
    }

    public static void updateNumcomments(String _numcomments, int _id){
        new Update(listHistoryModel.class).set("numcomments = ?", _numcomments).where("history_id = ?", _id).execute();
    }

    public static void updateComments(String _comments, int _id){
        new Update(listHistoryModel.class).set("comments = ?", _comments).where("history_id = ?", _id).execute();
    }

    public static void updateCommentId1(String _comment_id_1, int _id){
        new Update(listHistoryModel.class).set("comment_id_1 = ?", _comment_id_1).where("history_id = ?", _id).execute();
    }

    public static void updateCommentId2(String _comment_id_2, int _id){
        new Update(listHistoryModel.class).set("comment_id_2 = ?", _comment_id_2).where("history_id = ?", _id).execute();
    }

    public static void updateFromname1(String _from_name_1, int _id){
        new Update(listHistoryModel.class).set("from_name_1 = ?", _from_name_1).where("history_id = ?", _id).execute();
    }

    public static void updateFromname2(String _from_name_2, int _id){
        new Update(listHistoryModel.class).set("from_name_2 = ?", _from_name_2).where("history_id = ?", _id).execute();
    }

    public static void updateFromprofilepicture1(String _from_profile_picture_1, int _id){
        new Update(listHistoryModel.class).set("from_profile_picture_1 = ?", _from_profile_picture_1).where("history_id = ?", _id).execute();
    }

    public static void updateFromprofilepicture2(String _from_profile_picture_2, int _id){
        new Update(listHistoryModel.class).set("from_profile_picture_2 = ?", _from_profile_picture_2).where("history_id = ?", _id).execute();
    }

    public static void updateReply1(String _reply_1, int _id){
        new Update(listHistoryModel.class).set("reply_1 = ?", _reply_1).where("history_id = ?", _id).execute();
    }

    public static void updateReply2(String _reply_2, int _id){
        new Update(listHistoryModel.class).set("reply_2 = ?", _reply_2).where("history_id = ?", _id).execute();
    }

    public String getTx_status() {
        return tx_status;
    }

    private void setTx_status(String tx_status) {
        this.tx_status = tx_status;
    }

    public int getHistory_id() {
        return history_id;
    }

    private void setHistory_id(int history_id) {
        this.history_id = history_id;
    }

    public String getDatetime() {
        return datetime;
    }

    private void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getAmount() {
        return amount;
    }

    private void setAmount(String amount) {
        this.amount = amount;
    }

    public String getBalance() {
        return balance;
    }

    private void setBalance(String balance) {
        this.balance = balance;
    }

    public String getCcy_id() {
        return ccy_id;
    }

    private void setCcy_id(String ccy_id) {
        this.ccy_id = ccy_id;
    }

    public String getNumcomments() {
        return numcomments;
    }

    private void setNumcomments(String numcomments) {
        this.numcomments = numcomments;
    }

    public String getNumlikes() {
        return numlikes;
    }

    private void setNumlikes(String numlikes) {
        this.numlikes = numlikes;
    }

    public String getNumviews() {
        return numviews;
    }

    private void setNumviews(String numviews) {
        this.numviews = numviews;
    }

    public String getOwner() {
        return owner;
    }

    private void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwner_id() {
        return owner_id;
    }

    private void setOwner_id(String owner_id) {
        this.owner_id = owner_id;
    }

    public String getOwner_profile_picture() {
        return owner_profile_picture;
    }

    private void setOwner_profile_picture(String owner_profile_picture) {
        this.owner_profile_picture = owner_profile_picture;
    }

    public String getPost() {
        return post;
    }

    private void setPost(String post) {
        this.post = post;
    }

    public String getPrivacy() {
        return privacy;
    }

    private void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public String getShare() {
        return share;
    }

    private void setShare(String share) {
        this.share = share;
    }

    public String getTypecaption() {
        return typecaption;
    }

    private void setTypecaption(String typecaption) {
        this.typecaption = typecaption;
    }

    public String getTypepost() {
        return typepost;
    }

    private void setTypepost(String typepost) {
        this.typepost = typepost;
    }

    public String getWith() {
        return with;
    }

    private void setWith(String with) {
        this.with = with;
    }

    public String getWith_id() {
        return with_id;
    }

    private void setWith_id(String with_id) {
        this.with_id = with_id;
    }

    public String getWith_profile_picture() {
        return with_profile_picture;
    }

    private void setWith_profile_picture(String with_profile_picture) {
        this.with_profile_picture = with_profile_picture;
    }

    public String getComment_id_1() {
        return comment_id_1;
    }

    private void setComment_id_1(String comment_id_1) {
        this.comment_id_1 = comment_id_1;
    }

    public String getComment_id_2() {
        return comment_id_2;
    }

    private void setComment_id_2(String comment_id_2) {
        this.comment_id_2 = comment_id_2;
    }

    public String getFrom_name_1() {
        return from_name_1;
    }

    private void setFrom_name_1(String from_name_1) {
        this.from_name_1 = from_name_1;
    }

    public String getFrom_name_2() {
        return from_name_2;
    }

    private void setFrom_name_2(String from_name_2) {
        this.from_name_2 = from_name_2;
    }

    public String getFrom_profile_picture_1() {
        return from_profile_picture_1;
    }

    private void setFrom_profile_picture_1(String from_profile_picture_1) {
        this.from_profile_picture_1 = from_profile_picture_1;
    }

    public String getFrom_profile_picture_2() {
        return from_profile_picture_2;
    }

    private void setFrom_profile_picture_2(String from_profile_picture_2) {
        this.from_profile_picture_2 = from_profile_picture_2;
    }

    public String getReply_1() {
        return reply_1;
    }

    private void setReply_1(String reply_1) {
        this.reply_1 = reply_1;
    }

    public String getReply_2() {
        return reply_2;
    }

    private void setReply_2(String reply_2) {
        this.reply_2 = reply_2;
    }

    public String getComments() {
        return comments;
    }

    private void setComments(String comments) {
        this.comments = comments;
    }

    public String getIsLike() {
        return isLike;
    }

    private void setIsLike(String isLike) {
        this.isLike = isLike;
    }

    public String getLikes() {
        return likes;
    }

    private void setLikes(String likes) {
        this.likes = likes;
    }
}
