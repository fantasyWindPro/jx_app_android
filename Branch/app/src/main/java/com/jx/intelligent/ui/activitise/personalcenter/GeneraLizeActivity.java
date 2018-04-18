package com.jx.intelligent.ui.activitise.personalcenter;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.jx.intelligent.R;
import com.jx.intelligent.adapter.RecycleAdapter;
import com.jx.intelligent.adapter.RecycleCommonAdapter;
import com.jx.intelligent.adapter.holder.RecycleCommonViewHolder;
import com.jx.intelligent.base.RHBaseActivity;
import com.jx.intelligent.base.RHBaseApplication;
import com.jx.intelligent.constant.Constant;
import com.jx.intelligent.dao.CommunityService;
import com.jx.intelligent.db.DBManager;
import com.jx.intelligent.helper.TitleBarHelper;
import com.jx.intelligent.intf.OnItemClickListener;
import com.jx.intelligent.intf.ResponseResult;
import com.jx.intelligent.listener.OnRecycleScrollListener;
import com.jx.intelligent.result.GeneraLizeResult;
import com.jx.intelligent.ui.activitise.service.ServiceDetailActivity;
import com.jx.intelligent.util.LogUtil;
import com.jx.intelligent.util.SesSharedReferences;
import com.jx.intelligent.util.StringUtil;
import com.jx.intelligent.util.ToastUtil;
import com.jx.intelligent.util.UIUtil;
import com.jx.intelligent.util.Utils;
import com.jx.intelligent.view.SpaceItemDecoration;
import com.jx.intelligent.view.dialog.ProgressWheelDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 王云 on 2017/6/29 0029.
 * 我的推广页面
 */

public class GeneraLizeActivity extends RHBaseActivity {
    private ImageView btn_add_ad;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mManager;
    private RecycleCommonAdapter mAdapter;
    private TitleBarHelper titleBarHelper;
    private int page = 1;
    private boolean isFresh;
    private CommunityService communityService;
    private List<GeneraLizeResult.DataBean> datas = new ArrayList<GeneraLizeResult.DataBean>();

    private ProgressWheelDialog dialog;
    private DBManager dbManager;
    private String userId;
    private List<GeneraLizeResult.DataBean> moreDatas;

    @Override
    protected void init() {
        dbManager = new DBManager(RHBaseApplication.getInstance().getApplicationContext());
        dbManager.copyDBFile();
        SesSharedReferences sp = new SesSharedReferences();
        userId = sp.getUserId(UIUtil.getContext());
        getServices(page);
    }

    @Override
    protected int setContentLayout() {
        return R.layout.activity_service_list;
    }

    @Override
    protected void initTitle() {
        titleBarHelper = new TitleBarHelper(GeneraLizeActivity.this);
        titleBarHelper.setLeftImageRes(R.drawable.selector_back);
        titleBarHelper.setLeftClickListener(this);
        titleBarHelper.setMiddleClickListener(this);
    }

    @Override
    protected void findView(View contentView) {
        mSwipeRefreshLayout = (SwipeRefreshLayout) contentView.findViewById(R.id.srl);
        contentView.findViewById(R.id.layout_banner).setVisibility(View.GONE);
        mRecyclerView = (RecyclerView) contentView.findViewById(R.id.rcy);
        btn_add_ad = (ImageView) contentView.findViewById(R.id.btn_add_ad);
        btn_add_ad.setVisibility(View.GONE);
        ((LinearLayout) contentView.findViewById(R.id.refresh_empty_view_ll_no_refresh)).setVisibility(View.GONE);
        mManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mManager);
        //设置条目间隔
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(1));
        //渐变颜色
        mSwipeRefreshLayout.setColorSchemeColors(UIUtil.getColor(R.color.color_15aa5a), UIUtil.getColor(R.color.color_ff0000));
        titleBarHelper.setMiddleTitleText("我的推广");
        btn_add_ad.setOnClickListener(this);
        communityService = new CommunityService();
        dialog = new ProgressWheelDialog(GeneraLizeActivity.this);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.titlebar_left_rl:
                finish();
                break;
        }
    }

    private void initData() {

        mAdapter = new RecycleAdapter<GeneraLizeResult.DataBean>(UIUtil.getContext(), datas, true, R.layout.layout_service_item, mSwipeRefreshLayout) {
            @Override
            public void convert(RecycleCommonViewHolder holder, final GeneraLizeResult.DataBean data) {
                String [] urls = null;
                if(!StringUtil.isEmpty(data.getUrl()))
                {
                    urls = data.getUrl().split(",");
                }

                if(urls != null && urls.length > 0)
                {
                    holder.setImageByUrl(R.id.my_img, urls[0], R.id.loading_anim_iv, R.mipmap.small_empty_img);
                }

                holder.setText(R.id.tv_title, data.getSeller());
                holder.setText(R.id.tv_date, data.getPub_addtime());
                holder.setText(R.id.tv_content, data.getContent());
                holder.setText(R.id.tv_addr, data.getAddress());

                holder.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        LogUtil.e("点击了条目");
                        Intent intent = new Intent(GeneraLizeActivity.this, ServiceDetailActivity.class);
                        intent.putExtra("title", data.getContent());
                        intent.putExtra("Pub_id", data.getPubid());
                        startActivity(intent);
                    }
                });
            }



            @Override
            protected void opHeader(RecycleCommonViewHolder holder) {
                holder.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        LogUtil.e("点击了头");
                    }
                });
            }
        };

        if(datas.size()<10)
        {
            mAdapter.setHavefooter(false);
            mAdapter.setmLoadFinish(true);
        }
        else
        {
            mAdapter.setHavefooter(true);
            mAdapter.setmLoadFinish(false);
        }

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new OnRecycleScrollListener(mAdapter, (LinearLayoutManager) mManager) {
            @Override
            protected void loadMore() {
                if (!mAdapter.isLoadFinish()) { //有更多数据
                    mSwipeRefreshLayout.setEnabled(false); //正在加载更多,不可下拉刷新
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    isFresh = false;
                                    page += 1;
                                    getServices(page);
                                }
                            });
                        }
                    }).start();
                }
            }
        });
        //下拉刷新
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(mAdapter != null)
                                {
                                    mAdapter.setHavefooter(true);
                                    mAdapter.setmLoadFinish(false);
                                }
                                isFresh = true;
                                page = 1;
                                getServices(page);
                            }
                        });
                    }
                }).start();
            }
        });
    }

    void getServices(final int page)
    {
        getPreServicesData();
        if(!Utils.isNetworkAvailable(RHBaseApplication.getInstance().getApplicationContext()))
        {
            if(isFresh)
            {
                mSwipeRefreshLayout.setRefreshing(false);//刷新完成
            }
            else
            {
                mSwipeRefreshLayout.setEnabled(true);
            }

            if(datas.size() <=0 && moreDatas.size() <= 0)
            {
                mDiffViewHelper.showEmptyView();
            }
            else
            {
                mDiffViewHelper.showDataView();
            }
            return;
        }

        dialog.show();
        communityService.GeneraLizeTask( userId,page+"", new ResponseResult() {
            @Override
            public void resSuccess(Object object) {
                dialog.dismiss();
                GeneraLizeResult getGeneraLizeResult = (GeneraLizeResult) object;
                ShowGeneraLizeData(getGeneraLizeResult);
            }

            @Override
            public void resFailure(String message) {
                dialog.dismiss();
                ToastUtil.showToast(message);
                if(datas.size() <=0 && moreDatas.size() <= 0)
                {
                    mDiffViewHelper.showEmptyView();
                }
                else
                {
                    mDiffViewHelper.showDataView();
                }
            }
        });
    }

    /**
     * 获取缓存下来的我的发布的数据
     */
    void getPreServicesData()
    {
        if(page == 1)
        {
            Map<String, String> map = new HashMap<String, String>();
            map.put("phoneNum", SesSharedReferences.getPhoneNum(GeneraLizeActivity.this));
            map.put("page", "1");

            String js = dbManager.getUrlJsonData(Constant.MY_GENERALIZE + StringUtil.obj2JsonStr(map));
            if(!StringUtil.isEmpty(js))
            {
                GeneraLizeResult getGeneraLizeResult = new Gson().fromJson(js, GeneraLizeResult.class);
                ShowGeneraLizeData(getGeneraLizeResult);
                if(getGeneraLizeResult != null && getGeneraLizeResult.getData().size()>0)
                {
                    isFresh = true;
                }
            }
        }
    }

    /**
     * 显示我的发布的数据
     * @param generaLizeResult
     */
    void ShowGeneraLizeData(GeneraLizeResult generaLizeResult)
    {
        if(generaLizeResult != null)
        {
            moreDatas = generaLizeResult.getData();
            if(datas.size() <=0 && this.moreDatas.size() <= 0)
            {
                mDiffViewHelper.showEmptyView();
            }
            else {
                mDiffViewHelper.showDataView();
                if (page == 1) {
                    if (isFresh) {
                        datas.clear();
                        mSwipeRefreshLayout.setRefreshing(false);//刷新完成
                        datas.addAll(this.moreDatas);
                        if(mAdapter != null)
                        {
                            mAdapter.notifyDataSetChanged();
                            if (this.moreDatas.size() < 10) {
                                mAdapter.setHavefooter(false);
                                mAdapter.setmLoadFinish(true);
                            } else {
                                mAdapter.setHavefooter(true);
                                mAdapter.setmLoadFinish(false);
                            }
                        }
                    } else {
                        datas = this.moreDatas;
                        initData();
                    }
                } else {
                    mSwipeRefreshLayout.setEnabled(true);
                    if(mAdapter != null) {
                        mAdapter.loadMore(this.moreDatas); //数据长度为0,表示没有更多数据
                        mAdapter.notifyDataSetChanged();
                        if (this.moreDatas.size() < 10) {
                            mAdapter.setHavefooter(false);
                            mAdapter.setmLoadFinish(true);
                        } else {
                            mAdapter.setHavefooter(true);
                            mAdapter.setmLoadFinish(false);
                        }
                    }
                }
            }
        }
        else
        {
            if(datas.size() <=0 && moreDatas.size() <= 0)
            {
                mDiffViewHelper.showEmptyView();
            }
        }
    }

    @Override
    public void emptyRetryRefreshListener() {
        super.emptyRetryRefreshListener();
        page = 1;
        getServices(page);
    }
}