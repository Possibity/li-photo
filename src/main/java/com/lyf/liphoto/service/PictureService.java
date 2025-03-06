package com.lyf.liphoto.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyf.liphoto.model.dto.picture.PictureQueryRequest;
import com.lyf.liphoto.model.dto.picture.PictureReviewRequest;
import com.lyf.liphoto.model.dto.picture.PictureUploadRequest;
import com.lyf.liphoto.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lyf.liphoto.model.entity.User;
import com.lyf.liphoto.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
* @author 86157
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-02-20 23:45:03
*/
public interface PictureService extends IService<Picture> {
    /**
     * 上传图片
     *
     * @param inputSource 文件输入源
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(Object inputSource,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);

    /**
     * 获取查询对象
     * @param pictureQueryRequest 请求体转为QueryWrapper对象
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取图片封装的方法(单条)
     * @param picture
     * @param request
     * @return
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 分页获取图片封装(多条)
     * @param picturePage
     * @param request
     * @return
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 编写图片数据校验方法，用于更新和修改图片时进行判断：
     * @param picture
     */
    void validPicture(Picture picture);

    /**
     * 图片审核
     *
     * @param pictureReviewRequest
     * @param loginUser
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 根据用户角色给图片对象补充填充审核字段的值
     * @param picture
     * @param loginUser
     */
    public void fillReviewParams(Picture picture,User loginUser);
}
