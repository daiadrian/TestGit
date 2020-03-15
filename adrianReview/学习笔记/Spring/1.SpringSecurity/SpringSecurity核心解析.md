## 1.权限管理概念

在权限管理的概念中，有两个非常重要的名词：

- <font color=red>**认证：**</font>通过用户名和密码成功登陆系统后，让系统得到当前用户的角色身份
- <font color=red>**授权：**</font>系统根据当前用户的角色，给其授予对应可以操作的权限资源

> 权限系统的三个主要对象：
>
> 1. **<font color=red>用户：</font>**主要包含用户名，密码和当前用户的角色信息，可实现认证操作
> 2. **<font color=red>角色：</font>**主要包含角色名称，角色描述和当前角色拥有的权限信息，可实现授权操作
> 3. **<font color=red>权限：</font>**权限也可以称为菜单，主要包含当前权限名称，url地址等信息，可实现动态展示菜单
>
> > 注：这三个对象中，用户与角色是多对多的关系，角色与权限是多对多的关系，用户与权限没有直接关系，二者是通过角色来建立关联关系的



## 2.核心组件

### 2.1 SecurityContextHolder身份信息的容器

 		`SecurityContextHolder` 用于存储安全上下文（security context）的信息。<font color=red>当前操作的用户是谁，该用户是否已经被认证，他拥有哪些角色权限，这些都被保存在 SecurityContextHolder 中</font>

​		`SecurityContextHolder`默认使用 `ThreadLocal` 策略来存储认证信息（这是一种与线程绑定的策略）。Spring Security 在用户登录时自动绑定认证信息到当前线程，在用户退出时，自动清除当前线程的认证信息

**<font size=4px>获取当前用户的信息：</font>**

```java
Object principal = SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

if (principal instanceof UserDetails) {
	String username = ((UserDetails) principal).getUsername();
} else {
	String username = principal.toString();
}
```

 		`getAuthentication()` 返回了认证信息，再次 `getPrincipal()` 返回了身份信息，UserDetails 便是 Spring 对身份信息封装的一个接口



### 2.2 Authentication身份信息的抽象

Authentication（译：认证方式）；Principal（译：主要的，负责人）

```java
public interface Authentication extends Principal, Serializable {

	Collection<? extends GrantedAuthority> getAuthorities();

	Object getCredentials();

    /**
      *
      */
	Object getDetails();

    /**
      *
      */
	Object getPrincipal();

    /**
      *
      */
	boolean isAuthenticated();

    /**
      *
      */
	void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException;
}
```



​		 Authentication 在 spring security 中是最高级别的 身份 / 认证的抽象；可以从这个接口中得到用户拥有的权限信息列表，密码，用户细节信息，用户身份信息，认证信息

<font size=4px>**接口方法解析：**</font>

1.  `getAuthorities()`，权限信息列表
   - 默认是 GrantedAuthority 接口的一些实现类，通常是代表权限信息的一系列字符串 
2. `getCredentials()` ，密码信息
   - 用户输入的密码字符串，在<font color=blue>认证过后通常会被移除</font>，用于保障安全 
3.  `getDetails()`，细节信息
   - web 应用中的实现接口通常为 WebAuthenticationDetails，<font color=blue>它记录了访问者的 ip 地址和 sessionId 的值 </font>
4.  `getPrincipal()`，<font color=red>**身份信息**，大部分情况下返回的是 UserDetails 接口的实现类</font>



### 2.3AuthenticationManager身份认证器 

 		**AuthenticationManager（接口）是认证相关的核心接口，也是发起认证的出发点**

> ​		因为在实际需求中，我们可能会允许用户使用用户名 + 密码登录，同时允许用户使用邮箱 + 密码，手机号码 + 密码登录，甚至可能允许用户使用指纹登录，所以说 AuthenticationManager 一般不直接认证

​		AuthenticationManager 接口的 **常用实现类 ProviderManager** 内部会维护一个 `List` 列表，存放多种认证方式，实际上这是委托者模式的应用

​		也就是说，核心的认证入口始终只有一个：AuthenticationManager，不同的认证方式则对应了三个 AuthenticationProvider。<font color=red>在默认策略下，**只需要通过一个 AuthenticationProvider 的认证**，即可被认为是登录成功 </font>



#### AuthenticationProvider 

 		AuthenticationProvider 最最最常用的一个实现便是 DaoAuthenticationProvider 

在 Spring Security 中：

- 提交的用户名和密码，被封装成了 UsernamePasswordAuthenticationToken
- 根据用户名加载用户的任务则是交给了 UserDetailsService，在 DaoAuthenticationProvider 中，对应的方法便是 retrieveUser，虽然有两个参数，但是 retrieveUser 只有第一个参数起主要作用，返回一个 UserDetails
- 还需要完成 UsernamePasswordAuthenticationToken 和 UserDetails 密码的比对，这便是交给 additionalAuthenticationChecks 方法完成的，如果这个 void 方法没有抛异常，则认为比对成功



### 2.4 UserDetails和UserDetailsService

 		<font color=red>UserDetails 这个接口，它代表了最详细的用户信息</font>，这个接口涵盖了一些必要的用户信息字段，具体的实现类对它进行了扩展 

​		 <font color=red>UserDetailsService 只负责从特定的地方（通常是数据库）加载用户信息</font>。 UserDetailsService 常见的实现类有 JdbcDaoImpl，InMemoryUserDetailsManager，前者从数据库加载用户，后者从内存中加载用户，也可以自己实现 UserDetailsService



**<font size=4px>UserDetail和Authentication区别</font>**

-  Authentication 的 `getCredentials()` 与 UserDetails 中的 `getPassword()` 需要被区分对待
  - Authentication 是用户提交的密码凭证
  - UserDetails 是用户正确的密码，认证器其实就是对这两者的比对
-  Authentication 中的 `getAuthorities()` 实际是由 UserDetails 的 `getAuthorities()` 传递而形成的 
-  Authentication 接口中的 `getUserDetails()` 方法的 UserDetails 用户详细信息是经过 AuthenticationProvider 之后被设置进来的





## 3.核心过滤器

​		SpringSecurityFilterChain 是 SpringSecurity 的核心过滤器，是整个认证的入口。但是负责拦截请求的是 DelegatingFilterProxy 这个代理类

<font size=4px>**拦截请求的流程：**</font>

1. DelegatingFilterProxy 这个代理类会通过 beanName 为 springSecurityFilterChain 得到一个过滤器，这个过滤器是 FilterChainProxy ，然后执行这个过滤器
2. FilterChainProxy 过滤器会遍历封装要执行的过滤器链，然后加载过滤器链
   - 这里的过滤器链就是默认常用的 15 个过滤器



### 3.1常用15个过滤器

执行顺序从上往下

1. ***<font color=blue>SecurityContextPersistenceFilter</font>***
   - 使用 SecurityContextRepository 在 Session 中保存或更新一个 SecurityContext，并将 SecurityContext 给以后的过滤器使用，来为后续拦截器建立所需的上下文；请求结束后会清空SecurityContext
   - SecurityContext 中存储了当前用户的认证以及权限信息，可以说是安全上下文信息
2. ***WebAsyncManagerIntegrationFilter***
   - 用于集成 SecurityContext 到 Spring 异步执行机制中的 WebAsyncManager
3. ***HeaderWriterFilter***
   - 向请求的Header中添加相应的信息： 比如 X-Frame-Options，X-XSS-Protection*，X-Content-Type-Options
4. ***<font color=blue>CsrfFilter</font>***
   - csrf又称跨域请求伪造，SpringSecurity 会对**所有 POST 请求**验证是否包含系统生成的 csrf 的 Token 信息，如果不包含，则报错。起到防止csrf攻击的效果。
5. ***LogoutFilter***
   - 匹配URL为 `/logout` 的请求，实现用户退出，清除认证信息
6. ***<font color=blue>UsernamePasswordAuthenticationFilter</font>***
   - 认证操作全靠这个过滤器，默认匹配URL为 `/login` 且必须为 POST 请求
7. ***DefaultLoginPageGeneratingFilter***
   - 如果没有在配置文件中指定认证页面，则由该过滤器生成一个默认认证页面
8. ***DefaultLogoutPageGeneratingFilter***
   - 由此过滤器可以生成一个默认的退出登录页面
9. ***BasicAuthenticationFilter***
   - 此过滤器会自动解析HTTP请求中头部名字为Authentication，且以Basic开头的头信息
10. ***RequestCacheAwareFilter***
    - 通过HttpSessionRequestCache内部维护了一个RequestCache，用于缓存 HttpServletRequest
11. ***SecurityContextHolderAwareRequestFilter***
    - 针对ServletRequest进行了一次包装，使得 Request 具有更加丰富的API
12. ***<font color=blue>AnonymousAuthenticationFilter</font>***
    - 当SecurityContextHolder中认证信息为空，则会创建一个***匿名用户*** 存入到SecurityContextHolder中
    - SpringSecurity为了兼容未登录的访问，也走了一套认证流程，只不过是一个匿名的身份
13. ***SessionManagementFilter***
    - SecurityContextRepository 限制同一用户开启多个会话的数量
14. ***<font color=blue>ExceptionTranslationFilter</font>***
    - 异常转换过滤器，用来转换整个链路中出现的异常
    -  这个过滤器本身不处理异常，而是将认证过程中出现的异常交给内部维护的一些类去处理 
15. ***<font color=blue>FilterSecurityInterceptor</font>***
    - 获取所配置资源访问的授权信息
    - 然后根据 SecurityContextHolder 中存储的用户信息来决定其是否有权限



### 3.2 SecurityContextPersistenceFilter

SecurityContextPersistenceFilter 的两个主要作用便是：

① 请求来临时，创建 `SecurityContext` 安全上下文信息

② 请求结束时清空 `SecurityContextHolder`



​		微服务的一个设计理念需要实现服务通信的无状态，而 http 协议中的无状态意味着不允许存在 session，这可以通过 `setAllowSessionCreation(false)` 实现，但是这并不意味着 SecurityContextPersistenceFilter 变得无用，因为它还需要负责清除用户信息

​		在 Spring Security 中，虽然安全上下文信息被存储于 Session 中，但在实际使用中不应该直接操作 Session，而应当使用 ***SecurityContextHolder*** 

```java
public class SecurityContextPersistenceFilter extends GenericFilterBean {

   static final String FILTER_APPLIED = "__spring_security_scpf_applied";
   // 安全上下文存储的仓库，其实是HttpSessionSecurityContextRepository
   private SecurityContextRepository repo;
  
   public SecurityContextPersistenceFilter() {
      // HttpSessionSecurityContextRepository 是 SecurityContextRepository 接口的一个实现类
      // 使用 HttpSession 来存储 SecurityContext
      this(new HttpSessionSecurityContextRepository());
   }

   public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
         throws IOException, ServletException {
      HttpServletRequest request = (HttpServletRequest) req;
      HttpServletResponse response = (HttpServletResponse) res;

      if (request.getAttribute(FILTER_APPLIED) != null) {
         chain.doFilter(request, response);
         return;
      }
      request.setAttribute(FILTER_APPLIED, Boolean.TRUE);
      // 包装 request，response
      HttpRequestResponseHolder holder = new HttpRequestResponseHolder(request,
            response);
      // 从 Session 中获取安全上下文信息
      SecurityContext contextBeforeChainExecution = repo.loadContext(holder);
      try {
         // 请求开始时，设置安全上下文信息，这样就避免了用户直接从 Session 中获取安全上下文信息
         SecurityContextHolder.setContext(contextBeforeChainExecution);
         chain.doFilter(holder.getRequest(), holder.getResponse());
      }
      finally {
         // 请求结束后，清空安全上下文信息
         SecurityContext contextAfterChainExecution = SecurityContextHolder
               .getContext();
         SecurityContextHolder.clearContext();
         repo.saveContext(contextAfterChainExecution, holder.getRequest(),
               holder.getResponse());
         request.removeAttribute(FILTER_APPLIED);
         if (debug) {
            logger.debug("...");
         }
      }
   }
}
```



- 上面代码中的 repo 对象

```java
public class HttpSessionSecurityContextRepository implements SecurityContextRepository {
   // 'SPRING_SECURITY_CONTEXT' 是安全上下文默认存储在 Session 中的键值
   public static final String SPRING_SECURITY_CONTEXT_KEY = "SPRING_SECURITY_CONTEXT";
   ...
   private final Object contextObject = SecurityContextHolder.createEmptyContext();
   private boolean allowSessionCreation = true;
   private boolean disableUrlRewriting = false;
   private String springSecurityContextKey = SPRING_SECURITY_CONTEXT_KEY;

   private AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

   // 从当前 request 中取出安全上下文，如果 session 为空，则会返回一个新的安全上下文
   public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
      HttpServletRequest request = requestResponseHolder.getRequest();
      HttpServletResponse response = requestResponseHolder.getResponse();
      HttpSession httpSession = request.getSession(false);
      SecurityContext context = readSecurityContextFromSession(httpSession);
      if (context == null) {
         context = generateNewContext();
      }
      ...
      return context;
   }

   ...

   public boolean containsContext(HttpServletRequest request) {
      HttpSession session = request.getSession(false);
      if (session == null) {
         return false;
      }
      return session.getAttribute(springSecurityContextKey) != null;
   }

   private SecurityContext readSecurityContextFromSession(HttpSession httpSession) {
      if (httpSession == null) {
         return null;
      }
      ...
      // Session 存在的情况下，尝试获取其中的 SecurityContext
      Object contextFromSession = httpSession.getAttribute(springSecurityContextKey);
      if (contextFromSession == null) {
         return null;
      }
      ...
      return (SecurityContext) contextFromSession;
   }

   // 初次请求时创建一个新的 SecurityContext 实例
   protected SecurityContext generateNewContext() {
      return SecurityContextHolder.createEmptyContext();
   }

}
```



### 3.3身份认证过滤器

 UsernamePasswordAuthenticationFilter 主要肩负起了调用身份认证器，校验身份的作用 



```java
public class UsernamePasswordAuthenticationFilter extends
		AbstractAuthenticationProcessingFilter {

	public static final String SPRING_SECURITY_FORM_USERNAME_KEY = "username";
	public static final String SPRING_SECURITY_FORM_PASSWORD_KEY = "password";

	private String usernameParameter = SPRING_SECURITY_FORM_USERNAME_KEY;
	private String passwordParameter = SPRING_SECURITY_FORM_PASSWORD_KEY;
	private boolean postOnly = true;

	public UsernamePasswordAuthenticationFilter() {
		super(new AntPathRequestMatcher("/login", "POST"));
	}

	public Authentication attemptAuthentication(HttpServletRequest request,
			HttpServletResponse response) throws AuthenticationException {
		// 登录请求需要 POST 请求
        if (postOnly && !request.getMethod().equals("POST")) {
			throw new AuthenticationServiceException(
					"Authentication method not supported: " + request.getMethod());
		}
		// 获取表单中的用户名和密码
		String username = obtainUsername(request);
		String password = obtainPassword(request);

		if (username == null) username = "";
		if (password == null) password = "";
		username = username.trim();
		// 组装成 username+password 形式的 token
		UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
				username, password);		
		setDetails(request, authRequest);
        // 交给内部的 AuthenticationManager 去认证，并返回认证信息
		return this.getAuthenticationManager().authenticate(authRequest);
	}
}
```



- 父类的方法才是主要的过滤器方法

```java
public abstract class AbstractAuthenticationProcessingFilter extends GenericFilterBean
      implements ApplicationEventPublisherAware, MessageSourceAware {
	// 包含了一个身份认证器
	private AuthenticationManager authenticationManager;
	// 用于实现 remeberMe (记住我)
	private RememberMeServices rememberMeServices = new NullRememberMeServices();
	private RequestMatcher requiresAuthenticationRequestMatcher;
	// 这两个 Handler 很关键，分别代表了认证成功和失败相应的处理器
	private AuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
	private AuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();
	
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		...
		Authentication authResult;
		try {
			// 此处实际上就是调用 UsernamePasswordAuthenticationFilter 的 attemptAuthentication 方法
			authResult = attemptAuthentication(request, response);
			if (authResult == null) {
				// 子类未完成认证，立刻返回
				return;
			}
			sessionStrategy.onAuthentication(authResult, request, response);
		}
		// 在认证过程中可以直接抛出异常，在过滤器中，就像此处一样，进行捕获
		catch (InternalAuthenticationServiceException failed) {
			// 内部服务异常
			unsuccessfulAuthentication(request, response, failed);
			return;
		}
		catch (AuthenticationException failed) {
			// 认证失败
			unsuccessfulAuthentication(request, response, failed);
			return;
		}
		// 认证成功
		if (continueChainBeforeSuccessfulAuthentication) {
			chain.doFilter(request, response);
		}
		// 注意，认证成功后过滤器把 authResult 结果也传递给了成功处理器
		successfulAuthentication(request, response, chain, authResult);
	}
}
```



### 3.4匿名认证过滤器

 		SpirngSecurity 为了整体逻辑的统一性，即使是未通过认证的用户，也给予了一个匿名身份

​		 `AnonymousAuthenticationFilter` 该过滤器的位置也是非常的科学的，它位于常用的身份认证过滤器（如 `UsernamePasswordAuthenticationFilter`、`BasicAuthenticationFilter`、`RememberMeAuthenticationFilter`）之后

​		意味着只有在上述身份过滤器执行完毕后，SecurityContext 依旧没有用户信息，`AnonymousAuthenticationFilter` 该过滤器才会有意义；给予基于用户一个匿名身份 



```java
public class AnonymousAuthenticationFilter extends GenericFilterBean implements
      InitializingBean {
   private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();
   private String key;
   private Object principal;
   private List<GrantedAuthority> authorities;

   // 自动创建一个 "anonymousUser" 的匿名用户, 其具有 ANONYMOUS 角色
   public AnonymousAuthenticationFilter(String key) {
      this(key, "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
   }

   /**
    *
    * @param key key 用来识别该过滤器创建的身份
    * @param principal principal 代表匿名用户的身份
    * @param authorities authorities 代表匿名用户的权限集合
    */
   public AnonymousAuthenticationFilter(String key, Object principal,
         List<GrantedAuthority> authorities) {
      Assert.hasLength(key, "key cannot be null or empty");
      Assert.notNull(principal, "Anonymous authentication principal must be set");
      Assert.notNull(authorities, "Anonymous authorities must be set");
      this.key = key;
      this.principal = principal;
      this.authorities = authorities;
   }
   ...

   public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
         throws IOException, ServletException {
      // 过滤器链都执行到匿名认证过滤器这儿了还没有身份信息，塞一个匿名身份进去
      if (SecurityContextHolder.getContext().getAuthentication()== null) {
         SecurityContextHolder.getContext().setAuthentication(
               createAuthentication((HttpServletRequest) req));
      }
      chain.doFilter(req, res);
   }

   protected Authentication createAuthentication(HttpServletRequest request) {
     // 创建一个 AnonymousAuthenticationToken
      AnonymousAuthenticationToken auth = new AnonymousAuthenticationToken(key,
            principal, authorities);
      auth.setDetails(authenticationDetailsSource.buildDetails(request));

      return auth;
   }
   ...
}
```



### 3.5异常转换过滤器 

​		`ExceptionTranslationFilter` 异常转换过滤器位于整个 `SpringSecurityFilterChain` 的后方，用来转换整个链路中出现的异常，将其转化。一般其只处理两大类异常：`AccessDeniedException` 访问异常和 `AuthenticationException` 认证异常

​		这个过滤器非常重要，因为它将 Java 中的异常和 HTTP 的响应连接在了一起，这样在处理异常时，抛出相应的异常便可

- 如果该过滤器检测到 `AuthenticationException`，则将会交给内部的 `AuthenticationEntryPoint` 去处理
- 如果检测到 `AccessDeniedException`，需要先判断当前用户是不是匿名用户
  - 如果是匿名访问，则和前面一样运行 `AuthenticationEntryPoint` （认证的入口点）
  - 否则会委托给 `AccessDeniedHandler` 去处理，其默认实现是 `AccessDeniedHandlerImpl`



```java
public class ExceptionTranslationFilter extends GenericFilterBean {
  // 1. 处理异常转换的核心方法
  private void handleSpringSecurityException(HttpServletRequest request,
        HttpServletResponse response, FilterChain chain, RuntimeException exception)
        throws IOException, ServletException {
     if (exception instanceof AuthenticationException) {
       	// 2. 重定向到登录端点
        sendStartAuthentication(request, response, chain,
              (AuthenticationException) exception);
     }
     else if (exception instanceof AccessDeniedException) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authenticationTrustResolver.isAnonymous(authentication) || authenticationTrustResolver.isRememberMe(authentication)) {
		  // 3. 重定向到登录端点
           sendStartAuthentication(
                 request,
                 response,
                 chain,
                 new InsufficientAuthenticationException(
                       "Full authentication is required to access this resource"));
        }
        else {
           // 4. 交给 accessDeniedHandler 处理
           accessDeniedHandler.handle(request, response,
                 (AccessDeniedException) exception);
        }
     }
  }
}

// formLogin() 配置
public abstract class AbstractAuthenticationFilterConfigurer extends ...{
   ...
   //formLogin 配置了 AuthenticationEntryPoint
   private LoginUrlAuthenticationEntryPoint authenticationEntryPoint;
   // 认证失败的处理器
   private AuthenticationFailureHandler failureHandler;
   ...
}

// 匿名访问的处理类
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
	protected static final Log logger = LogFactory.getLog(AccessDeniedHandlerImpl.class);
	private String errorPage;

	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException,
			ServletException {
		if (!response.isCommitted()) {
			if (errorPage != null) {
                //1.设置异常到request
				request.setAttribute(WebAttributes.ACCESS_DENIED_403,
						accessDeniedException);
				//2.设置403状态码
				response.setStatus(HttpStatus.FORBIDDEN.value());
				//3.重定向到错误页面
				RequestDispatcher dispatcher = request.getRequestDispatcher(errorPage);
				dispatcher.forward(request, response);
			}
			else {
				response.sendError(HttpStatus.FORBIDDEN.value(),
					HttpStatus.FORBIDDEN.getReasonPhrase());
			}
		}
	}
    
	public void setErrorPage(String errorPage) {
		if ((errorPage != null) && !errorPage.startsWith("/")) {
			throw new IllegalArgumentException("errorPage must begin with '/'");
		}
		this.errorPage = errorPage;
	}
}
```



## 4.认证流程源码分析(重点)

<font color=red>**对使用用户名和密码进行认证，默认的认证流程**</font>

1. <font size=4px>`UsernamePasswordAuthenticationFilter.java`  用户名和密码认证的过滤器</font>

执行其过滤器方法，在其父类 `AbstractAuthenticationProcessingFilter.java` 中

```java
public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;

    if (!requiresAuthentication(request, response)) {
        chain.doFilter(request, response);
        return;
    }

    if (logger.isDebugEnabled()) {
        logger.debug("Request is to process authentication");
    }

    Authentication authResult;
    try {
        //1. 执行UsernamePasswordAuthenticationFilter的方法得到一个已经认证好的Authentication
        authResult = attemptAuthentication(request, response);
        if (authResult == null) {
            return;
        }
        sessionStrategy.onAuthentication(authResult, request, response);
    }
    catch (InternalAuthenticationServiceException failed) {
        //2. 执行失败的方法
        //其实就是调用AuthenticationFailureHandler的实现类
        //可以自行实现该类自定义错误的响应
        unsuccessfulAuthentication(request, response, failed);
        return;
    }
    catch (AuthenticationException failed) {
        unsuccessfulAuthentication(request, response, failed);
        return;
    }

    if (continueChainBeforeSuccessfulAuthentication) {
        chain.doFilter(request, response);
    }
	//2. 执行认证成功的方法
    //其实就是调用AuthenticationSuccessHandler的实现类
    //可以自行实现该类自定义成功的响应
    successfulAuthentication(request, response, chain, authResult);
}
```



<font size=4px>1.1 调用 `UsernamePasswordAuthenticationFilter.java` 的方法得到认证的Authentication</font>

```java
public Authentication attemptAuthentication(HttpServletRequest request,
			HttpServletResponse response) throws AuthenticationException {
    //1. 判断请求是否是POST请求
    if (postOnly && !request.getMethod().equals("POST")) {
        throw new AuthenticationServiceException(
            "Authentication method not supported: " + request.getMethod());
    }
    //2. 获取参数
    String username = obtainUsername(request);
    String password = obtainPassword(request);
    if (username == null) {
        username = "";
    }
    if (password == null) {
        password = "";
    }

    username = username.trim();
    //3. 构造一个UsernamePasswordAuthenticationToken对象
    //这个对象实现了Authentication(认证方式)
    UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
        username, password);

    setDetails(request, authRequest);
	//4. 开始认证
    return this.getAuthenticationManager().authenticate(authRequest);
}
```



2. <font size=4px>`ProviderManager.java`  对Authentication进行认证</font>

```java
public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
    //1. 因为有很多种不同的认证方式, 可以由开发者自行实现的认证方式(比如支付宝认证,微信认证等)
    Class<? extends Authentication> toTest = authentication.getClass();
    AuthenticationException lastException = null;
    AuthenticationException parentException = null;
    Authentication result = null;
    Authentication parentResult = null;
    boolean debug = logger.isDebugEnabled();
    
	//2. 这里是不同的认证方式提供者,也就是核心的认证流程
    //这个地方可以由开发者进行实现,实现自己的认证方式
    //只要通过其中一种方式的认证即可成功放行,所以使用了for循环
    for (AuthenticationProvider provider : getProviders()) {
        //3. 查看当前的认证方式提供者是否支持当前的Authentication
        if (!provider.supports(toTest)) {
            continue;
        }

        if (debug) {
            logger.debug("Authentication attempt using "
                         + provider.getClass().getName());
        }

        try {
            //4. 开始认证
            result = provider.authenticate(authentication);

            if (result != null) {
                copyDetails(authentication, result);
                break;
            }
        }
        catch (AccountStatusException e) {
            prepareException(e, authentication);
            throw e;
        }
        catch (InternalAuthenticationServiceException e) {
            prepareException(e, authentication);
            throw e;
        }
        catch (AuthenticationException e) {
            lastException = e;
        }
    }
    if (result == null && parent != null) {
        try {
            result = parentResult = parent.authenticate(authentication);
        }
        catch (ProviderNotFoundException e) {
        }
        catch (AuthenticationException e) {
            lastException = parentException = e;
        }
    }
    if (result != null) {
        if (eraseCredentialsAfterAuthentication
            && (result instanceof CredentialsContainer)) {
            ((CredentialsContainer) result).eraseCredentials();
        }
        if (parentResult == null) {
            eventPublisher.publishAuthenticationSuccess(result);
        }
        return result;
    }
    if (lastException == null) {
        lastException = new ProviderNotFoundException(messages.getMessage(
            "ProviderManager.providerNotFound",
            new Object[] { toTest.getName() },
            "No AuthenticationProvider found for {0}"));
    }
    if (parentException == null) {
        prepareException(lastException, authentication);
    }
    throw lastException;
}
```



3. <font size=4px>`AbstractUserDetailsAuthenticationProvider.java` 真正的认证</font>

```java
public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
    Assert.isInstanceOf(UsernamePasswordAuthenticationToken.class, authentication,
                        () -> messages.getMessage(
                            "AbstractUserDetailsAuthenticationProvider.onlySupports",
                            "Only UsernamePasswordAuthenticationToken is supported"));

    String username = (authentication.getPrincipal() == null) ? "NONE_PROVIDED"
        : authentication.getName();

    boolean cacheWasUsed = true;
    //1. 从缓存中获取 UserDetails
    UserDetails user = this.userCache.getUserFromCache(username);

    if (user == null) {
        cacheWasUsed = false;

        try {
            //2. 得到一个UserDetails对象, 这个对象用来和用户登录的对象authentication进行比对
            //这里因为是默认的登录,所以Provider是DaoAuthenticationProvider
            //调用UserDetailsService的loadUserByUsername方法获取UserDetails对象
            //可以自定义实现UserDetailsService接口来实现自己的获取方式(通常是数据库获取)
            //注意：这个用户一般是数据库中的用户信息,也就是正确的用户信息
            //注意：这个用户一般是数据库中的用户信息,也就是正确的用户信息
            //注意：这个用户一般是数据库中的用户信息,也就是正确的用户信息
            user = retrieveUser(username, 
                                (UsernamePasswordAuthenticationToken) authentication);
        }
        catch (UsernameNotFoundException notFound) {
            logger.debug("User '" + username + "' not found");

            if (hideUserNotFoundExceptions) {
                throw new BadCredentialsException(messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials",
                    "Bad credentials"));
            }
            else {
                throw notFound;
            }
        }
        Assert.notNull(user,
             "retrieveUser returned null - a violation of the interface contract");
    }

    try {
        //3. 检查UserDetails对象
        //preAuthenticationChecks是内部类
        preAuthenticationChecks.check(user);
        //4.对密码的校验,同样是DaoAuthenticationProvider的实现
        additionalAuthenticationChecks(user, 
                                 (UsernamePasswordAuthenticationToken) authentication);
    }
    catch (AuthenticationException exception) {
        if (cacheWasUsed) {
            cacheWasUsed = false;
            user = retrieveUser(username,
                                (UsernamePasswordAuthenticationToken) authentication);
            preAuthenticationChecks.check(user);
            additionalAuthenticationChecks(user,
                            (UsernamePasswordAuthenticationToken) authentication);
        }
        else {
            throw exception;
        }
    }
	//5. 对UserDetails对象的后置检查,同样是内部类
    postAuthenticationChecks.check(user);
    //6. 将数据库中的该用户的信息放到缓存中
    if (!cacheWasUsed) {
        this.userCache.putUserInCache(user);
    }

    Object principalToReturn = user;

    if (forcePrincipalAsString) {
        principalToReturn = user.getUsername();
    }
	//7. 重新构造一个UsernamePasswordAuthenticationToken对象
    //此处会将登录用户的权限信息放进去
    return createSuccessAuthentication(principalToReturn, authentication, user);
}
```



3.1 <font size=4px>`DaoAuthenticationProvider.java` 获取 UserDetails 对象</font>

```java
protected final UserDetails retrieveUser(String username,
			UsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {
    prepareTimingAttackProtection();
    try {
        //1. 这里调用UserDetailsService的loadUserByUsername方法获取UserDetails对象
        //所以可以自行实现UserDetailsService接口来自定义获取UserDetails对象
        UserDetails loadedUser = this.getUserDetailsService().loadUserByUsername(username);
        if (loadedUser == null) {
            throw new InternalAuthenticationServiceException(
                "UserDetailsService returned null, which is an interface contract violation");
        }
        return loadedUser;
    }
    catch (UsernameNotFoundException ex) {
        mitigateAgainstTimingAttack(authentication);
        throw ex;
    }
    catch (InternalAuthenticationServiceException ex) {
        throw ex;
    }
    catch (Exception ex) {
        throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
    }
}
```



<font size=4px>3.2 `DefaultPreAuthenticationChecks.java` 内部类对 UserDetails 对象进行前置检查</font>

```java
private class DefaultPreAuthenticationChecks implements UserDetailsChecker {
    public void check(UserDetails user) {
        //对UserDetails中的其中三个boolean方法进行判断
        //1.用户是否被锁定
        if (!user.isAccountNonLocked()) {
            logger.debug("User account is locked");

            throw new LockedException(messages.getMessage(
                "AbstractUserDetailsAuthenticationProvider.locked",
                "User account is locked"));
        }
		//2.用户是否可用
        if (!user.isEnabled()) {
            logger.debug("User account is disabled");

            throw new DisabledException(messages.getMessage(
                "AbstractUserDetailsAuthenticationProvider.disabled",
                "User is disabled"));
        }
        //3.用户是否已经过期
        if (!user.isAccountNonExpired()) {
            logger.debug("User account is expired");

            throw new AccountExpiredException(messages.getMessage(
                "AbstractUserDetailsAuthenticationProvider.expired",
                "User account has expired"));
        }
    }
}
```



<font size=4px>3.3 `DaoAuthenticationProvider.java` 对密码的校验</font>

```java
protected void additionalAuthenticationChecks(UserDetails userDetails,
			UsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {
    if (authentication.getCredentials() == null) {
        logger.debug("Authentication failed: no credentials provided");

        throw new BadCredentialsException(messages.getMessage(
            "AbstractUserDetailsAuthenticationProvider.badCredentials",
            "Bad credentials"));
    }
	//1. 获取原始密码
    String presentedPassword = authentication.getCredentials().toString();
	//2. 进行密码的比对校验, 可以自行实现PasswordEncoder接口自定义校验方式
    if (!passwordEncoder.matches(presentedPassword, userDetails.getPassword())) {
        logger.debug("Authentication failed: password does not match stored value");

        throw new BadCredentialsException(messages.getMessage(
            "AbstractUserDetailsAuthenticationProvider.badCredentials",
            "Bad credentials"));
    }
}
```



<font size=4px>3.4 `DefaultPostAuthenticationChecks.java` 对 UserDetails 对象的后置校验</font>

```java
private class DefaultPostAuthenticationChecks implements UserDetailsChecker {
    public void check(UserDetails user) {
        //1. 用户的凭据（密码）是否已过期
        if (!user.isCredentialsNonExpired()) {
            logger.debug("User account credentials have expired");

            throw new CredentialsExpiredException(messages.getMessage(
                "AbstractUserDetailsAuthenticationProvider.credentialsExpired",
                "User credentials have expired"));
        }
    }
}
```



4. <font size=4px>认证成功后会调用 `AbstractAuthenticationProcessingFilter.java` 的 successfulAuthentication 方法</font>

```java
protected void successfulAuthentication(HttpServletRequest request,
			HttpServletResponse response, FilterChain chain, Authentication authResult)
			throws IOException, ServletException {

    if (logger.isDebugEnabled()) {
        logger.debug("... " + authResult);
    }

    //将用户认证的信息封装成SecurityContext，然后放到SecurityContextHolder中
    SecurityContextHolder.getContext().setAuthentication(authResult);

    rememberMeServices.loginSuccess(request, response, authResult);

    if (this.eventPublisher != null) {
        eventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(
            authResult, this.getClass()));
    }
	//可自行实现AuthenticationSuccessHandler,自定义成功认证的响应
    //默认是SavedRequestAwareAuthenticationSuccessHandler
    successHandler.onAuthenticationSuccess(request, response, authResult);
}
```



- `SecurityContext.java` 其实就是包装了一个Authentication，然后重写的 equal 和 toString 方法，保证唯一性
- `SecurityContextHolder.java` 是一个ThreadLocal，也就是说将 SecurityContext 作为当前线程的独立副本，这次请求中都能够获取到同一个 SecurityContext ，即Authentication
- 过滤器链的第一个过滤器是 `SecurityContextPersistenceFilter.java` ，这个过滤器就是：
  - 请求来的时候，先检查 Session 中是否有 SecurityContext ，如果有就取出来放到线程中（SecurityContextHolder）
  - 响应返回的时候，检查SecurityContextHolder是否有SecurityContext ，如果有就放到Session中



### 4.1API中获取用户认证信息

```java
@GetMapping("/me")
public Object getSecurityUser(
    Authentication authentication,
    @AuthenticationPrincipal UserDetails userDetails
            ) {
      //从SecurityContextHolder中获取
      SecurityContextHolder.getContext().getAuthentication();
      //直接由Spring注入
      authentication;
      //通过注解由Spring注入
      return userDetails;
}
```

