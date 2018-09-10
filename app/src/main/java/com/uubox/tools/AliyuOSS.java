package com.uubox.tools;

import android.content.Context;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;

public class AliyuOSS {

    public void uploadFileToOSS(final Context context, String bucket, String ossfilepath, byte[] buff, OSSCompletedCallback<PutObjectRequest, PutObjectResult> completedCallback) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, ossfilepath, buff);
        exe(context, putObjectRequest, completedCallback);
    }

    public void uploadFileToOSS(final Context context, String bucket, String ossfilepath, String srcfilepath, OSSCompletedCallback<PutObjectRequest, PutObjectResult> completedCallback) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, ossfilepath, srcfilepath);
        exe(context, putObjectRequest, completedCallback);
    }

    public void uploadFilesToOSS(final Context context, String bucket, String[] osspaths, String[] srcfilepaths, OSSCompletedCallback<PutObjectRequest, PutObjectResult> completedCallback) {
        for (int i = 0; i < osspaths.length; i++) {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, osspaths[i], srcfilepaths[i]);
            exe(context, putObjectRequest, completedCallback);
        }
    }

    private void exe(final Context context, PutObjectRequest putObjectRequest, OSSCompletedCallback<PutObjectRequest, PutObjectResult> completedCallback) {
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(5 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(5 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求数，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次


        OSSCredentialProvider provider = new OSSPlainTextAKSKCredentialProvider("LTAILkfXHbE0tEf4", "pnezrct5QOytMKa52mnGDDhx1StUYW");
        OSSClient ossClient = new OSSClient(context, "oss-cn-shenzhen.aliyuncs.com", provider, conf);

        ossClient.asyncPutObject(putObjectRequest, completedCallback);
    }

}
