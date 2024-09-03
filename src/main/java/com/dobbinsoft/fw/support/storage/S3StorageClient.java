package com.dobbinsoft.fw.support.storage;


import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.Base64;
import com.amazonaws.util.DateUtils;
import com.dobbinsoft.fw.support.utils.StringUtils;
import org.springframework.beans.factory.InitializingBean;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class S3StorageClient implements StorageClient, InitializingBean {

    protected AmazonS3 s3Client;

    public abstract String getAccessKeyId();
    public abstract String getAccessKeySecret();
    public abstract String getBucketName();
    public String getRuntimeBucketName() {
        return getBucketName();
    }
    public abstract String getBaseUrl();
    public abstract String getEndpoint();


    public AmazonS3 getS3Client() {
        return s3Client;
    }

    @Override
    public abstract void afterPropertiesSet() throws Exception;

    @Override
    public StorageInfoResult info(String key) {
        ObjectMetadata objectMetadata = s3Client.getObjectMetadata(getRuntimeBucketName(), key);
        StorageInfoResult storageInfoResult = new StorageInfoResult();
        storageInfoResult.setKey(key);
        if (objectMetadata == null) {
            storageInfoResult.setExist(false);
            return storageInfoResult;
        }
        storageInfoResult.setExist(true);
        storageInfoResult.setContentLength(objectMetadata.getContentLength());
        storageInfoResult.setContentType(objectMetadata.getContentType());
        return storageInfoResult;
    }

    @Override
    public StorageResult save(StorageRequest request) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(request.getSize());
        metadata.setContentType(request.getContentType());
        PutObjectRequest putRequest = new PutObjectRequest(getRuntimeBucketName(), request.getPath() + "/" + request.getFilename(), request.getIs(), metadata);
        s3Client.putObject(putRequest);
        StorageResult storageResult = new StorageResult();
        storageResult.setSuc(true);
        storageResult.setUrl(getBaseUrl() + "/" + request.getPath() + "/" + request.getFilename());
        return storageResult;
    }

    @Override
    public StoragePrivateResult savePrivate(StorageRequest request) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(request.getSize());
        metadata.setContentType(request.getContentType());

        PutObjectRequest putRequest = new PutObjectRequest(
                getRuntimeBucketName(),
                request.getPath() + "/" + request.getFilename(),
                request.getIs(),
                metadata
        );

        // 设置 ACL 为私有读写
        putRequest.setCannedAcl(CannedAccessControlList.Private);

        s3Client.putObject(putRequest);

        StoragePrivateResult storageResult = new StoragePrivateResult();
        storageResult.setSuc(true);
        storageResult.setKey(request.getPath() + "/" + request.getFilename());
        storageResult.setUrl(getPrivateUrl(storageResult.getKey(), 300));

        return storageResult;
    }

    @Override
    public boolean delete(String url) {
        String keyFormUrl = getKeyFormUrl(url);
        s3Client.deleteObject(getRuntimeBucketName(), keyFormUrl);
        return true;
    }

    @Override
    public boolean deletePrivate(String key) {
        s3Client.deleteObject(getRuntimeBucketName(), key);
        return true;
    }

    public static class ImageProcessParams {
        private String key;
        private Map<String, String> params;
    }

    public static ImageProcessParams parseImageProcessParams(String url) {
        ImageProcessParams imageProcessParams = new ImageProcessParams();
        // 提取参数部分
        String[] split = url.split("\\?");
        Map<String, String> kvs = new HashMap<>();
        if (split.length > 1) {
            String query = split[1];
            String[] params = query.split("&");

            for (String param : params) {
                String[] kv = param.split("=");
                kvs.put(kv[0], kv[1]);
            }
        }
        imageProcessParams.params = kvs;
        imageProcessParams.key = split[0];
        return imageProcessParams;
    }

    @Override
    public String getPrivateUrl(String key, Integer expireSec) {
        ImageProcessParams imageProcessParams = parseImageProcessParams(key);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(getRuntimeBucketName(), imageProcessParams.key)
                .withExpiration(new Date(System.currentTimeMillis() + expireSec * 1000));
        imageProcessParams.params.forEach(request::addRequestParameter);
        URL url = s3Client.generatePresignedUrl(request);
        // 替换为自定义域名
        return url.toString();
    }

    @Override
    public String getKeyFormUrl(String url) {
        if (url.startsWith("http")) {
            url = url.replace("http://", "").replace("https://", "");
            int index = url.indexOf("/");
            String substring = url.substring(index + 1);
            int endIndex = substring.indexOf("?");
            if (endIndex > 0) {
                return substring.substring(0, endIndex);
            } else {
                return substring;
            }
        } else {
            return url;
        }
    }

    @Override
    public boolean delPath(String path) {
        String nextMarker = null;
        do {
            StorageListRequest request = new StorageListRequest();
            request.setRows(500);
            request.setPrefix(path);
            request.setNextMarker(nextMarker);
            StorageListResult storageListResult = listKeys(request);
            List<StorageListResult.Item> items = storageListResult.getItems();
            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(getRuntimeBucketName());
            List<DeleteObjectsRequest.KeyVersion> keys = items
                    .stream()
                    .map(item -> new DeleteObjectsRequest.KeyVersion(item.getKey()))
                    .collect(Collectors.toList());
            deleteObjectsRequest.setKeys(keys);
            s3Client.deleteObjects(deleteObjectsRequest);
            nextMarker = storageListResult.getMarker();
        } while (StringUtils.isNotEmpty(nextMarker));
        return false;
    }

    @Override
    public StorageListResult listKeys(StorageListRequest request) {
        ListObjectsV2Request listRequest = new ListObjectsV2Request()
                .withBucketName(getRuntimeBucketName())
                .withPrefix(request.getPrefix())
                .withDelimiter(request.getNextMarker())
                .withMaxKeys(request.getRows());
        ListObjectsV2Result result = s3Client.listObjectsV2(listRequest);
        StorageListResult storageListResult = new StorageListResult();
        List<S3ObjectSummary> objectSummaries = result.getObjectSummaries();
        List<StorageListResult.Item> keys = objectSummaries.stream().map(objectSummary -> {
            StorageListResult.Item item = new StorageListResult.Item();
            item.setKey(objectSummary.getKey());
            item.setSize(objectSummary.getSize());
            return item;
        }).collect(Collectors.toList());
        storageListResult.setItems(keys);
        storageListResult.setMarker(result.getDelimiter());
        return storageListResult;
    }

    @Override
    public String getPresignedUrl(String key, String method, Integer expireSec) {
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(getRuntimeBucketName(), key)
                .withMethod(HttpMethod.valueOf(method))
                .withExpiration(new Date(System.currentTimeMillis() + expireSec * 1000));

        URL url = s3Client.generatePresignedUrl(request);
        return url.toString();
    }

    @Override
    public PresignedPostResult getPresignedUrlPost(String objectKey, Integer expireSec) {
        Date expiration = new Date(System.currentTimeMillis() + expireSec * 1000);
        String iso8601Expiration = DateUtils.formatISO8601Date(expiration);

        // Policy conditions
        String policy = "{\n" +
                "  \"expiration\": \"" + iso8601Expiration + "\",\n" +
                "  \"conditions\": [\n" +
                "    {\"bucket\": \"" + getBucketName() + "\"},\n" +
                "    [\"starts-with\", \"$key\", \"" + objectKey + "\"]\n" +
                "  ]\n" +
                "}";

        String base64Policy = Base64.encodeAsString(policy.getBytes(StandardCharsets.UTF_8));
        String signature = null;
        try {
            signature = signPolicy(base64Policy);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        PresignedPostResult result = new PresignedPostResult();
        result.setPolicy(base64Policy);
        result.setSignature(signature);
        result.setBucket(getRuntimeBucketName());
        result.setKey(objectKey);
        result.setAccessKeyId(getAccessKeyId());
        result.setUrl("https://" + getBucketName() + "." + getEndpoint());
        return result;
    }

    private String signPolicy(String base64Policy) throws InvalidKeyException, NoSuchAlgorithmException {
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(getAccessKeySecret().getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
        byte[] rawHmac = mac.doFinal(base64Policy.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeAsString(rawHmac);
    }

    @Override
    public InputStream download(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(getRuntimeBucketName(), key);
        S3Object s3Object = s3Client.getObject(getObjectRequest);
        return s3Object.getObjectContent();
    }
}
