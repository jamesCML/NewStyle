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
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.ListObjectsRequest;
import com.alibaba.sdk.android.oss.model.ListObjectsResult;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;

import java.io.InputStream;
import java.util.Arrays;

public class AliyuOSS {
    private OSSClient mOSSClient;

    public AliyuOSS(Context context) {
        OSSCredentialProvider provider = new OSSPlainTextAKSKCredentialProvider("LTAILdzsjmyE6jLD", "qy5RINWPlVivhhCNKrM44kscstD0ZD");
        mOSSClient = new OSSClient(context, "oss-cn-shenzhen.aliyuncs.com", provider, getConfig());
    }

    public void uploadFileToOSS(String bucket, String ossfilepath, byte[] buff, OSSCompletedCallback<PutObjectRequest, PutObjectResult> completedCallback) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, ossfilepath, buff);
        exe(putObjectRequest, completedCallback);
    }

    public void uploadFileToOSS(String bucket, String ossfilepath, String srcfilepath, OSSCompletedCallback<PutObjectRequest, PutObjectResult> completedCallback) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, ossfilepath, srcfilepath);
        exe(putObjectRequest, completedCallback);
    }

    public void uploadFilesToOSS(String bucket, String[] osspaths, String[] srcfilepaths, OSSCompletedCallback<PutObjectRequest, PutObjectResult> completedCallback) {
        for (int i = 0; i < osspaths.length; i++) {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, osspaths[i], srcfilepaths[i]);
            exe(putObjectRequest, completedCallback);
        }
    }

    public ListObjectsResult listOSSFiles(String bucket, String content) {
        ListObjectsRequest listObjects = new ListObjectsRequest(bucket);
        listObjects.setPrefix(content);
        listObjects.setMaxKeys(1000);
        try {
            return mOSSClient.listObjects(listObjects);
        } catch (ClientException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        return null;
    }

    public GetObjectResult getObject(String bucket, String filepath) {
        GetObjectRequest get = new GetObjectRequest(bucket, filepath);
        try {
            return mOSSClient.getObject(get);
        } catch (ClientException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] getObjectBuff(String bucket, String filepath) {
        try {
            GetObjectResult getResult = getObject(bucket, filepath);
            if (getResult == null) {
                return null;
            }
            InputStream inputStream = getResult.getObjectContent();
            byte[] buffer = new byte[2048];
            int len = 0;
            ByteArrayList bufferList = new ByteArrayList();
            while ((len = inputStream.read(buffer)) > 0) {
                bufferList.add(Arrays.copyOfRange(buffer, 0, len));
            }
            return bufferList.all2Bytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isExistFile(String bucket, String filepath) {
        try {
            return mOSSClient.doesObjectExist(bucket, filepath);
        } catch (ClientException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void exe(PutObjectRequest putObjectRequest, OSSCompletedCallback<PutObjectRequest, PutObjectResult> completedCallback) {
        mOSSClient.asyncPutObject(putObjectRequest, completedCallback);
    }

    private ClientConfiguration getConfig() {
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(5 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求数，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        return conf;
    }

}
