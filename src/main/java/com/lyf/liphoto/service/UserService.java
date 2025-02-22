package com.lyf.liphoto.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyf.liphoto.model.dto.user.UserQueryRequest;
import com.lyf.liphoto.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lyf.liphoto.model.vo.LoginUserVO;
import com.lyf.liphoto.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 86157
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-02-17 23:06:44
*/
public interface UserService extends IService<User> {
    /**
     *  用户注册
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
    long userRegister(String userAccount,String userPassword,String checkPassword);

    /**
     * 获取加密后的密码
     * @param userPassword
     * @return
     */
    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);
    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);
    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取加密后的密码
     * @param userPassword
     * @return
     */
    String getEncryptPassword(String userPassword);

    /**
     * 获取脱敏后的用户信息
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏后的用户信息列表
     * @param userList
     * @return
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     *
     * @param userQueryRequest
     * @return
     */
     QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);
    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

}
