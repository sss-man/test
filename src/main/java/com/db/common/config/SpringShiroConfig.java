package com.db.common.config;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import com.db.sys.service.realm.ShiroUserRealm;
/**@Configuration 描述的bean一般为一个配置类*/
@Configuration
public class SpringShiroConfig {
	 /**
	  * @Bean一般用户描述方法，然后将方法的返回值
	  * 交给Spring管理,其中@Bean注解中的内容为Bean
	  * 对象的key。
	  * @return
	  */
	 @Bean("securityManager")
	 public SecurityManager newSecurityManager(
			 ShiroUserRealm realm,CacheManager cacheManager,
			 RememberMeManager rememberMeManager) {
		 DefaultWebSecurityManager sm=
		 new DefaultWebSecurityManager();
		 sm.setRealm(realm);
		 sm.setCacheManager(cacheManager);
		 sm.setRememberMeManager(rememberMeManager);
		 sm.setSessionManager(newSessionManager());
		 return sm;//不是java.lang包中的SecurityManager
	 }
	 @Bean("shiroFilterFactory")
	 public ShiroFilterFactoryBean newShiroFilterFactoryBean(
		 SecurityManager securityManager) {
		 //1.构建ShiroFilterFactoryBean对象(负责创建ShiroFilter工厂对象)
		 ShiroFilterFactoryBean fBean=new ShiroFilterFactoryBean();
		 //2.设置安全管理器
		 fBean.setSecurityManager(securityManager);
		 //3.设置登录页面对应的url(非认证用户要跳转到此url对应的页面)
		 fBean.setLoginUrl("/doLoginUI.do");
		 //4.设置过滤规则(哪些允许匿名访问，哪些需要认证访问)
		 Map<String,String> filterMap=
		 new LinkedHashMap<String,String>();
		 filterMap.put("/bower_components/**","anon");
		 filterMap.put("/build/**","anon");
		 filterMap.put("/dist/**","anon");
		 filterMap.put("/plugins/**","anon");
		 filterMap.put("/user/doLogin.do", "anon");
		 filterMap.put("/doLogout.do","logout");
		 filterMap.put("/**","user");
		 fBean.setFilterChainDefinitionMap(filterMap);
		 return fBean;
	 }
	 //==========================
	 //@Bean注解没有指定名字时，默认bean的名字为方法名
	 @Bean("lifecycleBeanPostProcessor")
	 public LifecycleBeanPostProcessor newLifecycleBeanPostProcessor() {
		 return new LifecycleBeanPostProcessor();
	 }
	 
	 @DependsOn("lifecycleBeanPostProcessor")
	 @Bean
	 public DefaultAdvisorAutoProxyCreator DefaultAdvisorAutoProxyCreator() {
		 return new DefaultAdvisorAutoProxyCreator();
	 }
	 @Bean
	 public AuthorizationAttributeSourceAdvisor newAuthorizationAdvisor(
			 SecurityManager securityManager) {
		 AuthorizationAttributeSourceAdvisor advisor=
		 new AuthorizationAttributeSourceAdvisor();
		 advisor.setSecurityManager(securityManager);
		 return advisor;
	 }
	 //配置缓存管理器(可以缓存用户的权限信息)
	 @Bean
	 public MemoryConstrainedCacheManager newCacheManager() {
		 return new MemoryConstrainedCacheManager();
	 }
	 //配置记住我
	 @Bean
	 public CookieRememberMeManager newCookieManager() {
		 CookieRememberMeManager cookieManager=
		 new CookieRememberMeManager();
		 SimpleCookie cookie=
		 new SimpleCookie("rememberMe");
		 cookie.setMaxAge(24*7*60*60);
		 cookieManager.setCookie(cookie);
		 return cookieManager;
	 }
	 public DefaultWebSessionManager newSessionManager() {
		 DefaultWebSessionManager sManager=new DefaultWebSessionManager();
		 sManager.setGlobalSessionTimeout(21600000);
		 sManager.setDeleteInvalidSessions(true);
		 sManager.setSessionValidationSchedulerEnabled(true);
		 return sManager;
	 }
}









