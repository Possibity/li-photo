package com.lyf.liphoto.controller;

import com.lyf.liphoto.annotation.AuthCheck;
import com.lyf.liphoto.common.BaseResponse;
import com.lyf.liphoto.common.ResultUtils;
import com.lyf.liphoto.constant.UserConstant;
import com.lyf.liphoto.exception.BusinessException;
import com.lyf.liphoto.exception.ErrorCode;
import com.lyf.liphoto.manager.CosManager;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.security.PublicKey;

/**
 * ClassName:FileController
 * Package: com.lyf.liphoto.controller
 * Description:
 *
 * @Author 黎云锋
 * @Create 2025/2/20 18:28
 */


@RestController
@Slf4j
@RequestMapping("/file")
public class FileController {
    @Resource
    private CosManager cosManager;
    /**
     * 测试文件上传
     *
     * @param multipartFile
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/upload")
    public BaseResponse<String> testUploadFile(@RequestPart("file")MultipartFile multipartFile){
        //文件目录
        String filename=multipartFile.getOriginalFilename();
        String filepath=String.format("/test/%s",filename);
        File file=null;
        try {
            //上传文件
            file=File.createTempFile(filepath,null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath,file);
            //返回可访问的地址
            return ResultUtils.success(filepath);
        } catch (Exception e) {
            log.error("file upload error,filepath="+filepath,e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传失败");
        }finally {
            if(file!=null){
                //删除临时文件
                boolean delete=file.delete();
                if(!delete){
                    log.error("file delete error,filepath={}",filepath);
                }
            }
        }
    }

    /**
     * 测试文件下载
     * 核心流程是根据路径获取到 COS 文件对象，然后将文件对象转换为文件流，并写入到 Servlet 的 Response 对象中。注意要设置文件下载专属的响应头。
     * @param filepath 文件路径
     * @param response 响应对象
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/download")
    public void testDownloadFile(String filepath, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInput=null;
        try{
            COSObject cosObject = cosManager.getObject(filepath);
            cosObjectInput=cosObject.getObjectContent();
            //处理下载到的流(将文件流转化为字节流）
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            //设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition","attachment;filename="+filepath);
            //写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        }catch (Exception e){
            log.error("file download error,filepath="+filepath,e);
        }finally {
            //释放流
            if (cosObjectInput!=null){
                cosObjectInput.close();
            }
        }

    }
}
