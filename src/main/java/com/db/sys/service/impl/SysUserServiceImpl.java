package com.db.sys.service.impl;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.db.common.exception.ServiceException;
import com.db.common.util.PageUtil;
import com.db.common.util.ShiroUtil;
import com.db.common.vo.PageObject;
import com.db.sys.dao.SysUserDao;
import com.db.sys.dao.SysUserRoleDao;
import com.db.sys.entity.SysUser;
import com.db.sys.service.SysUserService;
import com.db.sys.vo.SysUserDeptVo;
@Service
public class SysUserServiceImpl implements SysUserService {
    @Autowired
	private SysUserDao sysUserDao;
    @Autowired
    private SysUserRoleDao sysUserRoleDao;
    //SLF4J
    private static Logger log=LoggerFactory.getLogger(SysUserServiceImpl.class);
    @Override
    public int updatePassword(String password, String newPassword, String cfgPassword) {
    	//1.参数验证
    	//1.1原密码校验
    	if(StringUtils.isEmpty(password))
        throw new IllegalArgumentException("原密码不能为空");
    	SysUser user=ShiroUtil.getUser();
    	SimpleHash sh=
    	new SimpleHash("MD5", password, user.getSalt(), 1);
    	if(!sh.toHex().equals(user.getPassword()))
    	throw new IllegalArgumentException("原密码不正确");
    	//1.2新密码非空校验,.....
    	if(StringUtils.isEmpty(newPassword))
        throw new IllegalArgumentException("新密码不能为空");
    	if(!newPassword.equals(cfgPassword))
    	throw new IllegalArgumentException("两次密码输入不一致");
    	//2.修改密码
    	//2.1新密码加密
    	String newSalt=UUID.randomUUID().toString();
    	String newPwd=
    	new SimpleHash("MD5",newPassword, newSalt, 1).toHex();
    	//2.2新密码更新到数据库
    	int rows=
    	sysUserDao.updatePassword(newPwd,newSalt,user.getId());
    	if(rows==0)
    	throw new ServiceException("记录可能不存在");
    	//3.返回结果
    	return rows;
    }
    @Override
    public Map<String, Object> findObjectById(Integer id) {
    	//1.参数校验
    	if(id==null||id<1)
    	throw new IllegalArgumentException("id值不正确");
    	//2.基于id查询用户以及对应的部门信息
    	SysUserDeptVo user=
    	sysUserDao.findObjectById(id);
    	if(user==null)
    	throw new ServiceException("用户信息不存在");
    	//3.基于id查询用户对应的角色id
    	List<Integer> roleIds=sysUserRoleDao.findRoleIdsByUserId(id);
    	//4.对信息进行封装
    	Map<String,Object> map=new HashMap<String, Object>();
    	map.put("user", user);
    	map.put("roleIds", roleIds);
    	return map;
    }
    @Override
    public int updateObject(SysUser entity,
    		Integer[] roleIds) {
    	//1.参数校验
    	if(entity==null)
    		throw new IllegalArgumentException("保存对象不能为空");
    	if(StringUtils.isEmpty(entity.getUsername()))
    		throw new IllegalArgumentException("用户名不能为空");
    	//....
    	if(roleIds==null||roleIds.length==0)
    		throw new ServiceException("必须为用户分配角色");
    	//2.保存用户自身信息
    	//2.1构建一个盐值对象
    	int rows=sysUserDao.updateObject(entity);
    	if(rows==0)
    	throw new ServiceException("记录可能已经不存在");
    	//3.保存用户和角色关系数据
    	//3.1先删除关系数据
    	sysUserRoleDao.deleteObjectsByUserId(entity.getId());
    	//3.2再添加新的关系数据
    	sysUserRoleDao.insertObjects(
    			entity.getId(),
    			roleIds);
    	//4.返回结果
    	return rows;
    }
    @Override
    public int saveObject(SysUser entity,
    		Integer[] roleIds) {
    	//1.参数校验
    	if(entity==null)
    	throw new IllegalArgumentException("保存对象不能为空");
    	if(StringUtils.isEmpty(entity.getUsername())) {
    	log.debug("username is null");
    	throw new IllegalArgumentException("用户名不能为空");
    	}
    	if(StringUtils.isEmpty(entity.getPassword()))
    	throw new IllegalArgumentException("密码不能为空");
    	//....
    	if(roleIds==null||roleIds.length==0)
        throw new ServiceException("必须为用户分配角色");
    	//2.保存用户自身信息
    	//2.1构建一个盐值对象
    	String salt=UUID.randomUUID().toString();
    	//2.2对密码进行加密
    	//String password1=
    	//DigestUtils.md5DigestAsHex((salt+entity.getPassword()).getBytes());
    	//System.out.println("password1="+password1);
    	SimpleHash sh=new SimpleHash(//Shiro框架提供
    			"MD5",//algorithmName 算法名称
    			entity.getPassword(), //source要加密的对象
    			salt,//盐值
    			1);//hashIterations 加密次数
    	String password2=sh.toHex();
    	System.out.println("password2="+password2);
    	entity.setPassword(password2);
    	entity.setSalt(salt);
    	int rows=sysUserDao.insertObject(entity);
    	//3.保存用户和角色关系数据
    	sysUserRoleDao.insertObjects(
    			entity.getId(),
    			roleIds);
    	//4.返回结果
    	return rows;
    }
    
    
    @RequiresPermissions("sys:user:valid")
    @Override
    public int validById(Integer id, Integer valid, String modifiedUser) {
    	//1.参数校验
    	if(id==null||id<1)
        throw new IllegalArgumentException("id值无效");
    	if(valid==null||(valid!=0&&valid!=1))
        throw new IllegalArgumentException("valid值无效");
    	//2.修改状态
    	int rows=
    	sysUserDao.validById(id, valid, modifiedUser);
    	if(rows==0)
    	throw new ServiceException("记录可能已经不存在");
    	//3.返回结果
    	return rows;
    }
    
	@Override
	public PageObject<SysUserDeptVo> findPageObjects(Integer pageCurrent, String username) {
		//1.参数校验
		if(pageCurrent==null||pageCurrent<1)
		throw new IllegalArgumentException("页码值无效");
		//2.查询总记录数，并进行校验
		int rowCount=sysUserDao.getRowCount(username);
		if(rowCount==0)
		throw new ServiceException("没有对应记录");
		//3.查询当前页要呈现的记录
		int pageSize=3;
		int startIndex=(pageCurrent-1)*pageSize;
		List<SysUserDeptVo> records=
		sysUserDao.findPageObjects(username, startIndex, pageSize);
		//4.封装数据并返回
		return PageUtil.newInstance(pageCurrent, rowCount, pageSize, records);
	}

}
