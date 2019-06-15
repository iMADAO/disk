package com.madao.disk.service;

import com.madao.disk.bean.User;
import com.madao.disk.bean.UserExample;
import com.madao.disk.exception.ResultException;
import com.madao.disk.mapper.UserMapper;
import com.madao.disk.util.KeyUtil;
import com.madao.disk.util.MD5Encoder;
import com.madao.disk.util.ResultUtil;
import com.madao.disk.util.ResultView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    public void addUser(String userName, String password, String email){
        User user = new User();
        user.setUserName(userName);
        user.setEmail(email);
        user.setUserId(KeyUtil.genUniquKeyOnLong());
        user.setUserPassword(MD5Encoder.getEncryptedWithSalt(password, user.getUserId().toString()));
        userMapper.insertSelective(user);
    }

    public User checkUserLogin(String userName, String password){
        System.out.println(userName + "----" + password);
        UserExample userExample = new UserExample();
        UserExample.Criteria criteria = userExample.createCriteria();
        criteria.andUserNameEqualTo(userName);
        List<User> userList = userMapper.selectByExample(userExample);
        System.out.println(userList.size());
        if(userList==null || userList.size()==0)
            throw new ResultException("用户名或密码错误");
        User user = userList.get(0);
        System.out.println(user);
        String encodedPassword = MD5Encoder.getEncryptedWithSalt(password, user.getUserId().toString());
        if(!user.getUserPassword().equals(encodedPassword)){
            throw new ResultException("用户名或密码错误");
        }
        user.setUserPassword("");
        return user;
    }

    public void changeUserPic(Long userId, String picPath) {
        User user = userMapper.selectByPrimaryKey(userId);
        if(user==null){
            throw new ResultException("用户不存在");
        }
        user.setUserPic(picPath);
        userMapper.updateByPrimaryKeySelective(user);
    }

    public User getUserById(Long userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        return user;
    }

    public void changeUserName(Long userId, String userName) {
        User user = userMapper.selectByPrimaryKey(userId);
        user.setUserName(userName);
        userMapper.updateByPrimaryKeySelective(user);
    }


    //检查用户名是否已被使用
    public boolean checkIfUserNameExist(String userName){
        UserExample example = new UserExample();
        UserExample.Criteria criteria = example.createCriteria();
        criteria.andUserNameEqualTo(userName);
        int count = userMapper.countByExample(example);
        System.out.println("count.." + count);
        if(count <= 0)
            return true;
        return false;
    }

    //修改密码
    public void changeUserPassword(Long userId, String password, String newPassword) {
        User user = userMapper.selectByPrimaryKey(userId);
        if(user==null){
            throw new ResultException("用户不存在");
        }
        String encodePassword = MD5Encoder.getEncryptedWithSalt(password, userId.toString());
        if(!encodePassword.equals(user.getUserPassword())){
            throw new ResultException("密码错误");
        }
        String newEncodePassword = MD5Encoder.getEncryptedWithSalt(newPassword, userId.toString());

        user.setUserPassword(newEncodePassword);
        userMapper.updateByPrimaryKeySelective(user);
    }
}
