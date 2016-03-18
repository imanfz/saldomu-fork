package com.sgo.mdevcash.Beans;

import android.os.Parcel;
import android.os.Parcelable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.activeandroid.util.SQLiteUtils;
import com.sgo.mdevcash.coreclass.WebParams;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by yuddistirakiki on 2/15/16.
 */
@Table(name = "Balance")
public class BalanceModel extends Model implements Parcelable {

    public static final String BALANCE_PARCELABLE = "com.sgo.mdevcash.BALANCE_PARCELABLE";

    @Column
    private String amount;

    @Column
    private String ccy_id;

    @Column
    private String remain_limit;

    @Column
    private String period_limit;

    @Column
    private String next_reset;

    public BalanceModel(){
    }

    public BalanceModel(JSONObject mObject){
        this.setAmount(mObject.optString(WebParams.AMOUNT, ""));
        this.setCcy_id(mObject.optString(WebParams.CCY_ID, ""));
        this.setRemain_limit(mObject.optString(WebParams.REMAIN_LIMIT, ""));
        this.setPeriod_limit(mObject.optString(WebParams.PERIOD_LIMIT, ""));
        this.setNext_reset(mObject.optString(WebParams.NEXT_RESET, ""));
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCcy_id() {
        return ccy_id;
    }

    public void setCcy_id(String ccy_id) {
        this.ccy_id = ccy_id;
    }

    public String getRemain_limit() {
        return remain_limit;
    }

    public void setRemain_limit(String remain_limit) {
        this.remain_limit = remain_limit;
    }

    public String getPeriod_limit() {
        return period_limit;
    }

    public void setPeriod_limit(String period_limit) {
        this.period_limit = period_limit;
    }

    public String getNext_reset() {
        return next_reset;
    }

    public void setNext_reset(String next_reset) {
        this.next_reset = next_reset;
    }

    public BalanceModel(Parcel in)
    {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in)
    {
        // We just need to read back each
        // field in the order that it was
        // written to the parcel
        this.amount = in.readString();
        this.ccy_id = in.readString();
        this.remain_limit = in.readString();
        this.period_limit = in.readString();
        this.next_reset = in.readString();
    }


    public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        @Override
        public BalanceModel createFromParcel(Parcel in)
        {
            return new BalanceModel(in);
        }

        @Override
        public Object[] newArray(int size)
        {
            return new BalanceModel[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.amount);
        dest.writeString(this.ccy_id);
        dest.writeString(this.remain_limit);
        dest.writeString(this.period_limit);
        dest.writeString(this.next_reset);
    }

    public static List<BalanceModel> getAll() {
        // This is how you execute a query
        return new Select()
                .all()
                .from(BalanceModel.class)
                .execute();
    }

    public static void updateData(String _amount, String _remain_limit) {
        // This is how you execute a query
        new Update(BalanceModel.class)
                .set("amount,remain_limit",_amount,_remain_limit)
                .where("Id = ?", 1)
                .execute();
    }

    public static void deleteAll() {
        // This is how you execute a query
        new Delete().from(BalanceModel.class).execute();
        SQLiteUtils.execSql("DELETE FROM SQLITE_SEQUENCE WHERE name='Balance';");
    }
}
