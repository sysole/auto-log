package aop;

import annotation.Log;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Aspect
@Component
public class LogAspect {

    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);

    /**
     * 以自定义 @SysoleLog 注解为切点
     */
    @Pointcut("@annotation(annotation.Log)")
    public void Log() {}

    /**
     * 在切点之前织入
     * @param joinPoint
     * @throws Throwable
     */
    @Before("Log()")
    public void before(JoinPoint joinPoint) throws Throwable {
        // 开始打印请求日志
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        // 打印请求相关参数
        logger.info("========================================== Start ==========================================");
        Signature signature = joinPoint.getSignature();
        if (signature instanceof MethodSignature) {
            Method method = ((MethodSignature) signature).getMethod();
            if (method != null) {
                Log sysoleLog = method.getAnnotation(Log.class);
                // 打印注解信息
                logger.info("Method Description : {}", sysoleLog.desc());
            }
        }
        // 打印请求 url
        logger.info("URL                : {}", request.getRequestURL().toString());
        // 打印 Http method
        logger.info("HTTP Method        : {}", request.getMethod());
        // 打印调用 controller 的全路径以及执行方法
        logger.info("Class Method       : {}.{}", signature.getDeclaringTypeName(), signature.getName());
        // 打印请求的 IP
        logger.info("IP                 : {}", request.getRemoteAddr());
        // 打印请求入参
        logger.info("Request Args       : {}", new ObjectMapper().writeValueAsString(joinPoint.getArgs()));

    }

    /**
     * 在切点之后织入
     */
    @After("Log()")
    public void after() {
        // 结束后打个分隔线，方便查看
        logger.info("=========================================== End ===========================================");
    }

    /**
     * 环绕
     */
    @Around("Log()")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        // 打印请求开始时间
        long startTime = System.currentTimeMillis();
        // 执行方法
        Object result = proceedingJoinPoint.proceed();
        // 打印出参
        logger.info("Response Args      : {}", new ObjectMapper().writeValueAsString(result));
        // 执行耗时
        logger.info("Time-Consuming     : {} ms", System.currentTimeMillis() - startTime);
        return result;
    }
}
