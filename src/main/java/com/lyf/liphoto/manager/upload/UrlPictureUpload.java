package com.lyf.liphoto.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.lyf.liphoto.exception.BusinessException;
import com.lyf.liphoto.exception.ErrorCode;
import com.lyf.liphoto.exception.ThrowUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Service
public class UrlPictureUpload extends PictureUploadTemplate {  
    @Override  
    protected void validPicture(Object inputSource) {  
        String fileUrl = (String) inputSource;  
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址不能为空");
        // ... 跟之前的校验逻辑保持一致
        try {
            //1.验证URL格式：验证是否是合法的URL
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
        }
        //2.校验URL协议
        ThrowUtils.throwIf(!(fileUrl.startsWith("http://") || fileUrl.startsWith("https://")),
                ErrorCode.PARAMS_ERROR, "仅支持HTTP或HTTPS协议的文件地址");
        //3.发送HEAD请求以验证文件是否存在
        HttpResponse response = null;
        try {
            response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            //未正常返回，无需执行其他判断
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                return;
            }
            //4.校验文件类型
            String contentType = response.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)) {
                //允许的图片类型
                final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType.toLowerCase()),
                        ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
            // 5. 校验文件大小
            String contentLengthStr = response.header("Content-Length");
            if (StrUtil.isNotBlank(contentLengthStr)) {
                try {
                    long contentLength = Long.parseLong(contentLengthStr);
                    final long TWO_MB = 2 * 1024 * 1024L; // 限制文件大小为 2MB
                    ThrowUtils.throwIf(contentLength > TWO_MB, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2M");
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式错误");
                }
            }
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }  
  
    @Override  
    protected String getOriginFilename(Object inputSource) {  
        String fileUrl = (String) inputSource;  
        // 从 URL 中提取文件名 getName(存储的文件名有后缀), mainName(存储的文件名没有后缀)
        return FileUtil.getName(fileUrl);
    }  
  
    @Override  
    protected void processFile(Object inputSource, File file) throws Exception {
        String fileUrl = (String) inputSource;  
        // 下载文件到临时目录  
        HttpUtil.downloadFile(fileUrl, file);
    }  
}
