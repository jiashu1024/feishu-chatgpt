package com.zjs.feishubot.filter;

import com.zjs.feishubot.config.CommonConfig;
import com.zjs.feishubot.config.KeyGenerateConfig;
import com.zjs.feishubot.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

  protected final RedissonClient redissonClient;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

    // 获取请求的路径
    String path = request.getRequestURI();

    // 如果请求的路径在我们定义的列表中，直接跳过这个过滤器
    for (String endpoint : CommonConfig.ANONYMOUS_PATHS) {
      if (path.contains(endpoint)) {
        filterChain.doFilter(request, response);
        return;
      }
    }


    //获取 token
    String token = request.getHeader("Authorization");
    if (!StringUtils.hasText(token)) {
      filterChain.doFilter(request, response);
      return;
    }

    //解析 token
    try {
      Claims claims = JwtUtil.parseJWT(token);
      String userName = claims.getSubject();
      RBucket<Boolean> login = redissonClient.getBucket(KeyGenerateConfig.getUserLoginStatusKey(userName));
      if (login.get() == null) {
        throw new RuntimeException("用户未登录");
      } else {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userName, null, null);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
      }
      filterChain.doFilter(request, response);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
