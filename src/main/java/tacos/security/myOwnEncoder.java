package tacos.security;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author Yuyuan Huang
 * @create 2021-03-06 18:15
 */
public interface myOwnEncoder extends PasswordEncoder {
    @Override
    String encode(CharSequence rawPassword);

    @Override
    boolean matches(CharSequence charSequence, String s);


}
