package com.example.springsecurityapplication.config;

import com.example.springsecurityapplication.services.PersonDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final PersonDetailService personDetailService;

    @Bean
    public PasswordEncoder getPasswordEncode() {

        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //Конфигурируем работу Spring Security
        http.csrf().disable().//Отключаем защиту от межсайтовой подделки запросов
//        http.
                        authorizeHttpRequests(). //Указываем что все страницы должны быть защищены
                        //Указываем на то что данная страница admin доступна пользователю с ролью ADMIN
                        requestMatchers("/admin").hasRole("ADMIN").                       //Указываем что не аутентифицированые пользователи могут зайти на страницу аутентификации и на объект ошибки
                        // с помощью permitAll уазываем не аутентифицированые пользователи могут заходить на перечисленные страницы
                        requestMatchers("/authentication", "/error", "/registration", "/resources/**", "/static/**", "/css/**", "/js/**", "/img/**", "/product", "/product/info/{id}", "/product/search").permitAll().
                        // Указываем что для всех остальных страниц необходимо вызвать метод authenticated() который открывает форму аутентификации
                        //anyRequest().authenticated().

                        anyRequest().hasAnyRole("USER", "ADMIN").

                        and(). // Указываем что дальше настраивается аутентификация и соединяем ее с настройкой
                        formLogin().loginPage("/authentication"). // Указываем какой url запрос будет отправляется при заходе на защищенные страницы
                        loginProcessingUrl("/process_login"). // Указываем на какой адрес будет отпраляться данные с формы.
                        // Нам уже не нужно будет создавать метод в контроллере и обрабатывать данные с формы.
                        //Мы задали url, который используется по умолчанию для обработки формы аутентификации
                        //по средствам Spring Security. Spring Security будет ждать объект с формы
                        //аутентификации и затем сверять логин и пароль с. данными БД
                        defaultSuccessUrl("/person_account", true).// Указываем на какой url необходимо
                        //направить пользователя после успешной аутентификации. Вторым аргументом указываем true
                        //чтобы перенаправление шло в любом случае после успешной аутентификации.
                        failureUrl("/authentication?error"). //Указываем куда необходимо пенеправить
                        //пользователя при проваленной аутентификации
                        //В запрос будет передан объект error, который будет проверятся на форме и при наличии данного объекта
                        //в запросе выводится сообщение "Неправильный логин или пароль"
                        and().
                        logout().logoutUrl("/logout").logoutSuccessUrl("/authentication");
        return http.build();

    }

    @Autowired
    public SecurityConfig(PersonDetailService personDetailService) {
        this.personDetailService = personDetailService;
    }


    protected void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception{
        authenticationManagerBuilder.userDetailsService(personDetailService).
                passwordEncoder(getPasswordEncode());
    }
}
