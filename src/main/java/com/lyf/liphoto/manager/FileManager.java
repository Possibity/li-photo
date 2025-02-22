package com.lyf.liphoto.manager;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
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
@Service
@Slf4j
public class FileManager {
    @Resource
    private CosClientConfig cosClientConfig;
    @Resource
    private CosManager cosManager;

    /**
     * 上传图片
     * @param multipartFile 文件
     * @param uploadPathPrefix 上传路径前缀
     * @return
     */

    public UploadPictureResult uploadPicture(MultipartFile multipartFile,String uploadPathPrefix){
        //校验图片
        validPicture(multipartFile);
        //图片上传地址
        String uuid= RandomUtil.randomString(16);
        String originalFilename = multipartFile.getOriginalFilename();
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath=String.format("/%s/%s",uploadPathPrefix,uploadFilename);
        File file=null;
        try {
            //创建临时文件
            file=File.createTempFile(uploadPath,null);
            multipartFile.transferTo(file);
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
     * @param multipartFile 文件
     */
    public void validPicture(MultipartFile multipartFile){
        ThrowUtils.throwIf(multipartFile==null, ErrorCode.PARAMS_ERROR,"文件不能为空");
        //1.校验文件大小
        long fileSize=multipartFile.getSize();
        final long ONE_M=1024*1024L;
        /*if(fileSize>2*ONE_M){
            ResultUtils.error(ErrorCode.PARAMS_ERROR,"文件不能超过2M");
        }*/
        ThrowUtils.throwIf(fileSize>2*ONE_M,ErrorCode.PARAMS_ERROR,"文件不能超过2M");
        //2.校验文件后缀
        String suffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        //3.允许上传的文件后缀
        final List<String> ALLOW_FORMAT_LIST= Arrays.asList("jpeg","png","jpg","webp","svg");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(suffix),ErrorCode.PARAMS_ERROR,"文件后缀名不符");
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
