package com.lyf.liphoto.manager;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.lyf.liphoto.common.ResultUtils;
import com.lyf.liphoto.config.CosClientConfig;
import com.lyf.liphoto.exception.BusinessException;
import com.lyf.liphoto.exception.ErrorCode;
import com.lyf.liphoto.exception.ThrowUtils;
import com.lyf.liphoto.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * ClassName:FileManager
 * Package: com.lyf.liphoto.manager
 * Description:
 *  图片是否符合要求？需要校验
 *  将图片上传到哪里？需要指定路径
 *  如何解析图片？需要使用数据万象服务
 * @Author 黎云锋
 * @Create 2025/2/21 0:18
 */

/**
 * 文件服务
 * @deprecated 已废弃，改为使用 upload 包的模板方法优化
 */
@Deprecated
@Service
@Slf4j
public class FileManager {
    @Resource
    private CosClientConfig cosClientConfig;
    @Resource
    private CosManager cosManager;

    /**
     * 上传图片
     * @param fileUrl 文件URL地址
     * @param uploadPathPrefix 上传路径前缀
     * @return
     */

    public UploadPictureResult uploadPicture(String fileUrl,String uploadPathPrefix){
        //校验图片
        validPicture(fileUrl);
        //图片上传地址
        String uuid= RandomUtil.randomString(16);
        String originalFilename =FileUtil.getName(fileUrl);
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath=String.format("/%s/%s",uploadPathPrefix,uploadFilename);
        File file=null;
        try {
            //创建临时文件
            file=File.createTempFile(uploadPath,null);
            HttpUtil.downloadFile(fileUrl,file);
            //上传图片
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            //.getCiUploadResult() 获取内容智能（CI）处理结果。CI可能对上传的图片进行了分析或处理（如鉴黄、标签识别等）
            //getOriginalInfo() 从CI结果中提取原始文件的信息。这里指上传的原始图片文件的信息，而非任何处理后的版本（如缩略图）
            //getImageInfo()最终获取原始图片的详细元数据
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            //封装返回结果
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            double picScale = NumberUtil.round((double) picWidth / picHeight, 2).doubleValue();
            uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setUrl(cosClientConfig.getHost()+"/"+uploadPath);
            return uploadPictureResult;
        } catch (Exception e) {
            log.error("图片上传到对象存储失败",e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传失败");
        }finally {
            //删除临时文件
            this.deleteTempFile(file);
        }
    }

    /**
     * 校验文件
     * @param fileUrl 文件URL地址
     */
    public void validPicture(String fileUrl) {
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址不能为空");
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
    public void deleteTempFile(File file){
        if(file==null){
            return ;
        }
        //删除临时文件
        boolean delete = file.delete();
        if(!delete){
            log.error("file delete error,filepath={}",file.getAbsolutePath());
        }
    }
}
