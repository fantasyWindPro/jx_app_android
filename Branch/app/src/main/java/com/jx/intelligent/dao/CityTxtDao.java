package com.jx.intelligent.dao;

import android.util.Log;

import com.jx.intelligent.base.RHBaseApplication;
import com.jx.intelligent.constant.Constant;
import com.jx.intelligent.db.DBManager;
import com.jx.intelligent.http.callback.HttpResponseCallback;
import com.jx.intelligent.intf.ResponseResult;
import com.jx.intelligent.result.CityTxetResult;
import com.jx.intelligent.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 王云 on 2017/5/19 0019.
 */

public class CityTxtDao extends BaseDao {
    DBManager dbManager;

    {
        dbManager = new DBManager(RHBaseApplication.getInstance().getApplicationContext());
        dbManager.copyDBFile();
    }

    /**
     *
     *  获取城市用户数量的网络请求
     * @param responseResult
     */
    public void getUsernuberTask(String city ,final ResponseResult responseResult) {

        final Map<String, String> map = new HashMap<String, String>();
        map.put("city", city);

        sendAsyncRequest(Constant.USER_NUMBER, com.alibaba.fastjson.JSON.toJSONString(map), CityTxetResult.class, new HttpResponseCallback<CityTxetResult>() {
            @Override
            public void onFailure(int statusCode, String message, CityTxetResult cityTxetResult) {
                responseResult.resFailure(message);

            }

            @Override
            public void onSuccess(int statusCode, CityTxetResult cityTxetResult) {
                Log.e("我走到这里了","111111");
                if (cityTxetResult.getResult().equals(Constant.retCode_ok)) {
                    responseResult.resSuccess(cityTxetResult);

                    if(StringUtil.isEmpty(dbManager.getUrlJsonData(Constant.USER_NUMBER + StringUtil.obj2JsonStr(map))))
                    {
                        dbManager.insertUrlJsonData(Constant.USER_NUMBER + StringUtil.obj2JsonStr(map), StringUtil.obj2JsonStr(cityTxetResult));
                    }
                    else
                    {
                        dbManager.updateUrlJsonData(Constant.USER_NUMBER + StringUtil.obj2JsonStr(map), StringUtil.obj2JsonStr(cityTxetResult));
                    }
                } else {
                    responseResult.resFailure(cityTxetResult.getMsg());

                }
                    }


        });
    }

}
