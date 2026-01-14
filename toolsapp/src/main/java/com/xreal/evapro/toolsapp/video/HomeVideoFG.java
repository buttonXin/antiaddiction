package com.xreal.evapro.toolsapp.video;

import android.content.Intent;
import android.widget.EditText;

import com.xreal.evapro.toolsapp.base.BaseOLFragment;

public class HomeVideoFG extends BaseOLFragment {

    private EditText mEditText;

    @Override
    protected void addTitleBar(String text) {
        super.addTitleBar("视频功能");
    }

    @Override
    public void initData() {
        addButton("视频-自动读取剪切板", v -> startActivity(new Intent(mActivity, FullVideoAct.class)));


        mEditText = addEditText("输入URL");
        addButton("打开url的视频",  v -> {
            final Intent intent = new Intent(mActivity, FullVideoAct.class);
            intent.putExtra("url", mEditText.getText().toString());
            startActivity(intent);
        });


//        addText("https://upos-sz-estgoss.bilivideo.com/upgcxcode/56/55/967875556/967875556-1-16.mp4?e=ig8euxZM2rNcNbRVhwdVhwdlhWdVhwdVhoNvNC8BqJIzNbfq9rVEuxTEnE8L5F6VnEsSTx0vkX8fqJeYTj_lta53NCM=&nbs=1&platform=html5&oi=1026646042&gen=playurlv3&os=estgoss&og=ali&uipk=5&mid=0&deadline=1768215643&trid=7fa59f19ab0c4c25a0fe0341fb76734h&upsig=c6aacc007b551c5aec4dbeb02d070081&uparams=e,nbs,platform,oi,gen,os,og,uipk,mid,deadline,trid&bvc=vod&nettype=0&bw=389502&agrr=1&buvid=&build=0&dl=0&f=h_0_0&orderid=0,1");
//
//        addText("https://upos-sz-estgcos.bilivideo.com/upgcxcode/26/76/411147626/411147626-1-16.mp4?e=ig8euxZM2rNcNbRVhwdVhwdlhWdVhwdVhoNvNC8BqJIzNbfq9rVEuxTEnE8L5F6VnEsSTx0vkX8fqJeYTj_lta53NCM=&nbs=1&trid=120728a2fa0243f89b4157797d65c93h&oi=1026646042&os=estgcos&uipk=5&platform=html5&mid=0&deadline=1768217404&gen=playurlv3&og=cos&upsig=b08e5c06d8d05ec73ac4be4e521af1ff&uparams=e,nbs,trid,oi,os,uipk,platform,mid,deadline,gen,og&bvc=vod&nettype=0&bw=428006&buvid=&build=0&dl=0&f=h_0_0&agrr=1&orderid=0,1");

    }
}
