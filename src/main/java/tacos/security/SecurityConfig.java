package tacos.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;

import javax.sql.DataSource;

import static org.hibernate.internal.util.StringHelper.partiallyUnqualify;
import static org.hibernate.internal.util.StringHelper.root;

/**
 * @author Yuyuan Huang
 * @create 2021-01-27 14:19
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
//    @Autowired 基于数据库JDBC使用数据源
//    DataSource dataSource;
    @Bean
    public PasswordEncoder encoder(){
        return new StandardPasswordEncoder("53cr3t");
    }
    @Autowired
    private UserDetailsService userDetailsService;
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception{
        auth.userDetailsService(userDetailsService)
        .passwordEncoder(new BCryptPasswordEncoder());
        /**
         * @author Yuyuan Huang
         * @create 2021-01-27 14:19
         * 存在内存的用户信息存储
         * auth.inMemoryAuthentication()
         *                 .withUser("buzz")
         *                 .password("infinity")
         *                 .authorities("ROLE_USER")
         *                 .and()
         *                 .withUser("woody")
         *                 .password("bullseye")
         *                 .authorities("ROLE_USER");
         *
         *
         */
        /**
         * 基于数据库JDBC的用户信息存储
         * 数据库中的密码始终是加密的，用用户输入的明文密码加密后对比
         * auth.jdbcAuthentication()
                .dataSource(dataSource)
                .usersByUsernameQuery(
                        "select username,password,enabled from Users"+
                                "where username=?")
                .authoritiesByUsernameQuery(
                        "select username,authority from UserAuthorities"+
                                "where username=?")
                .passwordEncoder(new StandardPasswordEncoder("53cr3t"));
         */
        /**
         * auth.ldapAuthentication()
                .userSearchBase("ou=people")
                .userSearchFilter("(uid={0})")
                .groupSearchBase("ou=groups")
                .groupSearchFilter("member={0}")
                .passwordCompare()
                .passwordEncoder(new BCryptPasswordEncoder())
                .passwordAttribute("passcode");

//                .contextSource()
//        .root("dc=tacocloud,dc=com");
//                .url("ldap://tacocloud.com:389/dc=tacocloud,dc=com");
         */
        //自定义用户认证

    }
    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception{
        httpSecurity
                .authorizeRequests()
                .antMatchers("/design","/orders")
                .hasRole("ROLE_USER")
                .antMatchers("/","/**")
                .permitAll()
                .and()
                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/authenticate")
                .usernameParameter("user")
                .passwordParameter("pwd")
                .defaultSuccessUrl("/design")
                .and()//and连接配置块，并设置退出的页面
                .logout()
                .logoutSuccessUrl("/");
    }

//    @Override
//    protected void configure(HttpSecurity httpSecurity) throws  Exception{
//        //使用Spring SPEL表达式
//        httpSecurity
//                .authorizeRequests()
//                .antMatchers("/design","/orders")
//                .access("hasRole('ROLE_USER')")
//                .antMatchers("/","/**")
//                .access("permitAll");
//
//    }
//    @Override
//    protected void configure(HttpSecurity httpSecurity)throws Exception{
//        //使用Spring SPEL表达式，规定只有周二才能下单
//        httpSecurity
//                .authorizeRequests()
//                .antMatchers("/design","order")
//                .access("hasRole('ROLE_USER')&&"+"T(java.util.Calendar).getInstance().get("+"T(java.util.Calendar).DAY_OF_WEEK)=="+"T(java.util.Calendar).TUESDAY")
//                .antMatchers("/","/**")
//                .access("permitAll");
//    }
}
