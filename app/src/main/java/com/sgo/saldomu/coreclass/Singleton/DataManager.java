package com.sgo.saldomu.coreclass.Singleton;

import com.sgo.saldomu.Beans.BankDataTopUp;
import com.sgo.saldomu.Beans.DenomListModel;
import com.sgo.saldomu.Beans.SCADMCommunityModel;
import com.sgo.saldomu.Beans.listBankModel;
import com.sgo.saldomu.models.InvoiceDGI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataManager {

    private static DataManager singleton;

    private SCADMCommunityModel item;
    private ArrayList<DenomListModel> itemList;
    private ArrayList<listBankModel> bankData;
    private BankDataTopUp temp_other_atm;
    private List<InvoiceDGI> listInvoice;
    private HashMap<String, Object> invoiceParam;

    public static DataManager getInstance(){
        if (singleton == null){
            singleton = new DataManager();
        }
        return singleton;
    }

    public void setSCADMCommMod(SCADMCommunityModel item){
        this.item = item;
    }

    public SCADMCommunityModel getSACDMCommMod(){
        return item;
    }

    public ArrayList<DenomListModel> getItemList() {
        return itemList;
    }

    public void setItemList(ArrayList<DenomListModel> orderList) {
        this.itemList = orderList;
    }

    public ArrayList<listBankModel> getBankData() {
        return bankData;
    }

    public void setBankData(ArrayList<listBankModel> bankData) {
        this.bankData = bankData;
    }

    public BankDataTopUp getTemp_other_atm() {
        return temp_other_atm;
    }

    public void setTemp_other_atm(BankDataTopUp temp_other_atm) {
        this.temp_other_atm = temp_other_atm;
    }

    public List<InvoiceDGI> getListInvoice() {
        return listInvoice;
    }

    public void setListInvoice(List<InvoiceDGI> listInvoice) {
        this.listInvoice = listInvoice;
    }

    public HashMap<String, Object> getInvoiceParam() {
        return invoiceParam;
    }

    public void setInvoiceParam(HashMap<String, Object> invoiceParam) {
        this.invoiceParam = invoiceParam;
    }

//    public void logoutMessage(){
//        AlertDialogLogout test = AlertDialogLogout.getInstance();
//        test.showDialoginMain(MainPage.this, message);
//    }
//
//    public void logout(){
//
//        try {
//
//            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_LOGOUT);
////            RequestParams params = MyApiClient.getInstance().getSignatureWithParams(MyApiClient.LINK_LOGOUT);
//            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
//            params.put(WebParams.USER_ID, userPhoneID);
//
//            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_LOGOUT, params
//                    , new ResponseListener() {
//                        @Override
//                        public void onResponses(JsonObject object) {
//                            jsonModel model = RetrofitService.getInstance().getGson().fromJson(object, jsonModel.class);
//
//                            if (model.getError_code().equals(WebParams.SUCCESS_CODE)) {
//                                //stopService(new Intent(MainPage.this, UpdateLocationService.class));
//                                Logout(FIRST_SCREEN_INTRO);
//
//                            } else {
//                                Toast.makeText(MainPage.this, model.getError_message(), Toast.LENGTH_LONG).show();
//                            }
//                        }
//
//                        @Override
//                        public void onError(Throwable throwable) {
//                        }
//
//                        @Override
//                        public void onComplete() {
//                        }
//                    });
//        } catch (Exception e) {
//            Timber.d("httpclient:" + e.getMessage());
//        }
//    }
}
