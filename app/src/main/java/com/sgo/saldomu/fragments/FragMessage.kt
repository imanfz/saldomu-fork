package com.sgo.saldomu.fragments

import `in`.srain.cube.views.ptr.PtrFrameLayout
import `in`.srain.cube.views.ptr.PtrHandler
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ItemDecoration
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.gson.JsonObject
import com.sgo.saldomu.Beans.NotificationModelClass
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.MainPage
import com.sgo.saldomu.activities.NotificationActivity
import com.sgo.saldomu.adapter.NotificationListAdapter
import com.sgo.saldomu.coreclass.*
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.interfaces.ResponseListener
import com.sgo.saldomu.models.retrofit.NotifModel
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.empty_notification.*
import kotlinx.android.synthetic.main.frag_notification.*
import org.json.JSONArray
import org.json.JSONObject
import org.ocpsoft.prettytime.PrettyTime
import timber.log.Timber
import java.util.*

class FragMessage : BaseFragment() {

    private val mLayoutManager = LinearLayoutManager(activity)
    private var mAdapter: NotificationListAdapter? = null
    private var mData: ArrayList<NotificationModelClass>? = null
    private var mDataNotifDetail: ArrayList<JSONObject>? = null
    private var tempMData: NotificationModelClass? = null

    private var memberId: String? = null
    private var userId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_notification, container, false)
        return v
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sp = CustomSecurePref.getInstance().getmSecurePrefs()
        memberId = sp.getString(DefineValue.MEMBER_ID, "")
        userId = sp.getString(DefineValue.USERID_PHONE, "")
        accessKey = sp.getString(DefineValue.ACCESS_KEY, "")

        empty_layout.visibility = View.GONE
        btnRefresh.setOnClickListener { sentRetrieveAll() }
        rotate_header_list_view_frame.setPtrHandler(object : PtrHandler {
            override fun onRefreshBegin(frame: PtrFrameLayout?) {
                sentRetrieveAll()
            }

            override fun checkCanDoRefresh(frame: PtrFrameLayout?, content: View?, header: View?): Boolean {
                return canScroolUp()
            }
        })

        mLayoutManager.orientation = LinearLayoutManager.VERTICAL
        notification_recycle_list.layoutManager = mLayoutManager
        val itemDecoration: ItemDecoration = DividerItemDecoration(activity, null)
        notification_recycle_list.addItemDecoration(itemDecoration)
        notification_recycle_list.setHasFixedSize(true)
        notification_recycle_list.itemAnimator = DefaultItemAnimator()
        mData = ArrayList()
        mDataNotifDetail = ArrayList()
        mAdapter = NotificationListAdapter(activity, mData, object : NotificationListAdapter.OnItemClickListener {
            override fun onItemClickView(view: View, position: Int, isLongClick: Boolean) {
//                notificationItemClickAction(position)
            }

            override fun onItemBtnAccept(view: View, position: Int, isLongClick: Boolean) {
//                notificationItemClickAction(position)
            }

            override fun onItemBtnClaim(view: View, position: Int, isLongClick: Boolean) {
//                notificationItemClickAction(position)
            }
        })
        notification_recycle_list.adapter = mAdapter
        notification_recycle_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                for (i in 0 until notification_recycle_list.childCount) {
                    tempMData = mData!![i]
                    if (tempMData!!.notif_type == NotificationActivity.TYPE_DECLINE) {
                        if (notification_recycle_list.getChildAt(i).visibility == View.VISIBLE) {
                            if (!tempMData!!.isRead) {
                                Timber.d("on item visible idx position")
                            }
                        }
                    }
                }
            }
        })
        sentRetrieveAll()
        activity!!.setResult(MainPage.RESULT_NORMAL)
    }

    private fun sentRetrieveAll() {
        showProgressDialog()

        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_NOTIF_RETRIEVE_ALL)
        params[WebParams.USER_ID] = userId
        params[WebParams.MEMBER_ID] = memberId
        params[WebParams.COMM_ID] = MyApiClient.COMM_ID
        params[WebParams.DATE_TIME] = DateTimeFormat.getCurrentDateTime()
        params[WebParams.MEMBER_CREATED] = sp.getString(DefineValue.MEMBER_CREATED, "")

        Timber.d("isi params Retrieve Notif All:$params")
        RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_NOTIF_RETRIEVE_ALL, params,
                object : ResponseListener {
                    override fun onResponses(`object`: JsonObject?) {
                        dismissProgressDialog()

                        val model = getGson().fromJson(`object`, NotifModel::class.java)

                        val code = model.error_code
                        if (code == WebParams.SUCCESS_CODE) {

                            notification_recycle_list.visibility = View.VISIBLE
                            empty_layout.visibility = View.GONE

                            val mArrayData = JSONArray(getGson().toJson(model.data_user_notif))

                            var title: String?
                            var detail: String?
                            var time: String?
                            var toId: String?
                            var fromName: String? = ""
                            var fromId: String?
                            var notifId: String?
                            var fromProfilePicture: String? = ""
                            var dateTime: String?
                            var idResult: String?
                            var notifType: Int
                            var image = 0
                            var read: Boolean
                            var time1: Date
                            val p = PrettyTime(Locale(DefineValue.sDefSystemLanguage))
                            var mObject: JSONObject
                            var mObj: NotificationModelClass
                            mData!!.clear()
                            if (mArrayData.length() > 0) {
                                for (i in 0 until mArrayData.length()) {
                                    mObject = mArrayData.getJSONObject(i)
                                    notifId = mObject.getString(WebParams.NOTIF_ID)
                                    notifType = mObject.getInt(WebParams.NOTIF_TYPE)
                                    fromId = mObject.getString(WebParams.FROM_USER_ID)
                                    toId = mObject.getString(WebParams.TO_USER_ID)
                                    dateTime = mObject.getString(WebParams.CREATED_DATE)
                                    read = mObject.getInt(WebParams.NOTIF_READ) == 1
                                    idResult = mObject.getString(WebParams.ID_RESULT)
                                    title = mObject.getString(WebParams.DESCRIPTION)
                                    detail = mObject.getString(WebParams.SHORT_DESCRIPTION)
                                    image = R.drawable.ic_logo_inbox
                                    time1 = DateTimeFormat.convertStringtoCustomDateTime(dateTime)
                                    time = p.formatDuration(time1)
                                    mObj = NotificationModelClass(notifId, image, title, toId, fromName,
                                            fromId, detail, time, notifType, read, fromProfilePicture, dateTime, idResult)
                                    mData!!.add(mObj)
                                }
                            }
                            mData!!.sortWith(Comparator { o1, o2 ->
                                val date1 = DateTimeFormat.convertStringtoCustomDateTime(o1.date_time)
                                val date2 = DateTimeFormat.convertStringtoCustomDateTime(o2.date_time)
                                date2.compareTo(date1)
                            })
                            if (mData!!.size != 0) {
                                mAdapter!!.notifyDataSetChanged()
                            } else {
                                notification_recycle_list.visibility = View.GONE
                                empty_layout.visibility = View.VISIBLE
                                mData!!.clear()
                                mAdapter!!.notifyDataSetChanged()
                                activity!!.setResult(MainPage.RESULT_NOTIF)
                            }
                            activity!!.setResult(MainPage.RESULT_NOTIF)

                        } else if (code == WebParams.LOGOUT_CODE) {
                            val message = model.error_message
                            val test = AlertDialogLogout.getInstance()
                            test.showDialoginActivity(activity, message)
                        } else if (code == DefineValue.ERROR_9333) {
                            Timber.d("isi response app data:" + model.app_data)
                            val appModel = model.app_data
                            val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                            alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                        } else if (code == DefineValue.ERROR_0066) {
                            Timber.d("isi response maintenance:" + `object`.toString())
                            val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                            alertDialogMaintenance.showDialogMaintenance(activity, model.error_message)
                        } else {
                            if (code == "0003") {
                                Toast.makeText(activity, getString(R.string.notifications_empty), Toast.LENGTH_LONG).show()
                                notification_recycle_list.visibility = View.GONE
                                empty_layout.visibility = View.VISIBLE
                                mData!!.clear()
                                mAdapter!!.notifyDataSetChanged()
                                activity!!.setResult(MainPage.RESULT_NOTIF)
                            } else
                                Toast.makeText(activity, model.error_message, Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onError(throwable: Throwable?) {

                    }

                    override fun onComplete() {
                        rotate_header_list_view_frame.refreshComplete()
                    }
                })
    }

//    private fun notificationItemClickAction(position: Int) {
//        val mObj = mData!![position]
//        sentReadNotif(mObj.notif_id, position)
//        activity!!.finish()
//    }

    private fun canScroolUp(): Boolean {
        try {
            return mAdapter?.itemCount == 0 || notification_recycle_list == null || mLayoutManager.findFirstCompletelyVisibleItemPosition() == 0 && notification_recycle_list!!.getChildAt(0).top == 0
        } catch (ex: Exception) {
            Timber.wtf("Exception checkCandoRefresh:" + ex.message)
        }
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity!!.finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

//    private fun sentReadNotif(_notif_id: String, position: Int) {
//        try {
//            extraSignature = _notif_id
//            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_NOTIF_READ, extraSignature)
//            params[WebParams.USER_ID] = userId
//            params[WebParams.NOTIF_ID_READ] = _notif_id
//            params[WebParams.MEMBER_ID] = memberId
//            params[WebParams.COMM_ID] = MyApiClient.COMM_ID
//            params[WebParams.DATE_TIME] = DateTimeFormat.getCurrentDateTime()
//            Timber.d("isi params Read Notif:$params")
//            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_NOTIF_READ, params,
//                    object : ResponseListener {
//                        override fun onResponses(`object`: JsonObject) {
//                            val model = getGson().fromJson(`object`, jsonModel::class.java)
//                            var code = model.error_code
//                            if (code == WebParams.SUCCESS_CODE) {
//                                if (activity != null) {
//                                    mData!![position].isRead = true
//                                    activity!!.setResult(MainPage.RESULT_NOTIF)
//                                    mAdapter!!.notifyItemChanged(position)
//                                    checkNotification()
//                                }
//                            } else if (code == WebParams.LOGOUT_CODE) {
//                                val message = model.error_message
//                                val test = AlertDialogLogout.getInstance()
//                                test.showDialoginActivity(activity, message)
//                            } else if (code == DefineValue.ERROR_9333) {
//                                Timber.d("isi response app data:" + model.app_data)
//                                val appModel = model.app_data
//                                val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
//                                alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
//                            } else if (code == DefineValue.ERROR_0066) {
//                                Timber.d("isi response maintenance:$`object`")
//                                val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
//                                alertDialogMaintenance.showDialogMaintenance(activity, model.error_message)
//                            } else {
//                                code = model.error_code + ":" + model.error_message
//                                Toast.makeText(activity, code, Toast.LENGTH_LONG).show()
//                            }
//                        }
//
//                        override fun onError(throwable: Throwable) {}
//                        override fun onComplete() {}
//                    })
//        } catch (e: java.lang.Exception) {
//            Timber.d("httpclient:" + e.message)
//        }
//    }

//    private fun checkNotification() {
//        val mth: Thread = object : Thread() {
//            override fun run() {
//                val mContext = activity!!.parent
//                if (mContext is MainPage) {
//                    val mNoHand = NotificationHandler(mContext, sp)
//                    mNoHand.sentRetrieveNotif()
//                }
//            }
//        }
//        mth.start()
//    }
}