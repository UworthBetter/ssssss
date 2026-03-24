package com.qkyd.framework.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.CorsFilter;
import com.qkyd.framework.config.properties.PermitAllUrlProperties;
import com.qkyd.framework.security.filter.JwtAuthenticationTokenFilter;
import com.qkyd.framework.security.handle.AuthenticationEntryPointImpl;
import com.qkyd.framework.security.handle.LogoutSuccessHandlerImpl;

/**
 * spring security閰嶇疆
 *
 * @author qkyd
 */
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@EnableWebSecurity
@Configuration
public class SecurityConfig {
    /**
     * 鑷畾涔夌敤鎴疯璇侀€昏緫
     */
    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * 璁よ瘉澶辫触澶勭悊绫?
     */
    @Autowired
    private AuthenticationEntryPointImpl unauthorizedHandler;

    /**
     * 閫€鍑哄鐞嗙被
     */
    @Autowired
    private LogoutSuccessHandlerImpl logoutSuccessHandler;

    /**
     * token璁よ瘉杩囨护鍣?
     */
    @Autowired
    private JwtAuthenticationTokenFilter authenticationTokenFilter;

    /**
     * 璺ㄥ煙杩囨护鍣?
     */
    @Autowired
    private CorsFilter corsFilter;

    /**
     * 鍏佽鍖垮悕璁块棶鐨勫湴鍧€
     */
    @Autowired
    private PermitAllUrlProperties permitAllUrl;

    /**
     * 瑙ｅ喅 鏃犳硶鐩存帴娉ㄥ叆 AuthenticationManager
     *
     * @return
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * anyRequest | 鍖归厤鎵€鏈夎姹傝矾寰?
     * access | SpringEl琛ㄨ揪寮忕粨鏋滀负true鏃跺彲浠ヨ闂?
     * anonymous | 鍖垮悕鍙互璁块棶
     * denyAll | 鐢ㄦ埛涓嶈兘璁块棶
     * fullyAuthenticated | 鐢ㄦ埛瀹屽叏璁よ瘉鍙互璁块棶锛堥潪remember-me涓嬭嚜鍔ㄧ櫥褰曪級
     * hasAnyAuthority | 濡傛灉鏈夊弬鏁帮紝鍙傛暟琛ㄧず鏉冮檺锛屽垯鍏朵腑浠讳綍涓€涓潈闄愬彲浠ヨ闂?
     * hasAnyRole | 濡傛灉鏈夊弬鏁帮紝鍙傛暟琛ㄧず瑙掕壊锛屽垯鍏朵腑浠讳綍涓€涓鑹插彲浠ヨ闂?
     * hasAuthority | 濡傛灉鏈夊弬鏁帮紝鍙傛暟琛ㄧず鏉冮檺锛屽垯鍏舵潈闄愬彲浠ヨ闂?
     * hasIpAddress | 濡傛灉鏈夊弬鏁帮紝鍙傛暟琛ㄧずIP鍦板潃锛屽鏋滅敤鎴稩P鍜屽弬鏁板尮閰嶏紝鍒欏彲浠ヨ闂?
     * hasRole | 濡傛灉鏈夊弬鏁帮紝鍙傛暟琛ㄧず瑙掕壊锛屽垯鍏惰鑹插彲浠ヨ闂?
     * permitAll | 鐢ㄦ埛鍙互浠绘剰璁块棶
     * rememberMe | 鍏佽閫氳繃remember-me鐧诲綍鐨勭敤鎴疯闂?
     * authenticated | 鐢ㄦ埛鐧诲綍鍚庡彲璁块棶
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // CSRF绂佺敤锛屽洜涓轰笉浣跨敤session
                .csrf(csrf -> csrf.disable())
                // 绂佺敤HTTP鍝嶅簲鏍囧ご
                .headers(headers -> headers.cacheControl().disable())
                // 璁よ瘉澶辫触澶勭悊绫?
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                // 鍩轰簬token锛屾墍浠ヤ笉闇€瑕乻ession
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 杩囨护璇锋眰
                .authorizeHttpRequests(authorize -> authorize
                        // 瀵逛簬鐧诲綍login 娉ㄥ唽register 楠岃瘉鐮乧aptchaImage 鍏佽鍖垮悕璁块棶
                        .requestMatchers("/login", "/register", "/captchaImage").permitAll()
                        .requestMatchers("/watch/push").permitAll()
                        .requestMatchers("/ai/chat", "/ai/chat/**", "/ai/models", "/ai/test").permitAll()
                        .requestMatchers("/watch/require").permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/ws/**")).permitAll()
                        // 鍋ュ悍鏁版嵁妯℃嫙涓婁紶鎺ュ彛锛屽厑璁稿尶鍚嶈闂紙璁惧閴存潈锛?
                        .requestMatchers("/health/mock/**").permitAll()
                        // 闈欐€佽祫婧愶紝鍙尶鍚嶈闂?
                        .requestMatchers(HttpMethod.GET, "/", "/*.html", "/*.css", "/*.js", "/profile/**",
                                "/static/**", "/assets/**", "/css/**", "/js/**", "/img/**", "/images/**", "/fonts/**")
                        .permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-resources/**", "/v3/api-docs/**", "/webjars/**",
                                "/druid/**")
                        .permitAll()
                        // 鑷畾涔夊厑璁稿尶鍚嶈闂殑url
                        .requestMatchers(permitAllUrl.getUrls().toArray(new String[0])).permitAll()
                        // 闄や笂闈㈠鐨勬墍鏈夎姹傚叏閮ㄩ渶瑕侀壌鏉冭璇?
                        .anyRequest().authenticated())
                .headers(headers -> headers.frameOptions().disable());

        // 娣诲姞Logout filter
        httpSecurity.logout(logout -> logout.logoutUrl("/logout").logoutSuccessHandler(logoutSuccessHandler));
        // 娣诲姞JWT filter
        httpSecurity.addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
        // 娣诲姞CORS filter
        httpSecurity.addFilterBefore(corsFilter, JwtAuthenticationTokenFilter.class);
        httpSecurity.addFilterBefore(corsFilter, LogoutFilter.class);

        return httpSecurity.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(new AntPathRequestMatcher("/ws/**"));
    }

    /**
     * 寮烘暎鍒楀搱甯屽姞瀵嗗疄鐜?
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 韬唤璁よ瘉鎺ュ彛
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(bCryptPasswordEncoder());
        return authProvider;
    }
}

